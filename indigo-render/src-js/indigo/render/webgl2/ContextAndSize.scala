package indigo.render.webgl2

import indigoengine.webgl2.facades.WebGL2RenderingContext

// TODO: Parameterise and merge with the native version?
final case class ContextAndSize(
    context: WebGL2RenderingContext,
    width: Int,
    height: Int
)
