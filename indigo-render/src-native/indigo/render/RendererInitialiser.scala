package indigo.render

// import indigo.core.utils.IndigoLogger
import indigo.render.EmitGlobalEvent
import indigo.render.Renderer
import indigo.render.RendererConfig
import indigo.render.facades.gl.GL.*
import indigo.render.facades.gl.GLConstants.*
import indigo.render.facades.sdl.SDL.*
import indigo.render.facades.sdl.SDLConstants.*
import indigo.render.opengl.OpenGLRenderer
import indigo.shaders.RawShaderCode

import scala.annotation.nowarn
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@SuppressWarnings(
  Array(
    "scalafix:DisableSyntax.throw",
    "scalafix:DisableSyntax.while",
    "scalafix:DisableSyntax.var",
    "scalafix:DisableSyntax.null"
  )
)
final class RendererInitialiser(
    globalEventStream: EmitGlobalEvent
):

  val event   = stackalloc[facades.sdl.SDL.SDL_Event]()
  var running = true

  def setup(
      config: RendererConfig,
      // loadedTextureAssets: List[LoadedTextureAsset],
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
      new OpenGLRenderer(config, ctx, globalEventStream)
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

  // def createCanvas(width: Int, height: Int, parentElement: Element): html.Canvas =
  //   val defaultName = "indigo-container"

  //   val parentElementId =
  //     Option(parentElement.id)
  //       .map(id => if id.isEmpty then defaultName else id)
  //       .getOrElse(defaultName)

  //   val name = s"$parentElementId-canvas"

  //   createNamedCanvas(width, height, name, Some(parentElement))

  // @SuppressWarnings(
  //   Array(
  //     "scalafix:DisableSyntax.null",
  //     "scalafix:DisableSyntax.var"
  //   )
  // )
  // @nowarn("msg=unused")
  // def createNamedCanvas(width: Int, height: Int, name: String, appendToParent: Option[Element]): html.Canvas = {
  //   var canvas: html.Canvas = dom.document.getElementById(name).asInstanceOf[html.Canvas]

  //   if (canvas == null) {
  //     canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]

  //     appendToParent match
  //       case Some(parent) =>
  //         parent.appendChild(canvas)

  //       case None =>
  //         ()

  //     canvas.id = name
  //     canvas.width = width
  //     canvas.height = height
  //   }

  //   canvas
  // }

  // private def setupContextAndCanvas(
  //     canvas: html.Canvas,
  //     magnification: Int,
  //     antiAliasing: Boolean,
  //     premultipliedAlpha: Boolean,
  //     transparentBackground: Boolean
  // ): ContextAndCanvas = {
  //   val ctx = getContext(canvas, antiAliasing, premultipliedAlpha, transparentBackground)

  //   val cNc =
  //     new ContextAndCanvas(
  //       context = ctx,
  //       canvas = canvas,
  //       magnification = magnification
  //     )

  //   cNc
  // }

  // private def getContext(
  //     canvas: html.Canvas,
  //     antiAliasing: Boolean,
  //     premultipliedAlpha: Boolean,
  //     transparentBackground: Boolean
  // ): WebGLRenderingContext = {
  //   val args =
  //     Dynamic.literal(
  //       "premultipliedAlpha" -> premultipliedAlpha,
  //       "alpha"              -> transparentBackground,
  //       "antialias"          -> antiAliasing
  //     )

  //   ensureRenderingTechnologyAvailable(args)

  //   canvas.getContext("webgl2", args).asInstanceOf[WebGLRenderingContext]
  // }

  // @SuppressWarnings(
  //   Array(
  //     "scalafix:DisableSyntax.null",
  //     "scalafix:DisableSyntax.throw"
  //   )
  // )
  // private def ensureRenderingTechnologyAvailable(
  //     args: Dynamic
  // ): Unit =
  //   /* This test relies on a temporary canvas not attached to the document.
  //    */
  //   val tempCanvas = createNamedCanvas(1, 1, "indigowebgl2test", None)

  //   val gl2 = tempCanvas.getContext("webgl2", args).asInstanceOf[WebGLRenderingContext]

  //   if (gl2 == null)
  //     throw new Exception("WebGL 2.0 required by indigo game. This browser does not appear to support WebGL 2.0.")
  //   else if (!isWebGL2ReallySupported(gl2))
  //     throw new Exception(
  //       "WebGL 2.0 required by indigo game. This browser claims to support WebGL 2.0, but does not meet indigo's requirements."
  //     )
  //   else {
  //     IndigoLogger.info("Using WebGL 2.0")
  //     ()
  //   }

  // @SuppressWarnings(
  //   Array(
  //     "scalafix:DisableSyntax.null"
  //   )
  // )
  // private def isWebGL2ReallySupported(gl2: WebGLRenderingContext): Boolean = {
  //   IndigoLogger.info("Checking WebGL 2.0 availability...")

  //   def testWebGL2Compatibility(param: Int, min: Int, name: String): Boolean =
  //     try {
  //       val value = gl2.getParameter(param).asInstanceOf[Int]
  //       if (!value.toFloat.isNaN() && value >= min) true
  //       else {
  //         IndigoLogger.info(
  //           s" - WebGL 2.0 check '$name' failed. [min: ${min.toString}] [actual: ${value.toFloat.toString}]"
  //         )
  //         false
  //       }
  //     } catch {
  //       case _: Throwable => false
  //     }

  //   val tests = List(
  //     (WebGL2RenderingContext.MAX_3D_TEXTURE_SIZE, 256, "MAX_3D_TEXTURE_SIZE"),
  //     (WebGL2RenderingContext.MAX_DRAW_BUFFERS, 4, "MAX_DRAW_BUFFERS"),
  //     (WebGL2RenderingContext.MAX_COLOR_ATTACHMENTS, 4, "MAX_COLOR_ATTACHMENTS"),
  //     (WebGL2RenderingContext.MAX_VERTEX_UNIFORM_BLOCKS, 12, "MAX_VERTEX_UNIFORM_BLOCKS"),
  //     (WebGL2RenderingContext.MAX_VERTEX_TEXTURE_IMAGE_UNITS, 16, "MAX_VERTEX_TEXTURE_IMAGE_UNITS"),
  //     (WebGL2RenderingContext.MAX_FRAGMENT_INPUT_COMPONENTS, 60, "MAX_FRAGMENT_INPUT_COMPONENTS"),
  //     (WebGL2RenderingContext.MAX_UNIFORM_BUFFER_BINDINGS, 24, "MAX_UNIFORM_BUFFER_BINDINGS"),
  //     (WebGL2RenderingContext.MAX_COMBINED_UNIFORM_BLOCKS, 24, "MAX_COMBINED_UNIFORM_BLOCKS"),
  //     (WebGL2RenderingContext.MAX_VARYING_VECTORS, 15, "MAX_VARYING_VECTORS")
  //   )

  //   gl2 != null && tests.forall(t => testWebGL2Compatibility(t._1, t._2, t._3))
  // }
