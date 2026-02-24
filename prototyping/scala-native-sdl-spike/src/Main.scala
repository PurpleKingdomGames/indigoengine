import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import sdl.SDL.*
import sdl.SDLConstants.*
import gl.GL.*
import gl.GLConstants.*

object Main:
  def main(args: Array[String]): Unit =
    // Initialize SDL with video subsystem
    if !SDL_Init(SDL_INIT_VIDEO) then
      val err = fromCString(SDL_GetError())
      throw new RuntimeException(s"SDL_Init failed: $err")

    // Request OpenGL 2.1 compatibility context with double buffering
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 2)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 1)
    SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1)

    // Create a 400x400 window
    val window = SDL_CreateWindow(
      c"Hello Triangle - Scala Native",
      400,
      400,
      SDL_WINDOW_OPENGL
    )
    if window == null then
      val err = fromCString(SDL_GetError())
      SDL_Quit()
      throw new RuntimeException(s"SDL_CreateWindow failed: $err")

    // Create OpenGL context
    val glCtx = SDL_GL_CreateContext(window)
    if glCtx == null then
      val err = fromCString(SDL_GetError())
      SDL_DestroyWindow(window)
      SDL_Quit()
      throw new RuntimeException(s"SDL_GL_CreateContext failed: $err")

    // Set up viewport and clear color
    glViewport(0, 0, 400, 400)
    glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

    // Event loop
    val event = stackalloc[sdl.SDL.SDL_Event]()
    var running = true

    while running do
      // Poll all pending events
      while SDL_PollEvent(event) != 0 do
        // First 4 bytes of SDL_Event is the event type (Uint32)
        val eventType = !(event.asInstanceOf[Ptr[UInt]])
        if eventType == SDL_EVENT_QUIT then running = false

      // Clear the screen
      glClear(GL_COLOR_BUFFER_BIT)

      // Draw a triangle with colored vertices (legacy immediate mode)
      glBegin(GL_TRIANGLES)
      glColor3f(1.0f, 0.0f, 0.0f) // red
      glVertex2f(0.0f, 0.6f) // top
      glColor3f(0.0f, 1.0f, 0.0f) // green
      glVertex2f(-0.6f, -0.4f) // bottom-left
      glColor3f(0.0f, 0.0f, 1.0f) // blue
      glVertex2f(0.6f, -0.4f) // bottom-right
      glEnd()

      glFlush()
      SDL_GL_SwapWindow(window)

      // Cap at ~60 fps
      SDL_Delay(16.toUInt)

    // Cleanup
    SDL_GL_DestroyContext(glCtx)
    SDL_DestroyWindow(window)
    SDL_Quit()
