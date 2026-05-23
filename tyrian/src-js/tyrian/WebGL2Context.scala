package tyrian

import indigoengine.webgl2.facades.WebGL2RenderingContext

final case class WebGL2Context(
    ctx: WebGL2RenderingContext,
    width: Int,
    height: Int
)
