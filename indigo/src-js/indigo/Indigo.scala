package indigo

import cats.effect.IO
import indigo.launchers.GameLauncher
import indigo.platform.events.GlobalEventCallback
import org.scalajs.dom.Element
import org.scalajs.dom.document
import tyrian.Action
import tyrian.GlobalMsg
import tyrian.HtmlFragment
import tyrian.Result
import tyrian.Watcher
import tyrian.classic.Sub
import tyrian.extensions.Extension
import tyrian.extensions.ExtensionId

final case class Indigo(
    extensionId: ExtensionId,
    flags: Map[String, String],
    game: Game[?, ?, ?] | GameLauncher[?, ?],
    find: () => Option[Element],
    onLaunchSuccess: Option[GlobalMsg],
    onLaunchFailure: Option[GlobalMsg],
    eventMapping: PartialIso[GlobalMsg, GlobalEvent]
) extends Extension:

  private val MaxAttempts: Int = 10

  type ExtensionModel = Indigo.ExtensionModel

  def withExtensionId(value: ExtensionId): Indigo =
    this.copy(extensionId = value)

  def withFlags(value: Map[String, String]): Indigo =
    this.copy(flags = value)

  def withGame(value: Game[?, ?, ?] | GameLauncher[?, ?]): Indigo =
    this.copy(game = value)

  def withFind(value: () => Option[Element]): Indigo =
    this.copy(find = value)
  def findById(containerId: String): Indigo =
    withFind(() => Option(document.getElementById(containerId)))

  def withOnLaunchSuccess(value: Option[GlobalMsg]): Indigo =
    this.copy(onLaunchSuccess = value)
  def sendLaunchSuccessMsg(value: GlobalMsg): Indigo =
    this.copy(onLaunchSuccess = Some(value))
  def noLaunchSuccessMsg: Indigo =
    this.copy(onLaunchSuccess = None)

  def withOnLaunchFailure(value: Option[GlobalMsg]): Indigo =
    this.copy(onLaunchFailure = value)
  def sendLaunchFailureMsg(value: GlobalMsg): Indigo =
    this.copy(onLaunchFailure = Some(value))
  def noLaunchFailureMsg: Indigo =
    this.copy(onLaunchFailure = None)

  def withEventMapping(value: PartialIso[GlobalMsg, GlobalEvent]): Indigo =
    this.copy(eventMapping = value)

  def id: ExtensionId = extensionId

  def init: Result[ExtensionModel] =
    Result(
      Indigo.ExtensionModel(
        game,
        MaxAttempts
      )
    )
      .addGlobalMsgs(Indigo.LaunchMsg.AttemptStart(extensionId))

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel] =
    case Indigo.LaunchMsg.Retry(extId) if extId == extensionId && model.attempts <= 0 =>
      Result(model)
        .addActions(Action.emit(Indigo.LaunchMsg.Failed(extId)))

    case Indigo.LaunchMsg.Retry(extId) if extId == extensionId =>
      val nextDelay =
        val x = MaxAttempts - model.attempts
        Millis(x * x * 100L)

      Result(model.copy(attempts = model.attempts - 1))
        .addActions(Action.emitAfterDelay(Indigo.LaunchMsg.AttemptStart(extensionId), nextDelay))
        .log(
          s"Indigo Extension failed to find the required container element in the dom, will retry in ${nextDelay.toSeconds.toString()} seconds..."
        )

    case Indigo.LaunchMsg.AttemptStart(extId) if extId == extensionId =>
      Result(model)
        .addActions(Indigo.launchAction(extensionId, model.game, find, flags))

    case Indigo.LaunchMsg.Started(extId) if extId == extensionId =>
      onLaunchSuccess match
        case None =>
          Result(model)

        case Some(msg) =>
          Result(model)
            .addGlobalMsgs(msg)
            .log("Indigo Extension successfully launched the game.")

    case Indigo.LaunchMsg.Failed(extId) if extId == extensionId =>
      onLaunchFailure match
        case None =>
          Result(model)

        case Some(msg) =>
          Result(model)
            .addGlobalMsgs(msg)
            .log(s"Indigo Extention failed to launch the game after $MaxAttempts attempts.")

    case msg =>
      // Push events.
      model.game match
        case g: Game[?, ?, ?] =>
          eventMapping
            .to(msg)
            .foreach: e =>
              g.events.push(e)

        case g =>
          ()

      Result(model)

  def view(model: ExtensionModel): HtmlFragment =
    HtmlFragment.empty

  def watchers(model: ExtensionModel): Batch[Watcher] =
    model.game match
      case _: GameLauncher[?, ?] =>
        Batch.empty

      case g: Game[?, ?, ?] =>
        Batch.fromOption(
          g.events.eventCallback.map: eventCallback =>
            Indigo.indigoEventWatcher(extensionId, eventMapping, eventCallback)
        )

object Indigo:

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?] | GameLauncher[?, ?],
      containerId: String
  ): Indigo =
    Indigo(
      extensionId,
      flags,
      game,
      () => Option(document.getElementById(containerId)),
      None,
      None,
      PartialIso.none
    )

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?] | GameLauncher[?, ?],
      find: () => Option[Element]
  ): Indigo =
    Indigo(
      extensionId,
      flags,
      game,
      find,
      None,
      None,
      PartialIso.none
    )

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?] | GameLauncher[?, ?],
      find: () => Option[Element],
      onLaunchSuccess: GlobalMsg,
      onLaunchFailure: GlobalMsg
  ): Indigo =
    Indigo(
      extensionId,
      flags,
      game,
      find,
      Some(onLaunchSuccess),
      Some(onLaunchFailure),
      PartialIso.none
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def launchAction(
      extensionId: ExtensionId,
      game: Game[?, ?, ?] | GameLauncher[?, ?],
      find: () => Option[Element],
      flags: Map[String, String]
  ): Action =
    Action.run {
      find() match
        case Some(elem) if elem != null =>
          game.launch(elem, flags)
          Indigo.LaunchMsg.Started(extensionId)

        case _ =>
          Indigo.LaunchMsg.Retry(extensionId)
    }

  enum LaunchMsg extends GlobalMsg:
    case Retry(extensionId: ExtensionId)
    case AttemptStart(extensionId: ExtensionId)
    case Started(extensionId: ExtensionId)
    case Failed(extensionId: ExtensionId)

  final case class ExtensionModel(game: Game[?, ?, ?] | GameLauncher[?, ?], attempts: Int)

  private def indigoEventWatcher(
      extensionId: ExtensionId,
      eventMapping: PartialIso[GlobalMsg, GlobalEvent],
      globalEventStream: GlobalEventCallback
  ): Watcher =
    val sub = Sub.Observe[IO, GlobalEvent, GlobalMsg, Unit](
      id = "indigo-event-exchange-" + extensionId.toString,
      acquire = (callback: Either[Throwable, GlobalEvent] => Unit) =>
        IO(
          globalEventStream.registerEventCallback(event => callback(Right(event)))
        ),
      release = (_: Unit) =>
        IO(
          globalEventStream.clearEventCallback()
        ),
      toMsg = (event: GlobalEvent) => eventMapping.from(event)
    )
    Watcher(sub)
