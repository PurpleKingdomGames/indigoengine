package tyrian.sdl

import scala.scalanative.unsafe.*

import tyrian.sdl.facades.gl.GL.*
import tyrian.sdl.facades.sdl.SDL.*
import tyrian.sdl.facades.sdl.SDLConstants.*

final class SdlContext private (
    val window: SDL_Window,
    val glCtx: SDL_GLContext,
    val width: Int,
    val height: Int
):

  def destroy(): Unit =
    val _ = SDL_GL_DestroyContext(glCtx)
    SDL_DestroyWindow(window)
    SDL_Quit()

object SdlContext:

  @SuppressWarnings(
    Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.null")
  )
  def create(title: String, width: Int, height: Int): SdlContext =
    if !SDL_Init(SDL_INIT_VIDEO) then
      val err = fromCString(SDL_GetError())
      throw new RuntimeException(s"SDL_Init failed: $err")

    // OpenGL 4.1 Core Profile — highest version macOS supports.
    // Forward-compatible flag is required by macOS to get a Core Profile context.
    val _a: CInt = SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 4)
    val _b: CInt = SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 1)
    val _c: CInt = SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE)
    val _d: CInt = SDL_GL_SetAttribute(SDL_GL_CONTEXT_FLAGS, SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG)
    val _e: CInt = SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1)
    val _      = (_a, _b, _c, _d, _e)

    val window =
      Zone { (z: Zone) ?=>
        SDL_CreateWindow(toCString(title)(using z), width, height, SDL_WINDOW_OPENGL)
      }

    if window == null then
      val err = fromCString(SDL_GetError())
      SDL_Quit()
      throw new RuntimeException(s"SDL_CreateWindow failed: $err")

    val glCtx = SDL_GL_CreateContext(window)
    if glCtx == null then
      val err = fromCString(SDL_GetError())
      SDL_DestroyWindow(window)
      SDL_Quit()
      throw new RuntimeException(s"SDL_GL_CreateContext failed: $err")

    glViewport(0, 0, width, height)
    glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

    new SdlContext(window, glCtx, width, height)
