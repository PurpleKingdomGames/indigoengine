package ultraviolet.datatypes

final case class ShaderHeader(value: String)
object ShaderHeader:
  val Version300ES: ShaderHeader          = ShaderHeader("#version 300 es")
  val PrecisionHighPFloat: ShaderHeader   = ShaderHeader("precision highp float;")
  val PrecisionMediumPFloat: ShaderHeader = ShaderHeader("precision mediump float;")
  val PrecisionLowPFloat: ShaderHeader    = ShaderHeader("precision lowp float;")
