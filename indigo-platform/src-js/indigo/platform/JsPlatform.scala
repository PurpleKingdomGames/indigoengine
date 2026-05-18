package indigo.platform

import indigo.core.Outcome
import indigo.core.assets.AssetName
import indigo.core.config.EngineConfig
import indigo.core.datatypes.Vector2
import indigo.core.events.GlobalEvent
import indigo.core.utils.IndigoLogger
import indigo.platform.assets.AssetCollection
import indigo.platform.assets.ImageRef
import indigo.platform.assets.TextureAtlas
import indigo.platform.assets.TextureAtlasFunctions
import indigo.platform.events.GlobalEventStream
import indigo.platform.imaging.ImageService
import indigo.render.Renderer
import indigo.render.RendererInitialiser
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.AtlasId
import indigo.render.pipeline.assets.TextureRefAndOffset
import indigo.render.webgl2.ContextAndSize
import indigo.render.webgl2.LoadedTextureAsset
import indigo.shaders.RawShaderCode
import indigoengine.shared.collections.Batch
import org.scalajs.dom.ImageData
import org.scalajs.dom.html

class JsPlatform(
    engineConfig: EngineConfig,
    val globalEventStream: GlobalEventStream,
    imageService: ImageService[html.Image, ImageData]
) extends Platform[ContextAndSize] {

  val rendererInit: RendererInitialiser =
    new RendererInitialiser()

  def initialise(
      context: ContextAndSize,
      shaders: Set[RawShaderCode],
      assetCollection: AssetCollection
  ): Outcome[(Renderer[ContextAndSize], AssetMapping)] =
    for {
      textureAtlas        <- createTextureAtlas(assetCollection)
      loadedTextureAssets <- extractLoadedTextures(textureAtlas)
      assetMapping        <- setupAssetMapping(textureAtlas)
      renderer            <- startRenderer(context, engineConfig, loadedTextureAssets, shaders)
    } yield (renderer, assetMapping)

  def kill(): Unit =
    ()

  def pushGlobalEvent(event: GlobalEvent): Unit =
    globalEventStream.pushGlobalEvent(event)

  def registerEventCallback(cb: GlobalEvent => Unit): Unit =
    globalEventStream.registerEventCallback(cb)

  def clearEventCallback(): Unit =
    globalEventStream.clearEventCallback()

  def collectEvents: Batch[GlobalEvent] =
    globalEventStream.collect

  def createTextureAtlas(assetCollection: AssetCollection): Outcome[TextureAtlas] =
    Outcome(
      TextureAtlas.create(
        assetCollection.images.map(i => ImageRef(i.name, i.data.width, i.data.height, i.tag)).toList,
        (name: AssetName) => assetCollection.images.find(_.name == name),
        TextureAtlasFunctions.createAtlasData(imageService)
      )
    )

  def extractLoadedTextures(textureAtlas: TextureAtlas): Outcome[Batch[LoadedTextureAsset]] =
    Outcome(
      textureAtlas.atlases.toBatch
        .map { case (atlasId, atlas) => atlas.imageData.map(data => new LoadedTextureAsset(AtlasId(atlasId), data)) }
        .collect { case Some(s) => s }
    )

  def setupAssetMapping(textureAtlas: TextureAtlas): Outcome[AssetMapping] =
    Outcome(
      new AssetMapping(
        mappings = textureAtlas.legend
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

  def startRenderer(
      context: ContextAndSize,
      engineConfig: EngineConfig,
      loadedTextureAssets: Batch[LoadedTextureAsset],
      shaders: Set[RawShaderCode]
  ): Outcome[Renderer[ContextAndSize]] =
    Outcome {
      IndigoLogger.info("Starting renderer")
      rendererInit.setup(
        engineConfig,
        loadedTextureAssets,
        context,
        shaders
      )
    }
}
