package indigo.next

import indigo.launchers.GameLauncher
import indigo.next.bridge.BridgeMsg
import org.scalajs.dom.Element
import org.scalajs.dom.document
import tyrian.Action
import tyrian.GlobalMsg
import tyrian.HtmlFragment
import tyrian.Result
import tyrian.Watcher
import tyrian.extensions.Extension
import tyrian.extensions.ExtensionId

final case class Indigo(
    extensionId: ExtensionId,
    game: Game[?, ?, ?] | GameLauncher[?, ?, ?],
    find: () => Option[Element],
    onLaunchSuccess: Option[GlobalMsg],
    onLaunchFailure: Option[GlobalMsg]
) extends Extension:

  private val MaxAttempts: Int = 10

  type ExtensionModel = Indigo.ExtensionModel

  def withExtensionId(value: ExtensionId): Indigo =
    this.copy(extensionId = value)

  def withGame(value: Game[?, ?, ?] | GameLauncher[?, ?, ?]): Indigo =
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

  def id: ExtensionId = extensionId

  def init: Result[ExtensionModel] =
    Result(Indigo.ExtensionModel(game, MaxAttempts))
      .addGlobalMsgs(Indigo.LaunchMsg.AttemptStart)

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel] =
    case Indigo.LaunchMsg.Retry if model.attempts <= 0 =>
      Result(model)
        .addActions(Action.emit(Indigo.LaunchMsg.Failed))

    case Indigo.LaunchMsg.Retry =>
      val nextDelay =
        val x = MaxAttempts - model.attempts
        Millis(x * x * 100L)

      Result(model.copy(attempts = model.attempts - 1))
        .addActions(Action.emitAfterDelay(Indigo.LaunchMsg.AttemptStart, nextDelay))
        .log(
          s"Indigo Extension failed to find the required container element in the dom, will retry in ${nextDelay.toSeconds.toString()} seconds..."
        )

    case Indigo.LaunchMsg.AttemptStart =>
      Result(model)
        .addActions(Indigo.launchAction(model.game, find))

    case Indigo.LaunchMsg.Started =>
      onLaunchSuccess match
        case None =>
          Result(model)

        case Some(msg) =>
          Result(model)
            .addGlobalMsgs(msg)
            .log("Indigo Extension successfully launched the game.")

    case Indigo.LaunchMsg.Failed =>
      onLaunchFailure match
        case None =>
          Result(model)

        case Some(msg) =>
          Result(model)
            .addGlobalMsgs(msg)
            .log(s"Indigo Extention failed to launch the game after $MaxAttempts attempts.")

    case BridgeMsg.Send(data) =>
      game match
        case _: GameLauncher[?, ?, ?] =>
          Result(model)

        case g: Game[?, ?, ?] =>
          Result(model)
            .addActions(g.bridge.send(data))

    case _ =>
      Result(model)

  def view(model: ExtensionModel): HtmlFragment =
    HtmlFragment.empty

  def watchers(model: ExtensionModel): Batch[Watcher] =
    model.game match
      case _: GameLauncher[?, ?, ?] =>
        Batch.empty

      case g: Game[?, ?, ?] =>
        Batch(g.bridge.watch)

object Indigo:

  def apply(
      extensionId: ExtensionId,
      game: Game[?, ?, ?] | GameLauncher[?, ?, ?],
      containerId: String
  ): Indigo =
    Indigo(
      extensionId,
      game,
      () => Option(document.getElementById(containerId)),
      None,
      None
    )

  def apply(
      extensionId: ExtensionId,
      game: Game[?, ?, ?] | GameLauncher[?, ?, ?],
      find: () => Option[Element]
  ): Indigo =
    Indigo(
      extensionId,
      game,
      find,
      None,
      None
    )

  def apply(
      extensionId: ExtensionId,
      game: Game[?, ?, ?] | GameLauncher[?, ?, ?],
      find: () => Option[Element],
      onLaunchSuccess: GlobalMsg,
      onLaunchFailure: GlobalMsg
  ): Indigo =
    Indigo(
      extensionId,
      game,
      find,
      Some(onLaunchSuccess),
      Some(onLaunchFailure)
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def launchAction(game: Game[?, ?, ?] | GameLauncher[?, ?, ?], find: () => Option[Element]): Action =
    Action.run {
      find() match
        case Some(elem) if elem != null =>
          game.launch(elem, Map.empty[String, String])
          Indigo.LaunchMsg.Started

        case _ =>
          Indigo.LaunchMsg.Retry
    }

  enum LaunchMsg extends GlobalMsg:
    case Retry
    case AttemptStart
    case Started
    case Failed

  final case class ExtensionModel(game: Game[?, ?, ?] | GameLauncher[?, ?, ?], attempts: Int)
