package indigoengine.sdl.facades.gl

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@extern
object GL:
  // Viewport
  def glClearColor(r: CFloat, g: CFloat, b: CFloat, a: CFloat): Unit = extern
  def glClear(mask: UInt): Unit                                      = extern
  def glViewport(x: CInt, y: CInt, width: CInt, height: CInt): Unit  = extern

  // Shaders
  def glCreateShader(shaderType: UInt): UInt                                                           = extern
  def glShaderSource(shader: UInt, count: CInt, string: Ptr[CString], length: Ptr[CInt]): Unit         = extern
  def glCompileShader(shader: UInt): Unit                                                              = extern
  def glGetShaderiv(shader: UInt, pname: UInt, params: Ptr[CInt]): Unit                                = extern
  def glGetShaderInfoLog(shader: UInt, maxLength: CInt, length: Ptr[CInt], infoLog: Ptr[Byte]): Unit   = extern
  def glCreateProgram(): UInt                                                                          = extern
  def glAttachShader(program: UInt, shader: UInt): Unit                                                = extern
  def glLinkProgram(program: UInt): Unit                                                               = extern
  def glGetProgramiv(program: UInt, pname: UInt, params: Ptr[CInt]): Unit                              = extern
  def glGetProgramInfoLog(program: UInt, maxLength: CInt, length: Ptr[CInt], infoLog: Ptr[Byte]): Unit = extern
  def glUseProgram(program: UInt): Unit                                                                = extern
  def glDeleteShader(shader: UInt): Unit                                                               = extern

  // VAO
  def glGenVertexArrays(n: CInt, arrays: Ptr[UInt]): Unit = extern
  def glBindVertexArray(array: UInt): Unit                = extern

  // Drawing
  def glDrawArrays(mode: UInt, first: CInt, count: CInt): Unit = extern
