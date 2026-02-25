package demo

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import facades.sdl.SDL.*
import facades.sdl.SDLConstants.*
import facades.gl.GL.*
import facades.gl.GLConstants.*

object Shaders:

  // GLSL 4.10 (matches macOS OpenGL 4.1 via Metal).
  // Positions are generated from gl_VertexID — no vertex attributes needed,
  // which avoids a macOS Metal GL layer bug with null VBO offsets.
  val vertSrc: CString =
    c"""#version 410 core

out vec2 vUV;

void main() {
  vec2 pos[4];
  pos[0]=vec2(-1.0,-1.0);
  pos[1]=vec2( 1.0,-1.0);
  pos[2]=vec2(-1.0, 1.0);
  pos[3]=vec2( 1.0, 1.0);

  vUV = pos[gl_VertexID] * 0.5 + 0.5;

  gl_Position = vec4(pos[gl_VertexID], 0.0, 1.0);
}
"""

  val fragSrc: CString =
    c"""#version 410 core

in vec2 vUV;
layout(location = 0)
out vec4 fragColor;

void main() {
  fragColor = vec4(vUV.x, vUV.y, 0.0, 1.0);
}
"""

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
      throw new RuntimeException(
        s"Shader compile error (type ${shaderType.toInt}): $msg"
      )

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
