package demo

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import facades.sdl.SDL.*
import facades.sdl.SDLConstants.*
import facades.gl.GL.*
import facades.gl.GLConstants.*

object Main:

  def makeVao(): UInt =
    val vaoPtr = stackalloc[UInt]()
    glGenVertexArrays(1, vaoPtr)

    val vaoId = !vaoPtr
    glBindVertexArray(vaoId)

    vaoId

  def main(args: Array[String]): Unit =
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

    glViewport(0, 0, 400, 400)
    glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

    val program = Shaders.createProgram(Shaders.vertSrc, Shaders.fragSrc)
    val vao     = makeVao()

    // Initial clear+swap to ensure the Metal drawable is ready before first draw
    glClear(GL_COLOR_BUFFER_BIT)
    SDL_GL_SwapWindow(window)

    val event   = stackalloc[facades.sdl.SDL.SDL_Event]()
    var running = true

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
