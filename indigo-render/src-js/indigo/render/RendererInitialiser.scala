package indigo.render

import indigo.core.config.EngineConfig
import indigo.render.Renderer
import indigo.render.facades.WebGL2RenderingContext
import indigo.render.webgl2.ContextAndSize
import indigo.render.webgl2.LoadedTextureAsset
import indigo.render.webgl2.RendererWebGL2
import indigo.shaders.RawShaderCode

import scala.scalajs.js.JSConverters.*

final class RendererInitialiser() {

  def setup(
      config: EngineConfig,
      loadedTextureAssets: List[LoadedTextureAsset],
      context: WebGL2RenderingContext,
      width: Int,
      height: Int,
      shaders: Set[RawShaderCode]
  ): Renderer = {
    val cNc = new ContextAndSize(context, width, height)

    val r =
      new RendererWebGL2(config, loadedTextureAssets.toJSArray, cNc)

    r.init(shaders)
    r.resize(width, height)
    r
  }

}
