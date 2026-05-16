package indigo

// import indigo.core.events.ScreenCaptureEvent
// import indigo.internal.IndigoActions
// import indigo.internal.IndigoWatchers
// import indigo.internal.Utils
// import indigo.internal.WorldEventWatchers
import indigo.internal.models.LaunchStatus
import indigo.internal.models.Model
import indigo.internal.models.Msg
import tyrian.*
import tyrian.extensions.Extension
import tyrian.extensions.ExtensionId

final case class Indigo(
    extensionId: ExtensionId,
    args: Array[String],
    game: Game[?, ?, ?],
    // containerMarkerId: MarkerId,
    onLaunchSuccess: Option[GlobalMsg],
    onLaunchFailure: Option[GlobalMsg],
    eventMapping: PartialIso[GlobalMsg, GlobalEvent],
    settings: Settings
) extends Extension.Graphical[SDLContext]:

  type ExtensionModel = Model

  // private val canvasId: String = s"${game.gameId.asString}-canvas"

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
      // model._canvas match
      //   case None =>
      //     Result(model)

      //   case Some(canvas) =>
      //     Result(model)
      //       .addActions(Action.sideEffect(Utils.runFullScreen(canvas, model.game, request)))

    case Msg.LoadAssets(assets, key, makeAvailable) =>
      Result(model)
      // .addActions(Action.sideEffect(Utils.runLoadAssets(model.game, assets, key, makeAvailable)))

    case Msg.CaptureScreen(config, key) =>
      Result(model)
      // model._canvas match
      //   case None =>
      //     model.game.events.push(ScreenCaptureEvent.CaptureError(key, "No canvas available"))
      //     Result(model)

      //   case Some(canvas) =>
      //     Result(model)
      //       .addActions(Action.sideEffect(Utils.runCaptureScreen(model.game, canvas, config, key)))

    case Msg.PlaySound(assetName, volume, policy) =>
      Result(model)
      // .addActions(Action.sideEffect(model._audioPlayer.playSound(assetName, volume, policy)))

    case Msg.Halt(gameId) =>
      if game.gameId == gameId then
        Result(model.copy(running = false))
          .addActions(
            Action.sideEffect {
              game.system.halt()
            }
          )
      else Result(model)

    // case Msg.GameTick(gameId, runningTime) =>
    //   Result(model)
    // if game.gameId == gameId && model.running then
    //   Utils
    //     .processFrameTick(
    //       model.lastUpdated,
    //       runningTime,
    //       settings.frameRatePolicy
    //     )
    //     .flatMap {
    //       case TickUpdateResult.Wait =>
    //         Result(model)

    //       case TickUpdateResult.RunNow(timeDelta, updatedAt) =>
    //         Result(model.copy(lastUpdated = updatedAt))
    //           .addActions(
    //             Action.sideEffect {
    //               game.system.tick(updatedAt, timeDelta)
    //             }
    //           )
    //     }
    // else Result(model)

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
      Result(model)
      // .addActions(
      //   Indigo.launchAction(
      //     extensionId,
      //     model.game,
      //     args,
      //     IndigoCoreServices(
      //       NativeGamepadInputService(),
      //       model._audioPlayer,
      //       NativeImageService()
      //     ),
      //     context
      //   )
      // )
      // if extId == extensionId then
      //   val maybeCanvas =
      //     Option(document.getElementById(canvasId))
      //       .flatMap(e => if e.isInstanceOf[html.Canvas] then Option(e.asInstanceOf[html.Canvas]) else None)

      //   Result(
      //     model.copy(
      //       _eventWatchers =
      //         maybeCanvas.map(c => WorldEventWatchers.init(c, settings.clickTime, settings.disableContextMenu)),
      //       _canvas = maybeCanvas,
      //       _container = maybeCanvas.map(_.parentElement)
      //     )
      //   )
      //     .addActions(
      //       IndigoActions.launch(
      //         extensionId,
      //         model.game,
      //         maybeCanvas,
      //         flags,
      //         settings,
      //         IndigoCoreServices(
      //           BrowserGamepadInputService(),
      //           model._audioPlayer,
      //           BrowserImageService()
      //         )
      //       )
      //     )
      // else Result(model)

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

  // private given Theme = Theme.None

  def view(model: ExtensionModel): TerminalFragment =
    TerminalFragment.empty
  // def view(model: Model): HtmlFragment =
  //   HtmlFragment.insert(
  //     containerMarkerId,
  //     Canvas(
  //       width = Extent.Fill,
  //       height = Extent.Fill
  //     )
  //       .withId(canvasId)
  //       .toElem
  //   )

  def watchers(model: Model): Batch[Watcher] =
    Batch.empty
    // val gameTickWatcher =
    //   if model.running then Batch(IndigoWatchers.tick(game.gameId))
    //   else Batch.empty

    // val resizeWatcher =
    //   (model._canvas, model._container) match
    //     case (Some(cvs), Some(con)) =>
    //       Batch(IndigoWatchers.resize(game.gameId, cvs, con))

    //     case _ =>
    //       Batch.empty

    // val worldEventWatchers =
    //   model._eventWatchers match
    //     case None    => Batch.empty
    //     case Some(w) => w.watchers

    // Batch.fromOption(
    //   model.game.events.eventCallback.map: eventCallback =>
    //     IndigoWatchers.indigoEventWatcher(extensionId, eventMapping, eventCallback)
    // ) ++
    //   gameTickWatcher ++ resizeWatcher ++ worldEventWatchers

  def draw(ctx: SDLContext, runningTime: Seconds, timeDelta: Seconds, model: ExtensionModel): Unit =
    model.game.system.tick(ctx, runningTime, timeDelta)

object Indigo:

  val MaxStartupAttempts: Int = 10

  def apply(
      extensionId: ExtensionId,
      args: Array[String],
      game: Game[?, ?, ?]
      // containerMarkerId: MarkerId
  ): Indigo =
    Indigo(
      extensionId,
      args,
      game,
      // containerMarkerId,
      None,
      None,
      PartialIso.none,
      Settings.default
    )

  def apply(
      extensionId: ExtensionId,
      args: Array[String],
      game: Game[?, ?, ?],
      // containerMarkerId: MarkerId,
      onLaunchSuccess: GlobalMsg,
      onLaunchFailure: GlobalMsg
  ): Indigo =
    Indigo(
      extensionId,
      args,
      game,
      // containerMarkerId,
      Some(onLaunchSuccess),
      Some(onLaunchFailure),
      PartialIso.none,
      Settings.default
    )

  // Move to IndigoActions
  // private def launchAction(
  //     extensionId: ExtensionId,
  //     game: Game[?, ?, ?],
  //     args: Array[String],
  //     services: IndigoCoreServices[TempImageData, Array[Byte]],
  //     ctx: SDL_GLContext
  // ): Action =
  //   Action.run {
  //     game.launch(
  //       initialWidth = 800,   // : Int,
  //       initialHeight = 600,  // : Int,
  //       context = ctx, // : String, // Fake, obvs.
  //       args = args,          // : Array[String],
  //       services = services   // : IndigoCoreServices[TempImageData, Array[Byte]]
  //     )
  //     Msg.Launch(LaunchStatus.Started(extensionId))
  //   }
