package ultraviolet.datatypes

import scala.annotation.targetName

/** Incomplete list of implementations for common built in GLSL operations.
  *
  * The full list is here: https://registry.khronos.org/OpenGL-Refpages/gl4/html/
  */
trait ShaderDSLOps extends ShaderDSLTypeExtensions:

  extension (b: Boolean)
    def toInt: Int     = if b then 1 else 0
    def toFloat: Float = if b then 1.0f else 0.0f

  extension (i: Int) def toBoolean: Boolean = if i == 0 then false else true

  extension (f: Float)
    def toBoolean: Boolean = if f == 0.0f then false else true

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

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/abs.xhtml
  def abs(x: Int): Int     = Math.abs(x)
  def abs(x: Float): Float = Math.abs(x)
  def abs(x: vec2): vec2   = vec2(abs(x.x), abs(x.y))
  def abs(x: vec3): vec3   = vec3(abs(x.x), abs(x.y), abs(x.z))
  def abs(x: vec4): vec4   = vec4(abs(x.x), abs(x.y), abs(x.z), abs(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/atan.xhtml
  def atan(y: Float, x: Float): Float = atan(y / x)
  def atan(y: vec2, x: vec2): vec2    = vec2(atan(y.x / x.x), atan(y.y / x.y))
  def atan(y: vec3, x: vec3): vec3    = vec3(atan(y.x / x.x), atan(y.y / x.y), atan(y.z / x.z))
  def atan(y: vec4, x: vec4): vec4    = vec4(atan(y.x / x.x), atan(y.y / x.y), atan(y.z / x.z), atan(y.w / x.w))
  def atan(yOverX: Float): Float      = Math.atan(yOverX).toFloat
  def atan(yOverX: vec2): vec2        = vec2(atan(yOverX.x), atan(yOverX.y))
  def atan(yOverX: vec3): vec3        = vec3(atan(yOverX.x), atan(yOverX.y), atan(yOverX.z))
  def atan(yOverX: vec4): vec4        = vec4(atan(yOverX.x), atan(yOverX.y), atan(yOverX.z), atan(yOverX.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/ceil.xhtml
  def ceil(x: Float): Float = Math.ceil(x.toDouble).toFloat
  def ceil(x: vec2): vec2   = vec2(ceil(x.x), ceil(x.y))
  def ceil(x: vec3): vec3   = vec3(ceil(x.x), ceil(x.y), ceil(x.z))
  def ceil(x: vec4): vec4   = vec4(ceil(x.x), ceil(x.y), ceil(x.z), ceil(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/clamp.xhtml
  def clamp(x: Int, minVal: Int, maxVal: Int): Int         = min(max(x, minVal), maxVal)
  def clamp(x: Float, minVal: Float, maxVal: Float): Float = min(max(x, minVal), maxVal)
  def clamp(x: vec2, minVal: vec2, maxVal: vec2): vec2     = min(max(x, minVal), maxVal)
  def clamp(x: vec2, minVal: vec2, maxVal: Float): vec2    = min(max(x, minVal), maxVal)
  def clamp(x: vec3, minVal: vec3, maxVal: vec3): vec3     = min(max(x, minVal), maxVal)
  def clamp(x: vec3, minVal: vec3, maxVal: Float): vec3    = min(max(x, minVal), maxVal)
  def clamp(x: vec4, minVal: vec4, maxVal: vec4): vec4     = min(max(x, minVal), maxVal)
  def clamp(x: vec4, minVal: vec4, maxVal: Float): vec4    = min(max(x, minVal), maxVal)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/cos.xhtml
  def cos(x: Float): Float = Math.cos(x.toDouble).toFloat
  def cos(x: vec2): vec2   = vec2(cos(x.x), cos(x.y))
  def cos(x: vec3): vec3   = vec3(cos(x.x), cos(x.y), cos(x.z))
  def cos(x: vec4): vec4   = vec4(cos(x.x), cos(x.y), cos(x.z), cos(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/cross.xhtml
  def cross(x: vec3, y: vec3): Float =
    val a = x.y * y.z - y.y * x.z
    val b = x.z * y.x - y.z * x.x
    val c = x.x * y.y - y.x * x.y
    a * b * c

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/distance.xhtml
  def distance(x: Float, y: Float): Float = length(x - y)
  def distance(x: vec2, y: vec2): Float   = length(x - y)
  def distance(x: vec3, y: vec3): Float   = length(x - y)
  def distance(x: vec4, y: vec4): Float   = length(x - y)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/dot.xhtml
  def dot(x: Float, y: Float): Float = x * y
  def dot(x: vec2, y: vec2): Float   = x.x * y.x + x.y * y.y
  def dot(x: vec3, y: vec3): Float   = x.x * y.x + x.y * y.y + x.z * y.z
  def dot(x: vec4, y: vec4): Float   = x.x * y.x + x.y * y.y + x.z * y.z + x.w * y.w

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/floor.xhtml
  def floor(x: Float): Float = Math.floor(x).toFloat
  def floor(x: vec2): vec2   = vec2(floor(x.x), floor(x.y))
  def floor(x: vec3): vec3   = vec3(floor(x.x), floor(x.y), floor(x.z))
  def floor(x: vec4): vec4   = vec4(floor(x.x), floor(x.y), floor(x.z), floor(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/fract.xhtml
  def fract(x: Float): Float = x - floor(x)
  def fract(x: vec2): vec2   = x - floor(x)
  def fract(x: vec3): vec3   = x - floor(x)
  def fract(x: vec4): vec4   = x - floor(x)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/length.xhtml
  def length(x: Float): Float = Math.sqrt(Math.pow(x, 2.0f)).toFloat
  def length(x: vec2): Float  = Math.sqrt(Math.pow(x.x, 2.0f) + Math.pow(x.y, 2.0f)).toFloat
  def length(x: vec3): Float  = Math.sqrt(Math.pow(x.x, 2.0f) + Math.pow(x.y, 2.0f) + Math.pow(x.z, 2.0f)).toFloat
  def length(x: vec4): Float =
    Math.sqrt(Math.pow(x.x, 2.0f) + Math.pow(x.y, 2.0f) + Math.pow(x.z, 2.0f) + Math.pow(x.w, 2.0f)).toFloat

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/max.xhtml
  def max(x: Int, y: Int): Int       = Math.max(x, y)
  def max(x: Float, y: Float): Float = Math.max(x, y)
  def max(x: vec2, y: vec2): vec2    = vec2(max(x.x, y.x), max(x.y, y.y))
  def max(x: vec2, y: Float): vec2   = vec2(max(x.x, y), max(x.y, y))
  def max(x: vec3, y: vec3): vec3    = vec3(max(x.x, y.x), max(x.y, y.y), max(x.z, y.z))
  def max(x: vec3, y: Float): vec3   = vec3(max(x.x, y), max(x.y, y), max(x.z, y))
  def max(x: vec4, y: vec4): vec4    = vec4(max(x.x, y.x), max(x.y, y.y), max(x.z, y.z), max(x.w, y.w))
  def max(x: vec4, y: Float): vec4   = vec4(max(x.x, y), max(x.y, y), max(x.z, y), max(x.w, y))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/min.xhtml
  def min(x: Int, y: Int): Int       = Math.min(x, y)
  def min(x: Float, y: Float): Float = Math.min(x, y)
  def min(x: vec2, y: vec2): vec2    = vec2(min(x.x, y.x), min(x.y, y.y))
  def min(x: vec2, y: Float): vec2   = vec2(min(x.x, y), min(x.y, y))
  def min(x: vec3, y: vec3): vec3    = vec3(min(x.x, y.x), min(x.y, y.y), min(x.z, y.z))
  def min(x: vec3, y: Float): vec3   = vec3(min(x.x, y), min(x.y, y), min(x.z, y))
  def min(x: vec4, y: vec4): vec4    = vec4(min(x.x, y.x), min(x.y, y.y), min(x.z, y.z), min(x.w, y.w))
  def min(x: vec4, y: Float): vec4   = vec4(min(x.x, y), min(x.y, y), min(x.z, y), min(x.w, y))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/mix.xhtml
  def mix(x: Float, y: Float, a: Float): Float = x * (1 - a) + y * a
  def mix(x: vec2, y: vec2, a: vec2): vec2 =
    vec2(mix(x.x, y.x, a.x), mix(x.y, y.y, a.y))
  def mix(x: vec2, y: vec2, a: Float): vec2 =
    vec2(mix(x.x, y.x, a), mix(x.y, y.y, a))
  def mix(x: vec3, y: vec3, a: vec3): vec3 =
    vec3(mix(x.x, y.x, a.x), mix(x.y, y.y, a.y), mix(x.z, y.z, a.z))
  def mix(x: vec3, y: vec3, a: Float): vec3 =
    vec3(mix(x.x, y.x, a), mix(x.y, y.y, a), mix(x.z, y.z, a))
  def mix(x: vec4, y: vec4, a: vec4): vec4 =
    vec4(mix(x.x, y.x, a.x), mix(x.y, y.y, a.y), mix(x.z, y.z, a.z), mix(x.w, y.w, a.w))
  def mix(x: vec4, y: vec4, a: Float): vec4 =
    vec4(mix(x.x, y.x, a), mix(x.y, y.y, a), mix(x.z, y.z, a), mix(x.w, y.w, a))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/mod.xhtml
  def mod(x: Float, y: Float): Float = x - (y * Math.floor(x / y).toFloat)
  def mod(x: vec2, y: vec2): vec2    = vec2(mod(x.x, y.x), mod(x.y, y.y))
  def mod(x: vec2, y: Float): vec2   = vec2(mod(x.x, y), mod(x.y, y))
  def mod(x: vec3, y: vec3): vec3    = vec3(mod(x.x, y.x), mod(x.y, y.y), mod(x.z, y.z))
  def mod(x: vec3, y: Float): vec3   = vec3(mod(x.x, y), mod(x.y, y), mod(x.z, y))
  def mod(x: vec4, y: vec4): vec4    = vec4(mod(x.x, y.x), mod(x.y, y.y), mod(x.z, y.z), mod(x.w, y.w))
  def mod(x: vec4, y: Float): vec4   = vec4(mod(x.x, y), mod(x.y, y), mod(x.z, y), mod(x.w, y))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/normalize.xhtml
  def normalize(x: Float): Float =
    val m = length(x)
    if m == 0.0f then 0.0f else x / m
  def normalize(x: vec2): vec2 =
    val m = length(x)
    if m == 0.0f then vec2(0.0f) else vec2(x.x / m, x.y / m)
  def normalize(x: vec3): vec3 =
    val m = length(x)
    if m == 0.0f then vec3(0.0f) else vec3(x.x / m, x.y / m, x.z / m)
  def normalize(x: vec4): vec4 =
    val m = length(x)
    if m == 0.0f then vec4(0.0f) else vec4(x.x / m, x.y / m, x.z / m, x.w / m)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/pow.xhtml
  def pow(x: Float, y: Float): Float = Math.pow(x.toDouble, y.toDouble).toFloat
  def pow(x: vec2, y: vec2): vec2    = vec2(pow(x.x, y.x), pow(x.y, y.y))
  def pow(x: vec3, y: vec3): vec3    = vec3(pow(x.x, y.x), pow(x.y, y.y), pow(x.z, y.z))
  def pow(x: vec4, y: vec4): vec4    = vec4(pow(x.x, y.x), pow(x.y, y.y), pow(x.z, y.z), pow(x.w, y.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/reflect.xhtml
  def reflect(i: Float, n: Float): Float = i - 2.0f * dot(n, i) * n
  def reflect(i: vec2, n: vec2): vec2    = i - 2.0f * dot(n, i) * n
  def reflect(i: vec3, n: vec3): vec3    = i - 2.0f * dot(n, i) * n
  def reflect(i: vec4, n: vec4): vec4    = i - 2.0f * dot(n, i) * n

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/round.xhtml
  def round(x: Float): Float = Math.round(x.toDouble).toFloat
  def round(x: vec2): vec2   = vec2(round(x.x), round(x.y))
  def round(x: vec3): vec3   = vec3(round(x.x), round(x.y), round(x.z))
  def round(x: vec4): vec4   = vec4(round(x.x), round(x.y), round(x.z), round(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/sin.xhtml
  def sin(x: Float): Float = Math.sin(x.toDouble).toFloat
  def sin(x: vec2): vec2   = vec2(sin(x.x), sin(x.y))
  def sin(x: vec3): vec3   = vec3(sin(x.x), sin(x.y), sin(x.z))
  def sin(x: vec4): vec4   = vec4(sin(x.x), sin(x.y), sin(x.z), sin(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/smoothstep.xhtml
  def smoothstep(edge0: Float, edge1: Float, x: Float): Float =
    val t: Float = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f)
    t * t * (3.0f - 2.0f * t)
  def smoothstep(edge0: vec2, edge1: vec2, x: vec2): vec2 =
    vec2(smoothstep(edge0.x, edge1.x, x.x), smoothstep(edge0.y, edge1.y, x.y))
  def smoothstep(edge0: Float, edge1: Float, x: vec2): vec2 =
    vec2(smoothstep(edge0, edge1, x.x), smoothstep(edge0, edge1, x.y))
  def smoothstep(edge0: vec3, edge1: vec3, x: vec3): vec3 =
    vec3(smoothstep(edge0.x, edge1.x, x.x), smoothstep(edge0.y, edge1.y, x.y), smoothstep(edge0.z, edge1.z, x.z))
  def smoothstep(edge0: Float, edge1: Float, x: vec3): vec3 =
    vec3(smoothstep(edge0, edge1, x.x), smoothstep(edge0, edge1, x.y), smoothstep(edge0, edge1, x.z))
  def smoothstep(edge0: vec4, edge1: vec4, x: vec4): vec4 =
    vec4(
      smoothstep(edge0.x, edge1.x, x.x),
      smoothstep(edge0.y, edge1.y, x.y),
      smoothstep(edge0.z, edge1.z, x.z),
      smoothstep(edge0.w, edge1.w, x.w)
    )
  def smoothstep(edge0: Float, edge1: Float, x: vec4): vec4 =
    vec4(
      smoothstep(edge0, edge1, x.x),
      smoothstep(edge0, edge1, x.y),
      smoothstep(edge0, edge1, x.z),
      smoothstep(edge0, edge1, x.w)
    )

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/sqrt.xhtml
  def sqrt(x: Float): Float = Math.sqrt(x).toFloat
  def sqrt(x: vec2): vec2   = vec2(sqrt(x.x), sqrt(x.y))
  def sqrt(x: vec3): vec3   = vec3(sqrt(x.x), sqrt(x.y), sqrt(x.z))
  def sqrt(x: vec4): vec4   = vec4(sqrt(x.x), sqrt(x.y), sqrt(x.z), sqrt(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/step.xhtml
  def step(edge: Float, x: Float): Float = if x < edge then 0.0f else 1.0f
  def step(edge: vec2, x: vec2): vec2    = vec2(step(edge.x, x.x), step(edge.y, x.y))
  def step(edge: Float, x: vec2): vec2   = vec2(step(edge, x.x), step(edge, x.y))
  def step(edge: vec3, x: vec3): vec3    = vec3(step(edge.x, x.x), step(edge.y, x.y), step(edge.z, x.z))
  def step(edge: Float, x: vec3): vec3   = vec3(step(edge, x.x), step(edge, x.y), step(edge, x.z))
  def step(edge: vec4, x: vec4): vec4 = vec4(step(edge.x, x.x), step(edge.y, x.y), step(edge.z, x.z), step(edge.w, x.w))
  def step(edge: Float, x: vec4): vec4 = vec4(step(edge, x.x), step(edge, x.y), step(edge, x.z), step(edge, x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/tan.xhtml
  def tan(x: Float): Float = Math.tan(x.toDouble).toFloat
  def tan(x: vec2): vec2   = vec2(tan(x.x), tan(x.y))
  def tan(x: vec3): vec3   = vec3(tan(x.x), tan(x.y), tan(x.z))
  def tan(x: vec4): vec4   = vec4(tan(x.x), tan(x.y), tan(x.z), tan(x.w))

  // WebGL 2.0 converted `texture2D` and `textureCube` into `texture`, but it is more convenient for us to keep them separate.
  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/texture.xhtml
  def texture2D(sampler: sampler2D.type, coords: vec2): vec4     = vec4(0.0f)
  def textureCube(sampler: samplerCube.type, normal: vec3): vec4 = vec4(0.0f)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/transpose.xhtml
  def transpose(m: mat2): mat2 =
    mat2(
      m(0),
      m(2),
      m(1),
      m(3)
    )
  def transpose(m: mat3): mat3 =
    mat3(
      m(0),
      m(3),
      m(6),
      m(1),
      m(4),
      m(7),
      m(2),
      m(5),
      m(8)
    )
  def transpose(m: mat4): mat4 =
    mat4(
      m(0),
      m(4),
      m(8),
      m(12),
      m(1),
      m(5),
      m(9),
      m(13),
      m(2),
      m(6),
      m(10),
      m(14),
      m(3),
      m(7),
      m(11),
      m(15)
    )
