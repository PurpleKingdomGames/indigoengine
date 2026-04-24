package indigo

import cats.effect.IO
import indigo.core.time.FPS
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
import tyrian.syntax.*

final case class Indigo(
    extensionId: ExtensionId,
    flags: Map[String, String],
    game: Game[?, ?, ?],
    find: () => Option[Element],
    onLaunchSuccess: Option[GlobalMsg],
    onLaunchFailure: Option[GlobalMsg],
    eventMapping: PartialIso[GlobalMsg, GlobalEvent],
    frameRatePolicy: FrameRatePolicy
) extends Extension:

  type ExtensionModel = Indigo.ExtensionModel

  def withExtensionId(value: ExtensionId): Indigo =
    this.copy(extensionId = value)

  def withFlags(value: Map[String, String]): Indigo =
    this.copy(flags = value)

  def withGame(value: Game[?, ?, ?]): Indigo =
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

  def withFrameRatePolicy(value: FrameRatePolicy): Indigo =
    this.copy(frameRatePolicy = value)
  def unlimitedFrameRate: Indigo =
    withFrameRatePolicy(FrameRatePolicy.Unlimited)
  def targetFrameRate(target: FPS): Indigo =
    withFrameRatePolicy(FrameRatePolicy.Skip(target))

  def id: ExtensionId = extensionId

  def init: Result[ExtensionModel] =
    Result(Indigo.ExtensionModel(game))
      .addGlobalMsgs(Indigo.LaunchMsg.AttemptStart(extensionId))

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel] =
    case m: Indigo.LaunchMsg =>
      handleLaunchMsg(model)(m)

    case Indigo.GameTick(gameId, runningTime) if game.gameId == gameId =>
      Indigo
        .processFrameTick(
          model.lastUpdated,
          runningTime,
          frameRatePolicy
        )
        .flatMap {
          case Indigo.TickUpdateResult.Wait =>
            Result(model)

          case Indigo.TickUpdateResult.RunNow(timeDelta, updatedAt) =>
            Result(model.copy(lastUpdated = updatedAt))
              .addActions(
                Action.sideEffect {
                  game.system.tick(updatedAt, timeDelta)
                }
              )
        }

    case msg =>
      eventMapping
        .to(msg)
        .foreach: e =>
          model.game.events.push(e)

      Result(model)

  private def handleLaunchMsg(model: Indigo.ExtensionModel): Indigo.LaunchMsg => Result[Indigo.ExtensionModel] =
    case Indigo.LaunchMsg.Retry(extId) if extId == extensionId && model.attempts <= 0 =>
      Result(model)
        .addActions(Action.emit(Indigo.LaunchMsg.Failed(extId)))

    case Indigo.LaunchMsg.Retry(extId) if extId == extensionId =>
      val nextDelay =
        val x = Indigo.MaxStartupAttempts - model.attempts
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
            .log(s"Indigo Extension failed to launch the game after $Indigo.MaxStartupAttempts attempts.")

    case _: Indigo.LaunchMsg =>
      // Msg was not intended to be consumed by this game instance.
      Result(model)

  def view(model: ExtensionModel): HtmlFragment =
    HtmlFragment.empty

  def watchers(model: ExtensionModel): Batch[Watcher] =
    Batch.fromOption(
      model.game.events.eventCallback.map: eventCallback =>
        Indigo.indigoEventWatcher(extensionId, eventMapping, eventCallback)
    ) ++ Batch(Indigo.tick(game.gameId))

object Indigo:

  val MaxStartupAttempts: Int = 10

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?],
      containerId: String
  ): Indigo =
    Indigo(
      extensionId,
      flags,
      game,
      () => Option(document.getElementById(containerId)),
      None,
      None,
      PartialIso.none,
      FrameRatePolicy.Skip(FPS.`60`)
    )

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?],
      find: () => Option[Element]
  ): Indigo =
    Indigo(
      extensionId,
      flags,
      game,
      find,
      None,
      None,
      PartialIso.none,
      FrameRatePolicy.Skip(FPS.`60`)
    )

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?],
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
      PartialIso.none,
      FrameRatePolicy.Skip(FPS.`60`)
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def launchAction(
      extensionId: ExtensionId,
      game: Game[?, ?, ?],
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

  final case class ExtensionModel(
      game: Game[?, ?, ?],
      attempts: Int,
      lastUpdated: Seconds
  )
  object ExtensionModel:
    def apply(game: Game[?, ?, ?]): ExtensionModel =
      ExtensionModel(
        game,
        MaxStartupAttempts,
        Seconds.zero
      )

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

  private[indigo] def processFrameTick(
      lastUpdated: Seconds,
      runningTime: Seconds,
      frameRatePolicy: FrameRatePolicy
  ): Result[TickUpdateResult] =
    val timeSinceLastUpdate = runningTime - lastUpdated

    frameRatePolicy match
      case FrameRatePolicy.Unlimited =>
        Result(TickUpdateResult.RunNow(timeSinceLastUpdate, runningTime))

      case FrameRatePolicy.Skip(target) =>
        val targetFrameDuration = target.toSeconds // E.g. 16.7ms or 0.016s for 60fps

        if timeSinceLastUpdate >= targetFrameDuration then
          Result(TickUpdateResult.RunNow(timeSinceLastUpdate, runningTime))
        else Result(TickUpdateResult.Wait)

  def tick(gameId: GameId): Watcher =
    Watcher.animationFrameTick(s"[indigo-tick:${gameId.asString}]") { runningTime =>
      GameTick(gameId, runningTime)
    }

  final case class GameTick(gameId: GameId, runningTime: Seconds) extends GlobalMsg

  enum TickUpdateResult derives CanEqual:
    case Wait
    case RunNow(timeDelta: Seconds, updatedAt: Seconds)
