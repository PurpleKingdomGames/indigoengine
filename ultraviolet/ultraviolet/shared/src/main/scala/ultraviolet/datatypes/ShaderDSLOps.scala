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

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/acos.xhtml
  def acos(x: Float): Float = Math.acos(x.toDouble).toFloat
  def acos(x: vec2): vec2   = vec2(acos(x.x), acos(x.y))
  def acos(x: vec3): vec3   = vec3(acos(x.x), acos(x.y), acos(x.z))
  def acos(x: vec4): vec4   = vec4(acos(x.x), acos(x.y), acos(x.z), acos(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/all.xhtml
  def all(x: bvec2): Boolean = x.x && x.y
  def all(x: bvec3): Boolean = x.x && x.y && x.z
  def all(x: bvec4): Boolean = x.x && x.y && x.z && x.w

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/any.xhtml
  def any(x: bvec2): Boolean = x.x || x.y
  def any(x: bvec3): Boolean = x.x || x.y || x.z
  def any(x: bvec4): Boolean = x.x || x.y || x.z || x.w

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/asin.xhtml
  def asin(x: Float): Float = Math.asin(x.toDouble).toFloat
  def asin(x: vec2): vec2   = vec2(asin(x.x), asin(x.y))
  def asin(x: vec3): vec3   = vec3(asin(x.x), asin(x.y), asin(x.z))
  def asin(x: vec4): vec4   = vec4(asin(x.x), asin(x.y), asin(x.z), asin(x.w))

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

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/degrees.xhtml
  def degrees(radians: Float): Float = ((180.0 * radians) / Math.PI).toFloat
  def degrees(radians: vec2): vec2   = vec2(degrees(radians.x), degrees(radians.y))
  def degrees(radians: vec3): vec3   = vec3(degrees(radians.x), degrees(radians.y), degrees(radians.z))
  def degrees(radians: vec4): vec4 =
    vec4(degrees(radians.x), degrees(radians.y), degrees(radians.z), degrees(radians.w))

  // STUB
  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/dFdx.xhtml
  def dFdx(x: Float): Float = 0.0f
  def dFdx(x: vec2): vec2   = vec2(0.0f)
  def dFdx(x: vec3): vec3   = vec3(0.0f)
  def dFdx(x: vec4): vec4   = vec4(0.0f)

  // STUB
  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/dFdy.xhtml
  def dFdy(x: Float): Float = 0.0f
  def dFdy(x: vec2): vec2   = vec2(0.0f)
  def dFdy(x: vec3): vec3   = vec3(0.0f)
  def dFdy(x: vec4): vec4   = vec4(0.0f)

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

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/equal.xhtml
  def equal(x: vec2, y: vec2): bvec2   = bvec2(x.x == y.x, x.y == y.y)
  def equal(x: vec3, y: vec3): bvec3   = bvec3(x.x == y.x, x.y == y.y, x.z == y.z)
  def equal(x: vec4, y: vec4): bvec4   = bvec4(x.x == y.x, x.y == y.y, x.z == y.z, x.w == y.w)
  def equal(x: ivec2, y: ivec2): bvec2 = bvec2(x.x == y.x, x.y == y.y)
  def equal(x: ivec3, y: ivec3): bvec3 = bvec3(x.x == y.x, x.y == y.y, x.z == y.z)
  def equal(x: ivec4, y: ivec4): bvec4 = bvec4(x.x == y.x, x.y == y.y, x.z == y.z, x.w == y.w)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/exp.xhtml
  def exp(x: Float): Float = Math.exp(x.toDouble).toFloat
  def exp(x: vec2): vec2   = vec2(exp(x.x), exp(x.y))
  def exp(x: vec3): vec3   = vec3(exp(x.x), exp(x.y), exp(x.z))
  def exp(x: vec4): vec4   = vec4(exp(x.x), exp(x.y), exp(x.z), exp(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/exp2.xhtml
  def exp2(x: Float): Float = Math.pow(x.toDouble, 2.0).toFloat
  def exp2(x: vec2): vec2   = vec2(exp2(x.x), exp2(x.y))
  def exp2(x: vec3): vec3   = vec3(exp2(x.x), exp2(x.y), exp2(x.z))
  def exp2(x: vec4): vec4   = vec4(exp2(x.x), exp2(x.y), exp2(x.z), exp2(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/faceforward.xhtml
  def faceforwad(n: Float, i: Float, nRef: Float): Float = if dot(nRef, i) < 0 then n else -n
  def faceforwad(n: vec2, i: vec2, nRef: vec2): vec2     = if dot(nRef, i) < 0 then n else -n
  def faceforwad(n: vec3, i: vec3, nRef: vec3): vec3     = if dot(nRef, i) < 0 then n else -n
  def faceforwad(n: vec4, i: vec4, nRef: vec4): vec4     = if dot(nRef, i) < 0 then n else -n

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

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/fwidth.xhtml
  def fwidth(p: Float): Float = abs(dFdx(p)) + abs(dFdy(p))
  def fwidth(p: vec2): vec2   = abs(dFdx(p)) + abs(dFdy(p))
  def fwidth(p: vec3): vec3   = abs(dFdx(p)) + abs(dFdy(p))
  def fwidth(p: vec4): vec4   = abs(dFdx(p)) + abs(dFdy(p))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/greaterThan.xhtml
  def greaterThan(x: vec2, y: vec2): bvec2   = bvec2(x.x > y.x, x.y > y.y)
  def greaterThan(x: vec3, y: vec3): bvec3   = bvec3(x.x > y.x, x.y > y.y, x.z > y.z)
  def greaterThan(x: vec4, y: vec4): bvec4   = bvec4(x.x > y.x, x.y > y.y, x.z > y.z, x.w > y.w)
  def greaterThan(x: ivec2, y: ivec2): bvec2 = bvec2(x.x > y.x, x.y > y.y)
  def greaterThan(x: ivec3, y: ivec3): bvec3 = bvec3(x.x > y.x, x.y > y.y, x.z > y.z)
  def greaterThan(x: ivec4, y: ivec4): bvec4 = bvec4(x.x > y.x, x.y > y.y, x.z > y.z, x.w > y.w)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/greaterThanEqual.xhtml
  def greaterThanEqual(x: vec2, y: vec2): bvec2   = bvec2(x.x >= y.x, x.y >= y.y)
  def greaterThanEqual(x: vec3, y: vec3): bvec3   = bvec3(x.x >= y.x, x.y >= y.y, x.z >= y.z)
  def greaterThanEqual(x: vec4, y: vec4): bvec4   = bvec4(x.x >= y.x, x.y >= y.y, x.z >= y.z, x.w >= y.w)
  def greaterThanEqual(x: ivec2, y: ivec2): bvec2 = bvec2(x.x >= y.x, x.y >= y.y)
  def greaterThanEqual(x: ivec3, y: ivec3): bvec3 = bvec3(x.x >= y.x, x.y >= y.y, x.z >= y.z)
  def greaterThanEqual(x: ivec4, y: ivec4): bvec4 = bvec4(x.x >= y.x, x.y >= y.y, x.z >= y.z, x.w >= y.w)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/inversesqrt.xhtml
  def inversesqrt(x: Float): Float = 1.0f / sqrt(x)
  def inversesqrt(x: vec2): vec2   = 1.0f / sqrt(x)
  def inversesqrt(x: vec3): vec3   = 1.0f / sqrt(x)
  def inversesqrt(x: vec4): vec4   = 1.0f / sqrt(x)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/length.xhtml
  def length(x: Float): Float = Math.sqrt(Math.pow(x, 2.0f)).toFloat
  def length(x: vec2): Float  = Math.sqrt(Math.pow(x.x, 2.0f) + Math.pow(x.y, 2.0f)).toFloat
  def length(x: vec3): Float  = Math.sqrt(Math.pow(x.x, 2.0f) + Math.pow(x.y, 2.0f) + Math.pow(x.z, 2.0f)).toFloat
  def length(x: vec4): Float =
    Math.sqrt(Math.pow(x.x, 2.0f) + Math.pow(x.y, 2.0f) + Math.pow(x.z, 2.0f) + Math.pow(x.w, 2.0f)).toFloat

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/lessThan.xhtml
  def lessThan(x: vec2, y: vec2): bvec2   = bvec2(x.x < y.x, x.y < y.y)
  def lessThan(x: vec3, y: vec3): bvec3   = bvec3(x.x < y.x, x.y < y.y, x.z < y.z)
  def lessThan(x: vec4, y: vec4): bvec4   = bvec4(x.x < y.x, x.y < y.y, x.z < y.z, x.w < y.w)
  def lessThan(x: ivec2, y: ivec2): bvec2 = bvec2(x.x < y.x, x.y < y.y)
  def lessThan(x: ivec3, y: ivec3): bvec3 = bvec3(x.x < y.x, x.y < y.y, x.z < y.z)
  def lessThan(x: ivec4, y: ivec4): bvec4 = bvec4(x.x < y.x, x.y < y.y, x.z < y.z, x.w < y.w)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/lessThanEqual.xhtml
  def lessThanEqual(x: vec2, y: vec2): bvec2   = bvec2(x.x <= y.x, x.y <= y.y)
  def lessThanEqual(x: vec3, y: vec3): bvec3   = bvec3(x.x <= y.x, x.y <= y.y, x.z <= y.z)
  def lessThanEqual(x: vec4, y: vec4): bvec4   = bvec4(x.x <= y.x, x.y <= y.y, x.z <= y.z, x.w <= y.w)
  def lessThanEqual(x: ivec2, y: ivec2): bvec2 = bvec2(x.x <= y.x, x.y <= y.y)
  def lessThanEqual(x: ivec3, y: ivec3): bvec3 = bvec3(x.x <= y.x, x.y <= y.y, x.z <= y.z)
  def lessThanEqual(x: ivec4, y: ivec4): bvec4 = bvec4(x.x <= y.x, x.y <= y.y, x.z <= y.z, x.w <= y.w)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/log.xhtml
  def log(x: Float): Float = Math.log(x.toDouble).toFloat
  def log(x: vec2): vec2   = vec2(log(x.x), log(x.y))
  def log(x: vec3): vec3   = vec3(log(x.x), log(x.y), log(x.z))
  def log(x: vec4): vec4   = vec4(log(x.x), log(x.y), log(x.z), log(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/log2.xhtml
  def log2(x: Float): Float = (Math.log(x.toDouble) / Math.log(2)).toFloat
  def log2(x: vec2): vec2   = vec2(log2(x.x), log2(x.y))
  def log2(x: vec3): vec3   = vec3(log2(x.x), log2(x.y), log2(x.z))
  def log2(x: vec4): vec4   = vec4(log2(x.x), log2(x.y), log2(x.z), log2(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/matrixCompMult.xhtml
  def matrixCompMult(x: mat2, y: mat2): mat2 =
    val listA = x.mat
    val listB = y.mat

    val a00 = listA(0 * 2 + 0)
    val a01 = listA(0 * 2 + 1)
    val a10 = listA(1 * 2 + 0)
    val a11 = listA(1 * 2 + 1)

    val b00 = listB(0 * 2 + 0)
    val b01 = listB(0 * 2 + 1)
    val b10 = listB(1 * 2 + 0)
    val b11 = listB(1 * 2 + 1)

    mat2(
      a00 * b00 + a01 * b10,
      a00 * b01 + a01 * b11,
      a10 * b00 + a11 * b10,
      a10 * b01 + a11 * b11
    )
  def matrixCompMult(x: mat3, y: mat3): mat3 =
    val listA = x.mat
    val listB = y.mat

    val a00 = listA(0 * 3 + 0)
    val a01 = listA(0 * 3 + 1)
    val a02 = listA(0 * 3 + 2)
    val a10 = listA(1 * 3 + 0)
    val a11 = listA(1 * 3 + 1)
    val a12 = listA(1 * 3 + 2)
    val a20 = listA(2 * 3 + 0)
    val a21 = listA(2 * 3 + 1)
    val a22 = listA(2 * 3 + 2)

    val b00 = listB(0 * 3 + 0)
    val b01 = listB(0 * 3 + 1)
    val b02 = listB(0 * 3 + 2)
    val b10 = listB(1 * 3 + 0)
    val b11 = listB(1 * 3 + 1)
    val b12 = listB(1 * 3 + 2)
    val b20 = listB(2 * 3 + 0)
    val b21 = listB(2 * 3 + 1)
    val b22 = listB(2 * 3 + 2)

    mat3(
      a00 * b00 + a01 * b10 + a02 * b20,
      a00 * b01 + a01 * b11 + a02 * b21,
      a00 * b02 + a01 * b12 + a02 * b22,
      a10 * b00 + a11 * b10 + a12 * b20,
      a10 * b01 + a11 * b11 + a12 * b21,
      a10 * b02 + a11 * b12 + a12 * b22,
      a20 * b00 + a21 * b10 + a22 * b20,
      a20 * b01 + a21 * b11 + a22 * b21,
      a20 * b02 + a21 * b12 + a22 * b22
    )
  def matrixCompMult(x: mat4, y: mat4): mat4 =
    val listA = x.mat
    val listB = y.mat

    val a00 = listA(0 * 4 + 0)
    val a01 = listA(0 * 4 + 1)
    val a02 = listA(0 * 4 + 2)
    val a03 = listA(0 * 4 + 3)
    val a10 = listA(1 * 4 + 0)
    val a11 = listA(1 * 4 + 1)
    val a12 = listA(1 * 4 + 2)
    val a13 = listA(1 * 4 + 3)
    val a20 = listA(2 * 4 + 0)
    val a21 = listA(2 * 4 + 1)
    val a22 = listA(2 * 4 + 2)
    val a23 = listA(2 * 4 + 3)
    val a30 = listA(3 * 4 + 0)
    val a31 = listA(3 * 4 + 1)
    val a32 = listA(3 * 4 + 2)
    val a33 = listA(3 * 4 + 3)

    val b00 = listB(0 * 4 + 0)
    val b01 = listB(0 * 4 + 1)
    val b02 = listB(0 * 4 + 2)
    val b03 = listB(0 * 4 + 3)
    val b10 = listB(1 * 4 + 0)
    val b11 = listB(1 * 4 + 1)
    val b12 = listB(1 * 4 + 2)
    val b13 = listB(1 * 4 + 3)
    val b20 = listB(2 * 4 + 0)
    val b21 = listB(2 * 4 + 1)
    val b22 = listB(2 * 4 + 2)
    val b23 = listB(2 * 4 + 3)
    val b30 = listB(3 * 4 + 0)
    val b31 = listB(3 * 4 + 1)
    val b32 = listB(3 * 4 + 2)
    val b33 = listB(3 * 4 + 3)

    mat4(
      a00 * b00 + a01 * b10 + a02 * b20 + a03 * b30,
      a00 * b01 + a01 * b11 + a02 * b21 + a03 * b31,
      a00 * b02 + a01 * b12 + a02 * b22 + a03 * b32,
      a00 * b03 + a01 * b13 + a02 * b23 + a03 * b33,
      a10 * b00 + a11 * b10 + a12 * b20 + a13 * b30,
      a10 * b01 + a11 * b11 + a12 * b21 + a13 * b31,
      a10 * b02 + a11 * b12 + a12 * b22 + a13 * b32,
      a10 * b03 + a11 * b13 + a12 * b23 + a13 * b33,
      a20 * b00 + a21 * b10 + a22 * b20 + a23 * b30,
      a20 * b01 + a21 * b11 + a22 * b21 + a23 * b31,
      a20 * b02 + a21 * b12 + a22 * b22 + a23 * b32,
      a20 * b03 + a21 * b13 + a22 * b23 + a23 * b33,
      a30 * b00 + a31 * b10 + a32 * b20 + a33 * b30,
      a30 * b01 + a31 * b11 + a32 * b21 + a33 * b31,
      a30 * b02 + a31 * b12 + a32 * b22 + a33 * b32,
      a30 * b03 + a31 * b13 + a32 * b23 + a33 * b33
    )

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

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/not.xhtml
  def not(x: bvec2): bvec2 = bvec2(!x.x, !x.y)
  def not(x: bvec3): bvec3 = bvec3(!x.x, !x.y, !x.z)
  def not(x: bvec4): bvec4 = bvec4(!x.x, !x.y, !x.z, !x.w)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/notEqual.xhtml
  def notEqual(x: vec2, y: vec2): bvec2   = bvec2(x.x != y.x, x.y != y.y)
  def notEqual(x: vec3, y: vec3): bvec3   = bvec3(x.x != y.x, x.y != y.y, x.z != y.z)
  def notEqual(x: vec4, y: vec4): bvec4   = bvec4(x.x != y.x, x.y != y.y, x.z != y.z, x.w != y.w)
  def notEqual(x: ivec2, y: ivec2): bvec2 = bvec2(x.x != y.x, x.y != y.y)
  def notEqual(x: ivec3, y: ivec3): bvec3 = bvec3(x.x != y.x, x.y != y.y, x.z != y.z)
  def notEqual(x: ivec4, y: ivec4): bvec4 = bvec4(x.x != y.x, x.y != y.y, x.z != y.z, x.w != y.w)

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/pow.xhtml
  def pow(x: Float, y: Float): Float = Math.pow(x.toDouble, y.toDouble).toFloat
  def pow(x: vec2, y: vec2): vec2    = vec2(pow(x.x, y.x), pow(x.y, y.y))
  def pow(x: vec3, y: vec3): vec3    = vec3(pow(x.x, y.x), pow(x.y, y.y), pow(x.z, y.z))
  def pow(x: vec4, y: vec4): vec4    = vec4(pow(x.x, y.x), pow(x.y, y.y), pow(x.z, y.z), pow(x.w, y.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/radians.xhtml
  def radians(degrees: Float): Float = ((Math.PI * degrees) / 180.0).toFloat
  def radians(degrees: vec2): vec2   = vec2(radians(degrees.x), radians(degrees.y))
  def radians(degrees: vec3): vec3   = vec3(radians(degrees.x), radians(degrees.y), radians(degrees.z))
  def radians(degrees: vec4): vec4 =
    vec4(radians(degrees.x), radians(degrees.y), radians(degrees.z), radians(degrees.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/reflect.xhtml
  def reflect(i: Float, n: Float): Float = i - 2.0f * dot(n, i) * n
  def reflect(i: vec2, n: vec2): vec2    = i - 2.0f * dot(n, i) * n
  def reflect(i: vec3, n: vec3): vec3    = i - 2.0f * dot(n, i) * n
  def reflect(i: vec4, n: vec4): vec4    = i - 2.0f * dot(n, i) * n

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/refract.xhtml
  def refract(i: Float, n: Float, eta: Float): Float =
    val k = 1.0f - eta * eta * (1.0f - dot(n, i) * dot(n, i))
    if k < 0.0f then 0.0f
    else eta * i - (eta * dot(n, i) + sqrt(k)) * n
  def refract(i: vec2, n: vec2, eta: Float): vec2 = vec2(refract(i.x, n.x, eta), refract(i.y, n.y, eta))
  def refract(i: vec3, n: vec3, eta: Float): vec3 =
    vec3(refract(i.x, n.x, eta), refract(i.y, n.y, eta), refract(i.z, n.z, eta))
  def refract(i: vec4, n: vec4, eta: Float): vec4 =
    vec4(refract(i.x, n.x, eta), refract(i.y, n.y, eta), refract(i.z, n.z, eta), refract(i.w, n.w, eta))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/round.xhtml
  def round(x: Float): Float = Math.round(x.toDouble).toFloat
  def round(x: vec2): vec2   = vec2(round(x.x), round(x.y))
  def round(x: vec3): vec3   = vec3(round(x.x), round(x.y), round(x.z))
  def round(x: vec4): vec4   = vec4(round(x.x), round(x.y), round(x.z), round(x.w))

  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/sign.xhtml
  def sign(x: Float): Float = if x < 0.0f then -1.0f else if x > 0.0f then 1.0f else 0.0f
  def sign(x: vec2): vec2   = vec2(sign(x.x), sign(x.y))
  def sign(x: vec3): vec3   = vec3(sign(x.x), sign(x.y), sign(x.z))
  def sign(x: vec4): vec4   = vec4(sign(x.x), sign(x.y), sign(x.z), sign(x.w))
  def sign(x: Int): Int     = if x < 0 then -1 else if x > 0 then 1 else 0
  def sign(x: ivec2): ivec2 = ivec2(sign(x.x), sign(x.y))
  def sign(x: ivec3): ivec3 = ivec3(sign(x.x), sign(x.y), sign(x.z))
  def sign(x: ivec4): ivec4 = ivec4(sign(x.x), sign(x.y), sign(x.z), sign(x.w))

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
