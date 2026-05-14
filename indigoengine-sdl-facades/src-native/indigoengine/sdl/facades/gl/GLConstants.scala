package indigoengine.sdl.facades.gl

import scala.scalanative.unsigned.*

object GLConstants:
  val GL_COLOR_BUFFER_BIT: UInt = 0x00004000.toUInt
  val GL_TRIANGLE_STRIP: UInt   = 0x0005.toUInt
  val GL_VERTEX_SHADER: UInt    = 0x8b31.toUInt
  val GL_FRAGMENT_SHADER: UInt  = 0x8b30.toUInt
  val GL_COMPILE_STATUS: UInt   = 0x8b81.toUInt
  val GL_LINK_STATUS: UInt      = 0x8b82.toUInt
