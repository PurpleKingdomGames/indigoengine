package tyrian

import indigoengine.webgl2.facades.WebGL2RenderingContext

final case class WebGL2Context(
    ctx: WebGL2RenderingContext,
    width: Int,
    height: Int
) //:

// def destroy(): Unit =
// val _ = SDL_GL_DestroyContext(glCtx)
// SDL_DestroyWindow(window)
// SDL_Quit()

// object SDLContext:

//   @SuppressWarnings(
//     Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.null")
//   )
//   def create(title: String, width: Int, height: Int): SDLContext =
//     if !SDL_Init(SDL_INIT_VIDEO) then
//       val err = fromCString(SDL_GetError())
//       throw new RuntimeException(s"SDL_Init failed: $err")

//     // OpenGL 4.1 Core Profile — highest version macOS supports.
//     // Forward-compatible flag is required by macOS to get a Core Profile context.
//     val _a: CInt = SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 4)
//     val _b: CInt = SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 1)
//     val _c: CInt = SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE)
//     val _d: CInt = SDL_GL_SetAttribute(SDL_GL_CONTEXT_FLAGS, SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG)
//     val _e: CInt = SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1)
//     val _        = (_a, _b, _c, _d, _e)

//     Zone { (z: Zone) ?=>
//       val _1 = SDL_SetHint(toCString(SDL_HINT_WINDOW_ACTIVATE_WHEN_SHOWN)(using z), toCString("1")(using z))
//       val _2 = SDL_SetHint(toCString(SDL_HINT_WINDOW_ACTIVATE_WHEN_RAISED)(using z), toCString("1")(using z))
//       val _  = (_1, _2)
//     }

//     val window =
//       Zone { (z: Zone) ?=>
//         SDL_CreateWindow(toCString(title)(using z), width, height, SDL_WINDOW_OPENGL)
//       }

//     if window == null then
//       val err = fromCString(SDL_GetError())
//       SDL_Quit()
//       throw new RuntimeException(s"SDL_CreateWindow failed: $err")

//     val glCtx = SDL_GL_CreateContext(window)
//     if glCtx == null then
//       val err = fromCString(SDL_GetError())

//       SDL_DestroyWindow(window)
//       SDL_Quit()

//       throw new RuntimeException(s"SDL_GL_CreateContext failed: $err")

//     glViewport(0, 0, width, height)
//     glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

//     val _ = SDL_RaiseWindow(window)

//     new SDLContext(window, glCtx, width, height)
