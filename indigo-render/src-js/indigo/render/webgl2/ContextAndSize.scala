package indigo.render.webgl2

import indigoengine.webgl2.facades.WebGL2RenderingContext

final case class ContextAndSize(
    context: WebGL2RenderingContext,
    width: Int,
    height: Int
)
