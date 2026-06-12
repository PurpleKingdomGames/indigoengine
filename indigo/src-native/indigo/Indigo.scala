package indigo

import indigo.internal.IndigoActions
import indigo.internal.LogDrainWatcher
import indigo.internal.Utils
import indigo.internal.models.LaunchStatus
import indigo.internal.models.Model
import indigo.internal.models.Msg
import indigo.internal.models.TickUpdateResult
import indigo.internal.services.NativeGamepadInputService
import indigo.internal.services.NativeImageService
import indigo.platform.IndigoCoreServices
import tyrian.*

final case class Indigo(
    extensionId: ExtensionId,
    args: Array[String],
    game: Game[?, ?, ?],
    onLaunchSuccess: Option[GlobalMsg],
    onLaunchFailure: Option[GlobalMsg],
    eventMapping: PartialIso[GlobalMsg, GlobalEvent],
    settings: Settings
) extends Extension.Graphical[SDLContext, TerminalFragment]:

  type ExtensionModel = Model

  def withExtensionId(value: ExtensionId): Indigo =
    this.copy(extensionId = value)

  def withArgs(value: Array[String]): Indigo =
    this.copy(args = value)

  def withGame(value: Game[?, ?, ?]): Indigo =
    this.copy(game = value)

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

  def withSettings(value: Settings): Indigo =
    this.copy(settings = value)

  def id: ExtensionId = extensionId

  def init: Result[Model] =
    Result(Model(game))
      .addGlobalMsgs(Msg.Launch(LaunchStatus.AttemptStart(extensionId)))

  def update(model: Model): GlobalMsg => Result[Model] =
    case m: Msg =>
      handleMsg(model)(m)

    case msg =>
      eventMapping
        .to(msg)
        .foreach: e =>
          model.game.events.push(e)

      Result(model)

  private def handleMsg(model: Model): Msg => Result[Model] =
    case Msg.WorldEvents(events) =>
      events.foreach: e =>
        model.game.events.push(e)

      Result(model)

    case Msg.CanvasResize(w, h) =>
      model.game.events.push(ViewportResize(Size(w, h)))

      Result(model)

    case Msg.FullScreen(request) =>
      Result(model)

    case Msg.LoadAssets(assets, key, makeAvailable) =>
      Result(model)

    case Msg.CaptureScreen(config, key) =>
      Result(model)

    case Msg.PlaySound(assetName, volume, policy) =>
      Result(model)

    case Msg.Log(_, text) =>
      Result(model).log(text)

    case Msg.Halt(gameId) =>
      if game.gameId == gameId then
        Result(model.copy(running = false))
          .addActions(
            Action.sideEffect {
              game.system.halt()
            }
          )
      else Result(model)

    case Msg.Launch(LaunchStatus.Retry(extId)) =>
      if extId == extensionId && model.attempts <= 0 then
        Result(model)
          .addActions(Action.emit(Msg.Launch(LaunchStatus.Failed(extId))))
      else if extId == extensionId then
        val nextDelay =
          val x = Indigo.MaxStartupAttempts - model.attempts
          Millis(x * x * 100L)

        Result(model.copy(attempts = model.attempts - 1))
          .addActions(
            Action.emitAfterDelay(Msg.Launch(LaunchStatus.AttemptStart(extensionId)), nextDelay)
          )
          .log(
            s"Indigo Extension failed to find the required container element in the dom, will retry in ${nextDelay.toSeconds.toString()} seconds..."
          )
      else Result(model)

    case Msg.Launch(LaunchStatus.AttemptStart(extId)) =>
      if extId == extensionId then
        Result(model)
          .addActions(
            IndigoActions.launch(
              extensionId,
              model.game,
              args,
              IndigoCoreServices(
                NativeGamepadInputService(),
                model._audioPlayer,
                NativeImageService()
              )
            )
          )
      else Result(model)

    case Msg.Launch(LaunchStatus.Started(extId)) =>
      if extId == extensionId then
        onLaunchSuccess match
          case None =>
            Result(model)

          case Some(msg) =>
            Result(model)
              .addGlobalMsgs(msg)
              .log("Indigo Extension successfully launched the game.")
      else Result(model)

    case Msg.Launch(LaunchStatus.Failed(extId)) =>
      if extId == extensionId then
        onLaunchFailure match
          case None =>
            Result(model)

          case Some(msg) =>
            Result(model)
              .addGlobalMsgs(msg)
              .log(s"Indigo Extension failed to launch the game after ${Indigo.MaxStartupAttempts} attempts.")
      else Result(model)

  def view(model: Model): TerminalFragment =
    TerminalFragment.empty

  def watchers(model: Model): Batch[Watcher] =
    Batch(LogDrainWatcher(game))

  def draw(ctx: SDLContext, runningTime: Seconds, model: Model): Model =
    val timeDelta = runningTime - model.lastUpdatedAt

    Utils.processFrameTick(runningTime, timeDelta, settings.frameRatePolicy) match
      case TickUpdateResult.Wait =>
        model

      case TickUpdateResult.RunNow(timeDelta, updatedAt) =>
        model.game.system.tick(ctx, updatedAt, timeDelta)
        model.copy(
          lastUpdatedAt = updatedAt
        )

  def provideContext(model: Model): Option[SDLContext] =
    None

  def prepare(model: Model): Unit =
    ()

  def teardown(model: Model): Unit =
    ()

object Indigo:

  val MaxStartupAttempts: Int = 10

  def apply(
      extensionId: ExtensionId,
      args: Array[String],
      game: Game[?, ?, ?]
  ): Indigo =
    Indigo(
      extensionId,
      args,
      game,
      None,
      None,
      PartialIso.none,
      Settings.default
    )

  def apply(
      extensionId: ExtensionId,
      args: Array[String],
      game: Game[?, ?, ?],
      onLaunchSuccess: GlobalMsg,
      onLaunchFailure: GlobalMsg
  ): Indigo =
    Indigo(
      extensionId,
      args,
      game,
      Some(onLaunchSuccess),
      Some(onLaunchFailure),
      PartialIso.none,
      Settings.default
    )
