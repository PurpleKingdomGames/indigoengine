package indigo.render

import indigo.core.config.EngineConfig
import indigo.render.Renderer
import indigo.render.facades.gl.GL.*
import indigo.render.facades.gl.GLConstants.*
import indigo.render.facades.sdl.SDL.*
import indigo.render.facades.sdl.SDLConstants.*
import indigo.render.opengl.LoadedTextureAsset
import indigo.render.opengl.OpenGLRenderer
import indigo.shaders.RawShaderCode
import indigoengine.shared.collections.Batch

import scala.annotation.nowarn
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

// TODO: Should be like the JS version.
// final class RendererInitialiser():

//   def setup(
//       config: EngineConfig,
//       loadedTextureAssets: Batch[LoadedTextureAsset],
//       context: WebGL2RenderingContext,
//       width: Int,
//       height: Int,
//       shaders: Set[RawShaderCode]
//   ): Renderer =
//     val cNc = new ContextAndSize(context, width, height)

//     val r =
//       new RendererWebGL2(config, loadedTextureAssets.toJSArray, cNc)

//     r.init(shaders)
//     r.resize(width, height)
//     r

@SuppressWarnings(
  Array(
    "scalafix:DisableSyntax.throw",
    "scalafix:DisableSyntax.while",
    "scalafix:DisableSyntax.var",
    "scalafix:DisableSyntax.null"
  )
)
final class RendererInitialiser(
    // globalEventStream: EmitGlobalEvent
):

  val event   = stackalloc[facades.sdl.SDL.SDL_Event]()
  var running = true

  @nowarn("msg=unused")
  def setup(
      config: EngineConfig,
      loadedTextureAssets: Batch[LoadedTextureAsset],
      context: String,
      width: Int,
      height: Int,
      shaders: Set[RawShaderCode]
  ): Renderer =
    // val cNc = setupContextAndCanvas(
    //   canvas,
    //   config.magnification,
    //   config.antiAliasing,
    //   config.premultipliedAlpha,
    //   config.transparentBackground
    // )

    val ctx = setupWindowAndContext()

    val r =
      new OpenGLRenderer(config, ctx /*, globalEventStream*/ )
    //   new RendererWebGL2(config, loadedTextureAssets.toJSArray, cNc, globalEventStream)

    r.init(shaders)
    r

  @nowarn("msg=unused")
  def setupWindowAndContext(): SDL_GLContext =
    if !SDL_Init(SDL_INIT_VIDEO) then
      val err = fromCString(SDL_GetError())
      throw new RuntimeException(s"SDL_Init failed: $err")

    // OpenGL 4.1 Core Profile — highest version macOS supports.
    // Forward-compatible flag is required by macOS to get a Core Profile context.
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 4)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 1)
    SDL_GL_SetAttribute(
      SDL_GL_CONTEXT_PROFILE_MASK,
      SDL_GL_CONTEXT_PROFILE_CORE
    )
    SDL_GL_SetAttribute(
      SDL_GL_CONTEXT_FLAGS,
      SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG
    )
    SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1)

    val window = SDL_CreateWindow(
      c"Scala Native + SDL3 + OpenGL Demo",
      400,
      400,
      SDL_WINDOW_OPENGL
    )
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

    // TODO: Needs to move

    glViewport(0, 0, 400, 400)
    glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

    val program = Shaders.createProgram(Shaders.vertSrc, Shaders.fragSrc)
    val vao     = makeVao()

    // Initial clear+swap to ensure the Metal drawable is ready before first draw
    glClear(GL_COLOR_BUFFER_BIT)
    SDL_GL_SwapWindow(window)

    // TODO: Can't do this here..
    while running do
      while SDL_PollEvent(event) != 0 do
        val eventType = event.asInstanceOf[Ptr[CStruct1[UInt]]]._1
        if eventType == SDL_EVENT_QUIT then running = false

      SDL_GL_MakeCurrent(window, glCtx)
      glClear(GL_COLOR_BUFFER_BIT)
      glUseProgram(program)
      glBindVertexArray(vao)
      glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

      SDL_GL_SwapWindow(window)
      SDL_Delay(16.toUInt)

    SDL_GL_DestroyContext(glCtx)
    SDL_DestroyWindow(window)
    SDL_Quit()

    glCtx

  def makeVao(): UInt =
    val vaoPtr = stackalloc[UInt]()
    glGenVertexArrays(1, vaoPtr)

    val vaoId = !vaoPtr
    glBindVertexArray(vaoId)

    vaoId
