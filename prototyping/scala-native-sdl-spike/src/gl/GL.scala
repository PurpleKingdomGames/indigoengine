package gl

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@extern
object GL:
  def glClearColor(r: CFloat, g: CFloat, b: CFloat, a: CFloat): Unit = extern
  def glClear(mask: UInt): Unit = extern
  def glBegin(mode: UInt): Unit = extern
  def glEnd(): Unit = extern
  def glVertex2f(x: CFloat, y: CFloat): Unit = extern
  def glColor3f(r: CFloat, g: CFloat, b: CFloat): Unit = extern
  def glViewport(x: CInt, y: CInt, width: CInt, height: CInt): Unit = extern
  def glFlush(): Unit = extern

object GLConstants:
  val GL_COLOR_BUFFER_BIT: UInt = 0x00004000.toUInt
  val GL_TRIANGLES: UInt = 0x0004.toUInt
