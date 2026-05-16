package indigo.render

import indigo.core.config.EngineConfig
import indigo.render.Renderer
import indigo.render.webgl2.ContextAndSize
import indigo.render.webgl2.LoadedTextureAsset
import indigo.render.webgl2.RendererWebGL2
import indigo.shaders.RawShaderCode
import indigoengine.shared.collections.Batch

final class RendererInitialiser():

  def setup(
      config: EngineConfig,
      loadedTextureAssets: Batch[LoadedTextureAsset],
      context: ContextAndSize,
      shaders: Set[RawShaderCode]
  ): Renderer[ContextAndSize] =
    val r =
      new RendererWebGL2(config, loadedTextureAssets.toJSArray)

    r.init(context, shaders)
    r.resize(context)
    r
