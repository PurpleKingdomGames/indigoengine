package gl

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@extern
object GL:
  def glClearColor(r: CFloat, g: CFloat, b: CFloat, a: CFloat): Unit = extern
  def glClear(mask: UInt): Unit = extern
  def glViewport(x: CInt, y: CInt, width: CInt, height: CInt): Unit = extern

  // Shaders
  def glCreateShader(shaderType: UInt): UInt = extern
  def glShaderSource(shader: UInt, count: CInt, string: Ptr[CString], length: Ptr[CInt]): Unit = extern
  def glCompileShader(shader: UInt): Unit = extern
  def glGetShaderiv(shader: UInt, pname: UInt, params: Ptr[CInt]): Unit = extern
  def glGetShaderInfoLog(shader: UInt, maxLength: CInt, length: Ptr[CInt], infoLog: Ptr[Byte]): Unit = extern
  def glCreateProgram(): UInt = extern
  def glAttachShader(program: UInt, shader: UInt): Unit = extern
  def glLinkProgram(program: UInt): Unit = extern
  def glGetProgramiv(program: UInt, pname: UInt, params: Ptr[CInt]): Unit = extern
  def glGetProgramInfoLog(program: UInt, maxLength: CInt, length: Ptr[CInt], infoLog: Ptr[Byte]): Unit = extern
  def glUseProgram(program: UInt): Unit = extern
  def glDeleteShader(shader: UInt): Unit = extern

  // VAO (required in Core Profile even without vertex attributes)
  def glGenVertexArrays(n: CInt, arrays: Ptr[UInt]): Unit = extern
  def glBindVertexArray(array: UInt): Unit = extern

  // Drawing
  def glDrawArrays(mode: UInt, first: CInt, count: CInt): Unit = extern

object GLConstants:
  val GL_COLOR_BUFFER_BIT: UInt = 0x00004000.toUInt
  val GL_TRIANGLE_STRIP: UInt  = 0x0005.toUInt
  val GL_VERTEX_SHADER: UInt   = 0x8B31.toUInt
  val GL_FRAGMENT_SHADER: UInt = 0x8B30.toUInt
  val GL_COMPILE_STATUS: UInt  = 0x8B81.toUInt
  val GL_LINK_STATUS: UInt     = 0x8B82.toUInt
