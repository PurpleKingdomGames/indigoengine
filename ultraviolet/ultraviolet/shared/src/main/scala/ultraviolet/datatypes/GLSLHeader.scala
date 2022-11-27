package ultraviolet.datatypes

final case class GLSLHeader(glsl: String)
object GLSLHeader:

  inline def Version300ES: GLSLHeader = GLSLHeader("#version 300 es")

  inline def PrecisionHighPFloat: GLSLHeader   = GLSLHeader("precision highp float;")
  inline def PrecisionMediumPFloat: GLSLHeader = GLSLHeader("precision mediump float;")
  inline def PrecisionLowPFloat: GLSLHeader    = GLSLHeader("precision lowp float;")
