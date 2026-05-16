package indigo.internal

import indigo.FrameRatePolicy
import indigo.FullScreenEvent
import indigo.Game
import indigo.core.assets.AssetName
import indigo.core.assets.AssetPath
import indigo.core.assets.AssetType
import indigo.core.datatypes.BindingKey
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Vector2
import indigo.core.events.AssetEvent
import indigo.core.events.ScreenCaptureEvent
import indigo.core.render.ScreenCaptureConfig
import indigo.core.time.FPS
import indigo.internal.assets.AssetLoader
import indigo.internal.models.FullScreenRequest
import indigo.internal.models.TickUpdateResult
import indigo.shared.IndigoSystemEvent
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.*

import scala.annotation.nowarn
import scala.util.Failure
import scala.util.Success

object Utils:

  // TODO: Make sure we don't lose this.
  // TODO: Do we need to backport to the native version?
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

  // Running as a cheeky Future, might be worth revisiting sometime...
  def runFullScreen(canvas: html.Canvas, game: Game[?, ?, ?], request: FullScreenRequest): Unit =
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

  def runLoadAssets(
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
  def runCaptureScreen(
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
