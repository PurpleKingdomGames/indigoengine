package indigo

import cats.effect.IO
import indigo.core.assets.AssetName
import indigo.core.assets.AssetPath
import indigo.core.assets.AssetType
import indigo.core.audio.PlaybackPolicy
import indigo.core.audio.Volume
import indigo.core.datatypes.BindingKey
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Vector2
import indigo.core.events.AssetEvent
import indigo.core.events.PlaySound
import indigo.core.events.ScreenCaptureEvent
import indigo.core.render.ScreenCaptureConfig
import indigo.core.time.FPS
import indigo.internal.CanvasAndContext
import indigo.internal.WorldEventWatchers
import indigo.internal.assets.AssetLoader
import indigo.internal.services.AudioPlayer
import indigo.internal.services.BrowserGamepadInputService
import indigo.platform.IndigoCoreServices
import indigo.platform.events.GlobalEventCallback
import indigo.render.facades.WebGL2RenderingContext
import indigo.shared.IndigoSystemEvent
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.HTMLElement
import org.scalajs.dom.ResizeObserver
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.*
import tyrian.classic.Sub
import tyrian.extensions.Extension
import tyrian.extensions.ExtensionId
import tyrian.syntax.*
import tyrian.ui.Canvas
import tyrian.ui.Extent
import tyrian.ui.theme.Theme

import scala.annotation.nowarn
import scala.util.Failure
import scala.util.Success

