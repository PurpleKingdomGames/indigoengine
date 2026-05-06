package indigo.render

import indigo.core.config.EngineConfig
import indigo.render.Renderer
import indigo.render.facades.WebGL2RenderingContext
import indigo.render.webgl2.ContextAndCanvas
import indigo.render.webgl2.LoadedTextureAsset
import indigo.render.webgl2.RendererWebGL2
import indigo.shaders.RawShaderCode
import org.scalajs.dom.html

import scala.scalajs.js.JSConverters.*

final class RendererInitialiser() {

  def setup(
      config: EngineConfig,
      loadedTextureAssets: List[LoadedTextureAsset],
      canvas: html.Canvas,
      context: WebGL2RenderingContext,
      shaders: Set[RawShaderCode]
  ): Renderer = {
    val cNc = new ContextAndCanvas(
      context,
      canvas
    )

    val r =
      new RendererWebGL2(config, loadedTextureAssets.toJSArray, cNc)

    r.init(shaders)
    r
  }

}
