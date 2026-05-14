package indigoengine.sdl.facades.sdl

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

object SDLConstants:

  // SDL_Init flags
  val SDL_INIT_VIDEO: UInt = 0x00000020.toUInt

  val SDL_WINDOW_OPENGL: UInt = 0x00000002.toUInt

  // SDL_GL_SetAttribute
  val SDL_GL_DOUBLEBUFFER: CInt                    = 5
  val SDL_GL_CONTEXT_MAJOR_VERSION: CInt           = 17
  val SDL_GL_CONTEXT_MINOR_VERSION: CInt           = 18
  val SDL_GL_CONTEXT_FLAGS: CInt                   = 19
  val SDL_GL_CONTEXT_PROFILE_MASK: CInt            = 20
  val SDL_GL_CONTEXT_PROFILE_CORE: CInt            = 0x0001
  val SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG: CInt = 0x0002

  // Hint names
  val SDL_HINT_WINDOW_ACTIVATE_WHEN_SHOWN: String  = "SDL_WINDOW_ACTIVATE_WHEN_SHOWN"
  val SDL_HINT_WINDOW_ACTIVATE_WHEN_RAISED: String = "SDL_WINDOW_ACTIVATE_WHEN_RAISED"