final case class Indigo(
    extensionId: ExtensionId,
    flags: Map[String, String],
    game: Game[?, ?, ?],
    containerMarkerId: MarkerId,
    onLaunchSuccess: Option[GlobalMsg],
    onLaunchFailure: Option[GlobalMsg],
    eventMapping: PartialIso[GlobalMsg, GlobalEvent],
    settings: Indigo.Settings
) extends Extension:

  type ExtensionModel = Indigo.ExtensionModel

  def withExtensionId(value: ExtensionId): Indigo =
    this.copy(extensionId = value)

  def withFlags(value: Map[String, String]): Indigo =
    this.copy(flags = value)

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

  def withSettings(value: Indigo.Settings): Indigo =
    this.copy(settings = value)

  def id: ExtensionId = extensionId

  def init: Result[ExtensionModel] =
    Result(Indigo.ExtensionModel(game))
      .addGlobalMsgs(Indigo.Msg.Launch(Indigo.LaunchStatus.AttemptStart(extensionId)))

  def update(model: ExtensionModel): GlobalMsg => Result[ExtensionModel] =
    case m: Indigo.Msg =>
      handleMsg(model)(m)

    case msg =>
      eventMapping
        .to(msg)
        .foreach: e =>
          model.game.events.push(e)

      Result(model)

  private def handleMsg(model: Indigo.ExtensionModel): Indigo.Msg => Result[Indigo.ExtensionModel] =
    case Indigo.Msg.WorldEvents(events) =>
      events.foreach: e =>
        model.game.events.push(e)

      Result(model)

    case Indigo.Msg.CanvasResize(w, h) =>
      model.game.events.push(ViewportResize(Size(w, h)))

      Result(model)

    case Indigo.Msg.FullScreen(request) =>
      model._canvas match
        case None =>
          Result(model)

        case Some(canvas) =>
          Result(model)
            .addActions(Action.sideEffect(Indigo.runFullScreen(canvas, model.game, request)))

    case Indigo.Msg.LoadAssets(assets, key, makeAvailable) =>
      Result(model)
        .addActions(Action.sideEffect(Indigo.runLoadAssets(model.game, assets, key, makeAvailable)))

    case Indigo.Msg.CaptureScreen(config, key) =>
      model._canvas match
        case None =>
          model.game.events.push(ScreenCaptureEvent.CaptureError(key, "No canvas available"))
          Result(model)

        case Some(canvas) =>
          Result(model)
            .addActions(Action.sideEffect(Indigo.runCaptureScreen(model.game, canvas, config, key)))

    case Indigo.Msg.PlaySound(assetName, volume, policy) =>
      Result(model)
        .addActions(Action.sideEffect(model._audioPlayer.playSound(assetName, volume, policy)))

    case Indigo.Msg.Halt(gameId) =>
      if game.gameId == gameId then
        Result(model.copy(running = false))
          .addActions(
            Action.sideEffect {
              game.system.halt()
            }
          )
      else Result(model)

    case Indigo.Msg.GameTick(gameId, runningTime) =>
      if game.gameId == gameId && model.running then
        Indigo
          .processFrameTick(
            model.lastUpdated,
            runningTime,
            settings.frameRatePolicy
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
      else Result(model)

    case Indigo.Msg.Launch(Indigo.LaunchStatus.Retry(extId)) =>
      if extId == extensionId && model.attempts <= 0 then
        Result(model)
          .addActions(Action.emit(Indigo.Msg.Launch(Indigo.LaunchStatus.Failed(extId))))
      else if extId == extensionId then
        val nextDelay =
          val x = Indigo.MaxStartupAttempts - model.attempts
          Millis(x * x * 100L)

        Result(model.copy(attempts = model.attempts - 1))
          .addActions(
            Action.emitAfterDelay(Indigo.Msg.Launch(Indigo.LaunchStatus.AttemptStart(extensionId)), nextDelay)
          )
          .log(
            s"Indigo Extension failed to find the required container element in the dom, will retry in ${nextDelay.toSeconds.toString()} seconds..."
          )
      else Result(model)

    case Indigo.Msg.Launch(Indigo.LaunchStatus.AttemptStart(extId)) =>
      if extId == extensionId then
        val maybeCanvas =
          Option(document.getElementById(Indigo.CanvasId))
            .flatMap(e => if e.isInstanceOf[html.Canvas] then Option(e.asInstanceOf[html.Canvas]) else None)

        Result(
          model.copy(
            _eventWatchers =
              maybeCanvas.map(c => WorldEventWatchers.init(c, settings.clickTime, settings.disableContextMenu)),
            _canvas = maybeCanvas,
            _container = maybeCanvas.map(_.parentElement)
          )
        )
          .addActions(
            Indigo.launchAction(
              extensionId,
              model.game,
              maybeCanvas,
              flags,
              settings,
              IndigoCoreServices(
                BrowserGamepadInputService(),
                model._audioPlayer
              )
            )
          )
      else Result(model)

    case Indigo.Msg.Launch(Indigo.LaunchStatus.Started(extId)) =>
      if extId == extensionId then
        onLaunchSuccess match
          case None =>
            Result(model)

          case Some(msg) =>
            Result(model)
              .addGlobalMsgs(msg)
              .log("Indigo Extension successfully launched the game.")
      else Result(model)

    case Indigo.Msg.Launch(Indigo.LaunchStatus.Failed(extId)) =>
      if extId == extensionId then
        onLaunchFailure match
          case None =>
            Result(model)

          case Some(msg) =>
            Result(model)
              .addGlobalMsgs(msg)
              .log(s"Indigo Extension failed to launch the game after ${Indigo.MaxStartupAttempts} attempts.")
      else Result(model)

  private given Theme = Theme.None

  def view(model: ExtensionModel): HtmlFragment =
    HtmlFragment.insert(
      containerMarkerId,
      Canvas(
        width = Extent.Fill,
        height = Extent.Fill
      )
        .withId(Indigo.CanvasId)
        .toElem
    )

  def watchers(model: ExtensionModel): Batch[Watcher] =
    val gameTickWatcher =
      if model.running then Batch(Indigo.tick(game.gameId))
      else Batch.empty

    val resizeWatcher =
      (model._canvas, model._container) match
        case (Some(cvs), Some(con)) =>
          Batch(Indigo.resize(game.gameId, cvs, con))

        case _ =>
          Batch.empty

    val worldEventWatchers =
      model._eventWatchers match
        case None    => Batch.empty
        case Some(w) => w.watchers

    Batch.fromOption(
      model.game.events.eventCallback.map: eventCallback =>
        Indigo.indigoEventWatcher(extensionId, eventMapping, eventCallback)
    ) ++
      gameTickWatcher ++ resizeWatcher ++ worldEventWatchers

object Indigo:

  val CanvasId: String = "indigo-canvas"

  val MaxStartupAttempts: Int = 10

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?],
      containerMarkerId: MarkerId
  ): Indigo =
    Indigo(
      extensionId,
      flags,
      game,
      containerMarkerId,
      None,
      None,
      PartialIso.none,
      Settings.default
    )

  def apply(
      extensionId: ExtensionId,
      flags: Map[String, String],
      game: Game[?, ?, ?],
      containerMarkerId: MarkerId,
      onLaunchSuccess: GlobalMsg,
      onLaunchFailure: GlobalMsg
  ): Indigo =
    Indigo(
      extensionId,
      flags,
      game,
      containerMarkerId,
      Some(onLaunchSuccess),
      Some(onLaunchFailure),
      PartialIso.none,
      Settings.default
    )

  private def launchAction(
      extensionId: ExtensionId,
      game: Game[?, ?, ?],
      maybeCanvas: Option[html.Canvas],
      flags: Map[String, String],
      settings: Indigo.Settings,
      services: IndigoCoreServices
  ): Action =
    Action.run {
      maybeCanvas match
        case Some(canvas) =>
          val context: WebGL2RenderingContext =
            CanvasAndContext.setupContext(
              canvas,
              settings.premultipliedAlpha,
              settings.transparentBackground,
              settings.antiAliasing
            )

          game.launch(canvas.width, canvas.height, context, flags, services)
          Indigo.Msg.Launch(LaunchStatus.Started(extensionId))

        case _ =>
          Indigo.Msg.Launch(LaunchStatus.Retry(extensionId))
    }

  final case class ExtensionModel(
      game: Game[?, ?, ?],
      attempts: Int,
      lastUpdated: Seconds,
      running: Boolean,
      _eventWatchers: Option[WorldEventWatchers],
      _canvas: Option[html.Canvas],
      _container: Option[HTMLElement],
      _audioPlayer: AudioPlayer
  )
  object ExtensionModel:
    def apply(game: Game[?, ?, ?]): ExtensionModel =
      ExtensionModel(
        game,
        MaxStartupAttempts,
        Seconds.zero,
        running = true,
        None,
        None,
        None,
        AudioPlayer.init
      )

  private def indigoEventWatcher(
      extensionId: ExtensionId,
      eventMapping: PartialIso[GlobalMsg, GlobalEvent],
      globalEventStream: GlobalEventCallback
  ): Watcher =
    val toMsgHandler: GlobalEvent => Option[GlobalMsg] = {
      case FullScreenEvent.Enter =>
        Some(Indigo.Msg.FullScreen(FullScreenRequest.Enter))

      case FullScreenEvent.Exit =>
        Some(Indigo.Msg.FullScreen(FullScreenRequest.Exit))

      case FullScreenEvent.Toggle =>
        Some(Indigo.Msg.FullScreen(FullScreenRequest.Toggle))

      case AssetEvent.LoadAssets(batch, key, makeAvailable) =>
        Some(Indigo.Msg.LoadAssets(batch, key, makeAvailable))

      case ScreenCaptureEvent.Capture(config, key) =>
        Some(Indigo.Msg.CaptureScreen(config, key))

      case PlaySound(assetName, volume, policy) =>
        Some(Indigo.Msg.PlaySound(assetName, volume, policy))

      case event =>
        eventMapping.from(event)
    }

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
      toMsg = toMsgHandler
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
        val targetFrameDuration = target.asFrameDuration // E.g. 16.7ms or 0.016s for 60fps

        if timeSinceLastUpdate >= targetFrameDuration then
          Result(TickUpdateResult.RunNow(timeSinceLastUpdate, runningTime))
        else Result(TickUpdateResult.Wait)

  def tick(gameId: GameId): Watcher =
    Watcher.animationFrameTick(s"[indigo-tick:${gameId.asString}]") { runningTime =>
      Indigo.Msg.GameTick(gameId, runningTime)
    }

  def resize(gameId: GameId, canvas: html.Canvas, container: HTMLElement): Watcher = {
    val toMsg: ((Double, Double)) => Option[GlobalMsg] =
      dimensions => Some(Indigo.Msg.CanvasResize(dimensions._1.toInt, dimensions._2.toInt))

    val sub: Sub[IO, GlobalMsg] =
      Sub.make[IO, (Double, Double), GlobalMsg, ResizeObserver](s"[indigo-resize:${gameId.asString}]") { callback =>
        val ro =
          new ResizeObserver((_, _) => {
            /*
            This process does not currently respect `devicePixelRatio`, i.e.:

            ```
            val dpr    = Option(window.devicePixelRatio).getOrElse(1d)
            canvas.width = (bounds.width.toDouble * dpr).toInt
            canvas.height = (bounds.height.toDouble * dpr).toInt
            ```
            This keeps it consistent with the renderer. If the renderer
            stops reading the canvas directly, then we could bring this back.

            In theory, without accounting for device pixel ratio we could see
            blurry pixels on devices with high physical to css pixel ratios.
             */

            val bounds = container.getBoundingClientRect()

            canvas.width = bounds.width.toDouble.toInt
            canvas.height = bounds.height.toDouble.toInt

            callback(Right((bounds.width, bounds.height)))
          })

        ro.observe(container)

        IO(ro)
      }(ro => IO(ro.disconnect()))(toMsg)

    Watcher.fromSub(sub)
  }

  enum LaunchStatus:
    case Retry(extensionId: ExtensionId)
    case AttemptStart(extensionId: ExtensionId)
    case Started(extensionId: ExtensionId)
    case Failed(extensionId: ExtensionId)

  enum Msg extends GlobalMsg:
    case GameTick(gameId: GameId, runningTime: Seconds)
    case Halt(gameId: GameId)
    case Launch(status: LaunchStatus)
    case WorldEvents(events: Batch[GlobalEvent])
    case CanvasResize(width: Int, height: Int)
    case FullScreen(request: FullScreenRequest)
    case LoadAssets(assets: Set[AssetType], key: BindingKey, makeAvailable: Boolean)
    case CaptureScreen(config: ScreenCaptureConfig, key: BindingKey)
    case PlaySound(assetName: AssetName, volume: Volume, policy: PlaybackPolicy)

  enum FullScreenRequest derives CanEqual:
    case Enter, Exit, Toggle

  // Running as a cheeky Future, might be worth revisiting sometime...
  private def runFullScreen(canvas: html.Canvas, game: Game[?, ?, ?], request: FullScreenRequest): Unit =
    import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

    request match
      case FullScreenRequest.Enter =>
        canvas.requestFullscreen().toFuture.onComplete {
          case Success(_) =>
            game.events.push(FullScreenEvent.Entered)

          case Failure(_) =>
            game.events.push(FullScreenEvent.EnterError)
        }

      case FullScreenRequest.Exit =>
        document.exitFullscreen().toFuture.onComplete {
          case Success(_) =>
            game.events.push(FullScreenEvent.Exited)

          case Failure(_) =>
            game.events.push(FullScreenEvent.ExitError)
        }

      case FullScreenRequest.Toggle =>
        if Option(document.fullscreenElement).isEmpty then runFullScreen(canvas, game, FullScreenRequest.Enter)
        else runFullScreen(canvas, game, FullScreenRequest.Exit)

  private def runLoadAssets(
      game: Game[?, ?, ?],
      assets: Set[AssetType],
      key: BindingKey,
      makeAvailable: Boolean
  ): Unit =
    import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

    AssetLoader.loadAssets(assets).onComplete {
      case Success(ac) if makeAvailable =>
        game.events.push(IndigoSystemEvent.Rebuild(ac, AssetEvent.AssetBatchLoaded(key, assets, true)))

      case Success(_) =>
        game.events.push(AssetEvent.AssetBatchLoaded(key, assets, false))

      case Failure(e) =>
        game.events.push(AssetEvent.AssetBatchLoadError(key, e.getMessage))
    }

  @nowarn
  private def runCaptureScreen(
      game: Game[?, ?, ?],
      sourceCanvas: html.Canvas,
      config: ScreenCaptureConfig,
      key: BindingKey
  ): Unit =
    import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

    val image = captureCanvasToImage(sourceCanvas, config)

    AssetLoader.loadAssets(Set(image)).onComplete {
      case Success(ac) =>
        game.events.push(IndigoSystemEvent.Rebuild(ac, ScreenCaptureEvent.Captured(key, image)))

      case Failure(e) =>
        game.events.push(ScreenCaptureEvent.CaptureError(key, e.getMessage))
    }

  @nowarn
  private def captureCanvasToImage(
      sourceCanvas: html.Canvas,
      config: ScreenCaptureConfig
  ): AssetType.Image =
    val crop  = config.croppingRect.getOrElse(Rectangle(0, 0, sourceCanvas.width, sourceCanvas.height))
    val scale = config.scale.getOrElse(Vector2.one)
    val outW  = (crop.width * scale.x).toInt
    val outH  = (crop.height * scale.y).toInt

    val tmp = document.createElement("canvas").asInstanceOf[html.Canvas]
    tmp.width = outW
    tmp.height = outH
    val ctx2d = tmp.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    ctx2d.imageSmoothingEnabled = false
    ctx2d.drawImage(sourceCanvas, crop.x, crop.y, crop.width, crop.height, 0, 0, outW, outH)
    val dataUrl = tmp.toDataURL(config.imageType.toString)
    tmp.remove()

    AssetType.Image(
      AssetName(config.name.getOrElse(s"capture-${System.currentTimeMillis()}")),
      AssetPath(dataUrl)
    )

  enum TickUpdateResult derives CanEqual:
    case Wait
    case RunNow(timeDelta: Seconds, updatedAt: Seconds)

  final case class Settings(
      frameRatePolicy: FrameRatePolicy,
      antiAliasing: Boolean,
      premultipliedAlpha: Boolean,
      // TODO: This used to live in generated config - move back?
      transparentBackground: Boolean,
      clickTime: Millis,
      disableContextMenu: Boolean
  ):

    def withFrameRatePolicy(value: FrameRatePolicy): Settings =
      this.copy(frameRatePolicy = value)
    def unlimitedFrameRate: Settings =
      withFrameRatePolicy(FrameRatePolicy.Unlimited)
    def targetFrameRate(target: FPS): Settings =
      withFrameRatePolicy(FrameRatePolicy.Skip(target))

    def withAntiAliasing(enabled: Boolean): Settings =
      this.copy(antiAliasing = enabled)
    def useAntiAliasing: Settings =
      withAntiAliasing(true)
    def noAntiAliasing: Settings =
      withAntiAliasing(false)

    def withPremultipliedAlpha(enabled: Boolean): Settings =
      this.copy(premultipliedAlpha = enabled)
    def usePremultipliedAlpha: Settings =
      withPremultipliedAlpha(true)
    def noPremultipliedAlpha: Settings =
      withPremultipliedAlpha(false)

    def withTransparentBackground(enabled: Boolean): Settings =
      this.copy(transparentBackground = enabled)
    def useTransparentBackground: Settings =
      withTransparentBackground(true)
    def noTransparentBackground: Settings =
      withTransparentBackground(false)

    def withClickTime(millis: Millis): Settings =
      this.copy(clickTime = millis)

    def withDisableContextMenu(disabled: Boolean): Settings =
      this.copy(disableContextMenu = disabled)
    def noContextMenu: Settings =
      withDisableContextMenu(true)
    def allowContextMenu: Settings =
      withDisableContextMenu(false)

  object Settings:

    val default: Settings =
      Settings(
        FrameRatePolicy.Skip(FPS.`60`),
        antiAliasing = false,
        premultipliedAlpha = true,
        transparentBackground = true,
        clickTime = Millis(250),
        disableContextMenu = true
      )
