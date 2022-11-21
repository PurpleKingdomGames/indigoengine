package ultraviolet.datatypes

import scala.reflect.ClassTag

trait ShaderDSLTypes:

  final case class vec2(x: Float, y: Float):
    def +(f: Float): vec2 = vec2(x + f, y + f)
    def -(f: Float): vec2 = vec2(x - f, y - f)
    def *(f: Float): vec2 = vec2(x * f, y * f)
    def /(f: Float): vec2 = vec2(x / f, y / f)

    def +(v: vec2): vec2 = vec2(x + v.x, y + v.y)
    def -(v: vec2): vec2 = vec2(x - v.x, y - v.y)
    def *(v: vec2): vec2 = vec2(x * v.x, y * v.y)
    def /(v: vec2): vec2 = vec2(x / v.x, y / v.y)

    def `unary_-` : vec2 = vec2(-x, -y)

    def *(v: mat2): vec2 = vec2(0.0) // TODO: Replace stub

  object vec2:
    inline def apply(xy: Float): vec2 =
      vec2(xy, xy)

  final case class vec3(x: Float, y: Float, z: Float):
    def +(f: Float): vec3 = vec3(x + f, y + f, z + f)
    def -(f: Float): vec3 = vec3(x - f, y - f, z - f)
    def *(f: Float): vec3 = vec3(x * f, y * f, z * f)
    def /(f: Float): vec3 = vec3(x / f, y / f, z / f)

    def +(v: vec3): vec3 = vec3(x + v.x, y + v.y, z + v.z)
    def -(v: vec3): vec3 = vec3(x - v.x, y - v.y, z - v.z)
    def *(v: vec3): vec3 = vec3(x * v.x, y * v.y, z * v.z)
    def /(v: vec3): vec3 = vec3(x / v.x, y / v.y, z / v.z)

    def `unary_-` : vec3 = vec3(-x, -y, -z)

    def *(v: mat3): vec3 = vec3(0.0) // TODO: Replace stub

  object vec3:
    inline def apply(xyz: Float): vec3 =
      vec3(xyz, xyz, xyz)

    inline def apply(xy: vec2, z: Float): vec3 =
      vec3(xy.x, xy.y, z)

    inline def apply(x: Float, yz: vec2): vec3 =
      vec3(x, yz.x, yz.y)

  final case class vec4(x: Float, y: Float, z: Float, w: Float):
    def +(f: Float): vec4 = vec4(x + f, y + f, z + f, w + f)
    def -(f: Float): vec4 = vec4(x - f, y - f, z - f, w - f)
    def *(f: Float): vec4 = vec4(x * f, y * f, z * f, w * f)
    def /(f: Float): vec4 = vec4(x / f, y / f, z / f, w / f)

    def +(v: vec4): vec4 = vec4(x + v.x, y + v.y, z + v.z, w + v.w)
    def -(v: vec4): vec4 = vec4(x - v.x, y - v.y, z - v.z, w - v.w)
    def *(v: vec4): vec4 = vec4(x * v.x, y * v.y, z * v.z, w * v.w)
    def /(v: vec4): vec4 = vec4(x / v.x, y / v.y, z / v.z, w / v.w)

    def `unary_-` : vec4 = vec4(-x, -y, -z, -w)

    def *(v: mat4): vec4 = vec4(0.0) // TODO: Replace stub

  object vec4:
    inline def apply(xy: vec2, z: Float, w: Float): vec4 =
      vec4(xy.x, xy.y, z, w)

    inline def apply(x: Float, yz: vec2, w: Float): vec4 =
      vec4(x, yz.x, yz.y, w)

    inline def apply(x: Float, y: Float, zw: vec2): vec4 =
      vec4(x, y, zw.x, zw.y)

    inline def apply(xy: vec2, zw: vec2): vec4 =
      vec4(xy.x, xy.y, zw.x, zw.y)

    inline def apply(xyz: vec3, w: Float): vec4 =
      vec4(xyz.x, xyz.y, xyz.z, w)

    inline def apply(x: Float, yzw: vec3): vec4 =
      vec4(x, yzw.x, yzw.y, yzw.z)

    inline def apply(xyz: Float): vec4 =
      vec4(xyz, xyz, xyz, xyz)

  case object sampler2D
  case object samplerCube

  final case class array[T, L <: Singleton](private val size: L, private val arr: Array[T])(using convert: L => Int):
    def apply(index: Int): T = arr(index)
    def length: Int          = convert(size)
    def update(i: Int, value: T): Unit =
      arr(i) = value

  object array:
    def apply[T: ClassTag, L <: Singleton](size: L)(using convert: L => Int): array[T, L] =
      array[T, L](size, new Array[T](convert(size)))

  final case class mat2(mat: Array[Float]):
    def apply(index: Int): Float = mat(index)
    def update(i: Int, value: Float): Unit =
      mat(i) = value
    def update(i: Int, value: vec2): Unit =
      mat(i + 0) = value.x
      mat(i + 1) = value.y
  object mat2:
    def apply(m0: Float, m1: Float, m2: Float, m3: Float): mat2 =
      mat2(Array(m0, m1, m2, m3))

  final case class mat3(mat: Array[Float]):
    def apply(index: Int): Float = mat(index)
    def update(i: Int, value: Float): Unit =
      mat(i) = value
    def update(i: Int, value: vec3): Unit =
      mat(i + 0) = value.x
      mat(i + 1) = value.y
      mat(i + 2) = value.z
  object mat3:
    // format: off
    def apply(
      m0: Float, m1: Float, m2: Float,
      m3: Float, m4: Float, m5: Float,
      m6: Float, m7: Float, m8: Float,
    ): mat3 =
      mat3(
        Array(
          m0, m1, m2,
          m3, m4, m5,
          m6, m7, m8
        )
      )

  final case class mat4(mat: Array[Float]):
    def apply(index: Int): Float = mat(index)
    def update(i: Int, value: Float): Unit =
      mat(i) = value
    def update(i: Int, value: vec4): Unit =
      mat(i + 0) = value.x
      mat(i + 1) = value.y
      mat(i + 2) = value.z
      mat(i + 3) = value.w
  object mat4:
    // format: off
    def apply(
       m0: Float,  m1: Float,  m2: Float,  m3: Float,
       m4: Float,  m5: Float,  m6: Float,  m7: Float,
       m8: Float,  m9: Float, m10: Float, m11: Float,
      m12: Float, m13: Float, m14: Float, m15: Float,
    ): mat4 =
      mat4(
        Array(
           m0,  m1,  m2,  m3,
           m4,  m5,  m6,  m7,
           m8,  m9, m10, m11,
          m12, m13, m14, m15,
        )
      )
