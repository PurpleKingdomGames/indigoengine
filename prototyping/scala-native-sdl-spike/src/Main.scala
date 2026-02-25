import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import sdl.SDL.*
import sdl.SDLConstants.*
import gl.GL.*
import gl.GLConstants.*

object Main:

  // GLSL 4.10 (matches macOS OpenGL 4.1 via Metal).
  // Positions are generated from gl_VertexID — no vertex attributes needed,
  // which avoids a macOS Metal GL layer bug with null VBO offsets.
  val vertSrc: CString =
    c"#version 410 core\nout vec2 vUV;\nvoid main() {\n  vec2 pos[4];\n  pos[0]=vec2(-1.0,-1.0);\n  pos[1]=vec2( 1.0,-1.0);\n  pos[2]=vec2(-1.0, 1.0);\n  pos[3]=vec2( 1.0, 1.0);\n  vUV = pos[gl_VertexID] * 0.5 + 0.5;\n  gl_Position = vec4(pos[gl_VertexID], 0.0, 1.0);\n}\n"

  val fragSrc: CString =
    c"#version 410 core\nin vec2 vUV;\nlayout(location = 0) out vec4 fragColor;\nvoid main() {\n  fragColor = vec4(vUV.x, vUV.y, 0.0, 1.0);\n}\n"

  def compileShader(shaderType: UInt, src: CString): UInt =
    val shader = glCreateShader(shaderType)
    val srcPtr = stackalloc[CString]()
    !srcPtr = src
    glShaderSource(shader, 1, srcPtr, null)
    glCompileShader(shader)

    val status = stackalloc[CInt]()
    glGetShaderiv(shader, GL_COMPILE_STATUS, status)
    if !status == 0 then
      val logBuf = stackalloc[Byte](512)
      glGetShaderInfoLog(shader, 512, null, logBuf)
      val msg = fromCString(logBuf)
      throw new RuntimeException(s"Shader compile error (type ${shaderType.toInt}): $msg")

    shader

  def createProgram(vs: CString, fs: CString): UInt =
    val vert    = compileShader(GL_VERTEX_SHADER, vs)
    val frag    = compileShader(GL_FRAGMENT_SHADER, fs)
    val program = glCreateProgram()
    glAttachShader(program, vert)
    glAttachShader(program, frag)
    glLinkProgram(program)
    glDeleteShader(vert)
    glDeleteShader(frag)

    val linkStatus = stackalloc[CInt]()
    glGetProgramiv(program, GL_LINK_STATUS, linkStatus)
    if !linkStatus == 0 then
      val logBuf = stackalloc[Byte](512)
      glGetProgramInfoLog(program, 512, null, logBuf)
      val msg = fromCString(logBuf)
      throw new RuntimeException(s"Program link error: $msg")

    program

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
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_FLAGS, SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG)
    SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1)

    val window = SDL_CreateWindow(
      c"UV Quad - Scala Native",
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

    val program = createProgram(vertSrc, fragSrc)
    val vao     = makeVao()

    // Initial clear+swap to ensure the Metal drawable is ready before first draw
    glClear(GL_COLOR_BUFFER_BIT)
    SDL_GL_SwapWindow(window)

    val event   = stackalloc[sdl.SDL.SDL_Event]()
    var running = true

    while running do
      while SDL_PollEvent(event) != 0 do
        val eventType = !(event.asInstanceOf[Ptr[UInt]])
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
