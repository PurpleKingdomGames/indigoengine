package ultraviolet.datatypes

import scala.annotation.targetName

/** Stub implementations for common built in GLSL operations.
  */
trait ShaderDSLOps extends ShaderDSLTypeExtensions:

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/abs.xhtml
  def abs(genType: Int): Int     = Math.abs(genType)
  def abs(genType: Float): Float = Math.abs(genType)
  def abs(genType: vec2): vec2   = vec2(abs(genType.x), abs(genType.y))
  def abs(genType: vec3): vec3   = vec3(abs(genType.x), abs(genType.y), abs(genType.z))
  def abs(genType: vec4): vec4   = vec4(abs(genType.x), abs(genType.y), abs(genType.z), abs(genType.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/atan.xhtml
  def atan(y: Float, x: Float): Float = atan(y / x)
  def atan(y: vec2, x: vec2): vec2    = vec2(atan(y.x / x.x), atan(y.y / x.y))
  def atan(y: vec3, x: vec3): vec3    = vec3(atan(y.x / x.x), atan(y.y / x.y), atan(y.z / x.z))
  def atan(y: vec4, x: vec4): vec4    = vec4(atan(y.x / x.x), atan(y.y / x.y), atan(y.z / x.z), atan(y.w / x.w))
  def atan(yOverX: Float): Float      = Math.atan(yOverX).toFloat
  def atan(yOverX: vec2): vec2        = vec2(atan(yOverX.x), atan(yOverX.y))
  def atan(yOverX: vec3): vec3        = vec3(atan(yOverX.x), atan(yOverX.y), atan(yOverX.z))
  def atan(yOverX: vec4): vec4        = vec4(atan(yOverX.x), atan(yOverX.y), atan(yOverX.z), atan(yOverX.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/clamp.xhtml
  def clamp(genType: Int, min: Int, max: Int): Int         = 0
  def clamp(genType: Float, min: Float, max: Float): Float = 0.0f
  def clamp(genType: vec2, min: vec2, max: vec2): vec2     = vec2(0.0f)
  def clamp(genType: vec2, min: vec2, max: Float): vec2    = vec2(0.0f)
  def clamp(genType: vec3, min: vec3, max: vec3): vec3     = vec3(0.0f)
  def clamp(genType: vec3, min: vec3, max: Float): vec3    = vec3(0.0f)
  def clamp(genType: vec4, min: vec4, max: vec4): vec4     = vec4(0.0f)
  def clamp(genType: vec4, min: vec4, max: Float): vec4    = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/cos.xhtml
  def cos(genType: Float): Float = 0.0f
  def cos(genType: vec2): vec2   = vec2(0.0f)
  def cos(genType: vec3): vec3   = vec3(0.0f)
  def cos(genType: vec4): vec4   = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/dot.xhtml
  def dot(x: Float, y: Float): Float = 0.0f
  def dot(x: vec2, y: vec2): Float   = 0.0f
  def dot(x: vec3, y: vec3): Float   = 0.0f
  def dot(x: vec4, y: vec4): Float   = 0.0f

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/floor.xhtml
  def floor(genType: vec2): vec2 = genType

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/fract.xhtml
  def fract(genType: Float): Float = genType
  def fract(genType: vec2): vec2   = genType

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/length.xhtml
  def length(genType: Float | vec2 | vec3 | vec4): Float =
    genType match
      case f: Float =>
        Math.sqrt(Math.pow(f, 2.0f)).toFloat

      case vec2(x, y) =>
        Math.sqrt(Math.pow(x, 2.0f) + Math.pow(y, 2.0f)).toFloat

      case vec3(x, y, z) =>
        Math.sqrt(Math.pow(x, 2.0f) + Math.pow(y, 2.0f) + Math.pow(z, 2.0f)).toFloat

      case vec4(x, y, z, w) =>
        Math
          .sqrt(
            Math.pow(x, 2.0f) +
              Math.pow(y, 2.0f) +
              Math.pow(z, 2.0f) +
              Math.pow(w, 2.0f)
          )
          .toFloat

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/max.xhtml
  def max(x: Float, y: Float): Float = 0.0f
  def max(x: vec2, y: vec2): vec2    = vec2(0.0f)
  def max(x: vec2, y: Float): vec2   = vec2(0.0f)
  def max(x: vec3, y: vec3): vec3    = vec3(0.0f)
  def max(x: vec3, y: Float): vec3   = vec3(0.0f)
  def max(x: vec4, y: vec4): vec4    = vec4(0.0f)
  def max(x: vec4, y: Float): vec4   = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/min.xhtml
  def min(x: Float, y: Float): Float = 0.0f
  def min(x: vec2, y: vec2): vec2    = vec2(0.0f)
  def min(x: vec2, y: Float): vec2   = vec2(0.0f)
  def min(x: vec3, y: vec3): vec3    = vec3(0.0f)
  def min(x: vec3, y: Float): vec3   = vec3(0.0f)
  def min(x: vec4, y: vec4): vec4    = vec4(0.0f)
  def min(x: vec4, y: Float): vec4   = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/mix.xhtml
  def mix(x: Float, y: Float, a: Float): Float = 0.0f
  def mix(x: vec2, y: vec2, a: vec2): vec2     = vec2(0.0f)
  def mix(x: vec2, y: vec2, a: Float): vec2    = vec2(0.0f)
  def mix(x: vec3, y: vec3, a: vec3): vec3     = vec3(0.0f)
  def mix(x: vec3, y: vec3, a: Float): vec3    = vec3(0.0f)
  def mix(x: vec4, y: vec4, a: vec4): vec4     = vec4(0.0f)
  def mix(x: vec4, y: vec4, a: Float): vec4    = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/normalize.xhtml
  def normalize(genType: Float): Float = 0.0f
  def normalize(genType: vec2): vec2   = vec2(0.0f)
  def normalize(genType: vec3): vec3   = vec3(0.0f)
  def normalize(genType: vec4): vec4   = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/pow.xhtml
  def pow(x: Float, y: Float): Float = 0.0f
  def pow(x: vec2, y: vec2): vec2    = vec2(0.0f)
  def pow(x: vec3, y: vec3): vec3    = vec3(0.0f)
  def pow(x: vec4, y: vec4): vec4    = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/reflect.xhtml
  def reflect(i: Float, n: Float): Float = 0.0f
  def reflect(i: vec2, n: vec2): vec2    = vec2(0.0f)
  def reflect(i: vec3, n: vec3): vec3    = vec3(0.0f)
  def reflect(i: vec4, n: vec4): vec4    = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/sin.xhtml
  def sin(genType: Float): Float = 0.0f
  def sin(genType: vec2): vec2   = vec2(0.0f)
  def sin(genType: vec3): vec3   = vec3(0.0f)
  def sin(genType: vec4): vec4   = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/smoothstep.xhtml
  def smoothstep(edge0: Float, edge1: Float, x: Float): Float = 0.0f
  def smoothstep(edge0: vec2, edge1: vec2, x: vec2): vec2     = vec2(0.0f)
  def smoothstep(edge0: Float, edge1: Float, x: vec2): vec2   = vec2(0.0f)
  def smoothstep(edge0: vec3, edge1: vec3, x: vec3): vec3     = vec3(0.0f)
  def smoothstep(edge0: Float, edge1: Float, x: vec3): vec3   = vec3(0.0f)
  def smoothstep(edge0: vec4, edge1: vec4, x: vec4): vec4     = vec4(0.0f)
  def smoothstep(edge0: Float, edge1: Float, x: vec4): vec4   = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/step.xhtml
  def step(edge: Float, x: Float): Float =
    if x < edge then 0.0f else 1.0f

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/texture.xhtml
  def texture2D(sampler: sampler2D.type, coords: vec2): vec4 =
    vec4(0.0f)
  def textureCube(sampler: samplerCube.type, normal: vec3): vec4 =
    vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/tan.xhtml
  def tan(genType: Float): Float = 0.0f
  def tan(genType: vec2): vec2   = vec2(0.0f)
  def tan(genType: vec3): vec3   = vec3(0.0f)
  def tan(genType: vec4): vec4   = vec4(0.0f)
