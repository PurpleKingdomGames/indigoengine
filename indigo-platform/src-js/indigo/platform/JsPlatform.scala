package indigo.platform

import indigo.core.Outcome
import indigo.core.assets.AssetName
import indigo.core.config.GameConfig
import indigo.core.datatypes.Vector2
import indigo.core.events.FullScreenEnterError
import indigo.core.events.FullScreenEntered
import indigo.core.events.FullScreenExitError
import indigo.core.events.FullScreenExited
import indigo.core.events.GlobalEvent
import indigo.core.utils.IndigoLogger
import indigo.platform.AssetMapping
import indigo.platform.AtlasId
import indigo.platform.Platform
import indigo.platform.PlatformFullScreen
import indigo.platform.TextureRefAndOffset
import indigo.platform.assets.AssetCollection
import indigo.platform.assets.ImageRef
import indigo.platform.assets.TextureAtlas
import indigo.platform.assets.TextureAtlasFunctions
import indigo.platform.events.GlobalEventStream
import indigo.platform.events.WorldEvents
import indigo.platform.input.GamepadInputCaptureImpl
import indigo.platform.renderer.Renderer
import indigo.platform.renderer.RendererConfig
import indigo.platform.renderer.RendererInitialiser
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.shaders.RawShaderCode
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.KVP
import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.html.Canvas
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

import scala.annotation.nowarn
import scala.util.Failure
import scala.util.Success

class JsPlatform(
    parentElement: Element,
    gameConfig: GameConfig,
    val globalEventStream: GlobalEventStream
) extends Platform
    with PlatformFullScreen {

  val rendererInit: RendererInitialiser =
    new RendererInitialiser(gameConfig.advanced.renderingTechnology, globalEventStream)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  private var _canvas: Canvas = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _running: Boolean         = true
  private val _worldEvents: WorldEvents = new WorldEvents

  def initialise(
      firstRun: Boolean,
      shaders: Set[RawShaderCode],
      assetCollection: AssetCollection
  ): Outcome[(Renderer, AssetMapping)] =
    for {
      textureAtlas        <- createTextureAtlas(assetCollection)
      loadedTextureAssets <- extractLoadedTextures(textureAtlas)
      assetMapping        <- setupAssetMapping(textureAtlas)
      canvas              <- createCanvas(firstRun, parentElement, gameConfig)
      _                   <- listenToWorldEvents(firstRun, canvas, gameConfig, globalEventStream)
      renderer            <- startRenderer(gameConfig, loadedTextureAssets, canvas, shaders)
      _ = _canvas = canvas
    } yield (renderer, assetMapping)

  @nowarn("msg=discarded")
  def tick(loop: Double => Unit): Unit =
    if _running then dom.window.requestAnimationFrame(loop)
    ()

  @nowarn("msg=discarded")
  def delay(amount: Double, thunk: () => Unit): Unit =
    dom.window.setTimeout(thunk, amount)

  def kill(): Unit =
    _running = false
    _worldEvents.kill()
    GamepadInputCaptureImpl.kill()
    ()

  def pushGlobalEvent(event: GlobalEvent): Unit =
    globalEventStream.pushGlobalEvent(event)

  def collectEvents: Batch[GlobalEvent] =
    globalEventStream.collect

  def createTextureAtlas(assetCollection: AssetCollection): Outcome[TextureAtlas] =
    Outcome(
      TextureAtlas.create(
        assetCollection.images.map(i => ImageRef(i.name, i.data.width, i.data.height, i.tag)).toList,
        (name: AssetName) => assetCollection.images.find(_.name == name),
        TextureAtlasFunctions.createAtlasData
      )
    )

  def extractLoadedTextures(textureAtlas: TextureAtlas): Outcome[List[LoadedTextureAsset]] =
    Outcome(
      textureAtlas.atlases.toList
        .map { case (atlasId, atlas) => atlas.imageData.map(data => new LoadedTextureAsset(AtlasId(atlasId), data)) }
        .collect { case Some(s) => s }
    )

  def setupAssetMapping(textureAtlas: TextureAtlas): Outcome[AssetMapping] =
    Outcome(
      new AssetMapping(
        mappings = KVP.from(
          textureAtlas.legend
            .map { case (name, atlasIndex) =>
              name -> TextureRefAndOffset(
                atlasName = atlasIndex.id,
                atlasSize = textureAtlas.atlases
                  .get(atlasIndex.id.toString)
                  .map(_.size.value)
                  .map(i => Vector2(i.toDouble))
                  .getOrElse(Vector2.one),
                offset = atlasIndex.offset.toVector,
                size = atlasIndex.size.toVector
              )
            }
        )
      )
    )

  def createCanvas(firstRun: Boolean, parentElement: Element, gameConfig: GameConfig): Outcome[Canvas] =
    if firstRun then
      Outcome(
        rendererInit.createCanvas(
          gameConfig.viewport.width,
          gameConfig.viewport.height,
          parentElement
        )
      )
    else Outcome(_canvas)

  def listenToWorldEvents(
      firstRun: Boolean,
      canvas: Canvas,
      gameConfig: GameConfig,
      globalEventStream: GlobalEventStream
  ): Outcome[Unit] =
    Outcome {
      if firstRun then
        IndigoLogger.info("Starting world events")
        _worldEvents.init(
          canvas,
          gameConfig.resizePolicy,
          gameConfig.magnification,
          gameConfig.advanced.disableContextMenu,
          globalEventStream,
          gameConfig.advanced.clickTime.toLong
        )
        GamepadInputCaptureImpl.init()
      else IndigoLogger.info("Re-using existing world events")
    }

  def startRenderer(
      gameConfig: GameConfig,
      loadedTextureAssets: List[LoadedTextureAsset],
      canvas: Canvas,
      shaders: Set[RawShaderCode]
  ): Outcome[Renderer] =
    Outcome {
      IndigoLogger.info("Starting renderer")
      rendererInit.setup(
        new RendererConfig(
          renderingTechnology = gameConfig.advanced.renderingTechnology,
          clearColor = gameConfig.clearColor,
          magnification = gameConfig.magnification,
          maxBatchSize = gameConfig.advanced.batchSize,
          antiAliasing = gameConfig.advanced.antiAliasing,
          premultipliedAlpha = gameConfig.advanced.premultipliedAlpha,
          transparentBackground = gameConfig.transparentBackground
        ),
        loadedTextureAssets,
        canvas,
        shaders
      )
    }

  def toggleFullScreen(): Unit =
    if (Option(dom.document.fullscreenElement).isEmpty)
      enterFullScreen()
    else
      exitFullScreen()

  def enterFullScreen(): Unit =
    _canvas.requestFullscreen().toFuture.onComplete {
      case Success(()) =>
        globalEventStream.pushGlobalEvent(FullScreenEntered)

      case Failure(_) =>
        globalEventStream.pushGlobalEvent(FullScreenEnterError)
    }

  def exitFullScreen(): Unit =
    dom.document.exitFullscreen().toFuture.onComplete {
      case Success(()) =>
        globalEventStream.pushGlobalEvent(FullScreenExited)

      case Failure(_) =>
        globalEventStream.pushGlobalEvent(FullScreenExitError)
    }
}
