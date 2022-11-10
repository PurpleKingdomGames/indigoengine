package ultraviolet

import ultraviolet.core.ShaderDSLOps

import scala.annotation.StaticAnnotation

object syntax extends ShaderDSLOps:

  extension (f: Float)
    def +(v: vec2): vec2 = vec2(f + v.x, f + v.y)
    def -(v: vec2): vec2 = vec2(f - v.x, f - v.y)
    def *(v: vec2): vec2 = vec2(f * v.x, f * v.y)
    def /(v: vec2): vec2 = vec2(f / v.x, f / v.y)

    def +(v: vec3): vec3 = vec3(f + v.x, f + v.y, f + v.z)
    def -(v: vec3): vec3 = vec3(f - v.x, f - v.y, f - v.z)
    def *(v: vec3): vec3 = vec3(f * v.x, f * v.y, f * v.z)
    def /(v: vec3): vec3 = vec3(f / v.x, f / v.y, f / v.z)

    def +(v: vec4): vec4 = vec4(f + v.x, f + v.y, f + v.z, f + v.w)
    def -(v: vec4): vec4 = vec4(f - v.x, f - v.y, f - v.z, f - v.w)
    def *(v: vec4): vec4 = vec4(f * v.x, f * v.y, f * v.z, f * v.w)
    def /(v: vec4): vec4 = vec4(f / v.x, f / v.y, f / v.z, f / v.w)

  type RawGLSL = ultraviolet.core.RawGLSL
  val RawGLSL: ultraviolet.core.RawGLSL.type = ultraviolet.core.RawGLSL

  type Shader[In, Out] = ultraviolet.core.Shader[In, Out]
  val Shader: ultraviolet.core.Shader.type = ultraviolet.core.Shader

  final class out extends StaticAnnotation
  final class in  extends StaticAnnotation

  // object glsl:

  //   inline def Version300ES: RawGLSL = RawGLSL("#version 300 es\n")

  //   inline def PrecisionHighPFloat: RawGLSL   = RawGLSL("precision highp float;\n")
  //   inline def PrecisionMediumPFloat: RawGLSL = RawGLSL("precision mediump float;\n")
  //   inline def PrecisionLowPFloat: RawGLSL    = RawGLSL("precision lowp float;\n")

  // end glsl

end syntax
