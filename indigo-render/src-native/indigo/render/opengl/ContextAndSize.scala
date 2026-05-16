package indigo.render.opengl

import indigoengine.sdl.facades.sdl.SDL.SDL_GLContext

// import org.scalajs.dom.WebGLRenderingContext

final case class ContextAndSize(
    context: SDL_GLContext,
    width: Int,
    height: Int
)
