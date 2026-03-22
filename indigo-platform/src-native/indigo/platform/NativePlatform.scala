package indigo.platform

import indigo.core.Outcome
import indigo.core.config.GameConfig
import indigo.core.events.GlobalEvent
import indigo.core.utils.IndigoLogger
import indigo.platform.assets.AssetCollection
import indigo.platform.events.GlobalEventStream
import indigo.render.Renderer
import indigo.render.RendererConfig
import indigo.render.RendererInitialiser
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.TextureRefAndOffset
import indigo.shaders.RawShaderCode
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.KVP

import scala.annotation.nowarn

/** No-op Native Platform implementation stub.
  *
  * This is a placeholder implementation to validate that the platform abstraction architecture compiles against native
  * platforms. All methods are no-ops and this should not be used for actual game execution.
  */
@nowarn // TODO: Refine/Remove once the platform is running in native.
class NativePlatform(
    gameConfig: GameConfig,
    val globalEventStream: GlobalEventStream
) extends Platform
    with PlatformFullScreen:

  val rendererInit: RendererInitialiser =
    new RendererInitialiser(globalEventStream)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _running: Boolean = true

  def initialise(
      firstRun: Boolean,
      shaders: Set[RawShaderCode],
      assetCollection: AssetCollection
  ): Outcome[(Renderer, AssetMapping)] =
    val stubMapping = AssetMapping(KVP.empty[TextureRefAndOffset])

    startRenderer(gameConfig, shaders).map(r => r -> stubMapping)

  def startRenderer(
      gameConfig: GameConfig,
      // loadedTextureAssets: List[LoadedTextureAsset],
      // canvas: Canvas,
      shaders: Set[RawShaderCode]
  ): Outcome[Renderer] =
    Outcome {
      IndigoLogger.info("Starting renderer")
      rendererInit.setup(
        new RendererConfig(
          clearColor = gameConfig.clearColor,
          magnification = gameConfig.magnification,
          maxBatchSize = gameConfig.advanced.batchSize,
          antiAliasing = gameConfig.advanced.antiAliasing,
          premultipliedAlpha = gameConfig.advanced.premultipliedAlpha,
          transparentBackground = gameConfig.transparentBackground
        ),
        // loadedTextureAssets,
        // canvas,
        shaders
      )
    }

  def tick(loop: Double => Unit): Unit = ()

  def delay(amount: Double, thunk: () => Unit): Unit = ()

  def kill(): Unit =
    _running = false
    // _worldEvents.kill()
    // GamepadInputCaptureImpl.kill()
    ()

  def pushGlobalEvent(event: GlobalEvent): Unit =
    globalEventStream.pushGlobalEvent(event)

  def collectEvents: Batch[GlobalEvent] =
    globalEventStream.collect

  def toggleFullScreen(): Unit = ()

  def enterFullScreen(): Unit = ()

  def exitFullScreen(): Unit = ()
