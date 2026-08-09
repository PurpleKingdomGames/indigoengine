package indigo.core.datatypes.mutable

import indigo.core.datatypes.Matrix4
import indigo.core.datatypes.Vector3

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

/** `CheapMatrix4` is intended for use internally within Indigo, but remains available for general use. You are advised
  * to use `Matrix4` generally. `CheapMatrix4` carries over much of the functionality of `Matrix4` but is based on
  * mutable data for performance reasons, and takes some shortcuts during multiplication to reduce work based on how the
  * engine itself behaves.
  */
opaque type CheapMatrix4 = Float32Array

object CheapMatrix4:

  extension (m: CheapMatrix4)
    def x: Float = m(12)
    def y: Float = m(13)

    // Multiplying by a translation matrix, which is the identity apart from row 3. Every term of the general
    // multiply is against a 1 or a 0 except the w column, so each cell reduces to itself plus its row's w.
    def translate(byX: Float, byY: Float, byZ: Float): CheapMatrix4 =
      val a03 = m(3)
      val a13 = m(7)
      val a33 = m(15)

      m(0) = m(0) + a03 * byX
      m(1) = m(1) + a03 * byY
      m(2) = m(2) + a03 * byZ

      m(4) = m(4) + a13 * byX
      m(5) = m(5) + a13 * byY
      m(6) = m(6) + a13 * byZ

      m(12) = m(12) + a33 * byX
      m(13) = m(13) + a33 * byY
      m(14) = m(14) + a33 * byZ

      m

    // Multiplying by a rotation matrix, which only has non-identity values in the top left 2x2. Column 2 is left
    // untouched on purpose - the general multiply would write it back unchanged. Rows are read up front because
    // each output pair depends on both of its inputs.
    def rotate(angle: Float): CheapMatrix4 =
      val c = Math.cos(angle).toFloat
      val s = Math.sin(angle).toFloat

      val a00 = m(0)
      val a01 = m(1)
      val a10 = m(4)
      val a11 = m(5)
      val a30 = m(12)
      val a31 = m(13)

      m(0) = a00 * c - a01 * s
      m(1) = a00 * s + a01 * c

      m(4) = a10 * c - a11 * s
      m(5) = a10 * s + a11 * c

      m(12) = a30 * c - a31 * s
      m(13) = a30 * s + a31 * c

      m

    // Multiplying by a scale matrix, which is diagonal, so every cell is just scaled by its own column's factor.
    def scale(byX: Float, byY: Float, byZ: Float): CheapMatrix4 =
      m(0) = m(0) * byX
      m(1) = m(1) * byY
      m(2) = m(2) * byZ

      m(4) = m(4) * byX
      m(5) = m(5) * byY
      m(6) = m(6) * byZ

      m(12) = m(12) * byX
      m(13) = m(13) * byY
      m(14) = m(14) * byZ

      m

    def *(other: CheapMatrix4): CheapMatrix4 = {

      // If they are commented out below, it's because we know...
      // ... that those fields aren't used by the engine...
      // ... and by knowing things we can avoid work.

      val listA = m
      val listB = other

      val a00 = listA(0 * 4 + 0)
      val a01 = listA(0 * 4 + 1)
      val a02 = listA(0 * 4 + 2)
      val a03 = listA(0 * 4 + 3)
      val a10 = listA(1 * 4 + 0)
      val a11 = listA(1 * 4 + 1)
      val a12 = listA(1 * 4 + 2)
      val a13 = listA(1 * 4 + 3)
      // val a20 = listA(2 * 4 + 0)
      // val a21 = listA(2 * 4 + 1)
      // val a22 = listA(2 * 4 + 2)
      // val a23 = listA(2 * 4 + 3)
      val a30 = listA(3 * 4 + 0)
      val a31 = listA(3 * 4 + 1)
      val a32 = listA(3 * 4 + 2)
      val a33 = listA(3 * 4 + 3)

      val b00 = listB(0 * 4 + 0)
      val b01 = listB(0 * 4 + 1)
      val b02 = listB(0 * 4 + 2)
      // val b03 = listB(0 * 4 + 3)
      val b10 = listB(1 * 4 + 0)
      val b11 = listB(1 * 4 + 1)
      val b12 = listB(1 * 4 + 2)
      // val b13 = listB(1 * 4 + 3)
      val b20 = listB(2 * 4 + 0)
      val b21 = listB(2 * 4 + 1)
      val b22 = listB(2 * 4 + 2)
      // val b23 = listB(2 * 4 + 3)
      val b30 = listB(3 * 4 + 0)
      val b31 = listB(3 * 4 + 1)
      val b32 = listB(3 * 4 + 2)
      // val b33 = listB(3 * 4 + 3)

      m(0) = a00 * b00 + a01 * b10 + a02 * b20 + a03 * b30
      m(1) = a00 * b01 + a01 * b11 + a02 * b21 + a03 * b31
      m(2) = a00 * b02 + a01 * b12 + a02 * b22 + a03 * b32
      // m(3) = a00 * b03 + a01 * b13 + a02 * b23 + a03 * b33

      m(4) = a10 * b00 + a11 * b10 + a12 * b20 + a13 * b30
      m(5) = a10 * b01 + a11 * b11 + a12 * b21 + a13 * b31
      m(6) = a10 * b02 + a11 * b12 + a12 * b22 + a13 * b32
      // m(7) = a10 * b03 + a11 * b13 + a12 * b23 + a13 * b33

      // m(8) = a20 * b00 + a21 * b10 + a22 * b20 + a23 * b30
      // m(9) = a20 * b01 + a21 * b11 + a22 * b21 + a23 * b31
      // m(10) = a20 * b02 + a21 * b12 + a22 * b22 + a23 * b32
      // m(11) = a20 * b03 + a21 * b13 + a22 * b23 + a23 * b33

      m(12) = a30 * b00 + a31 * b10 + a32 * b20 + a33 * b30
      m(13) = a30 * b01 + a31 * b11 + a32 * b21 + a33 * b31
      m(14) = a30 * b02 + a31 * b12 + a32 * b22 + a33 * b32
      // m(15) = a30 * b03 + a31 * b13 + a32 * b23 + a33 * b33

      m
    }

    inline def toFloat32Array: Float32Array =
      m

    def toMatrix4: Matrix4 =
      Matrix4(m.toArray.map(_.toDouble))

    def deepClone: CheapMatrix4 =
      new Float32Array(m)

    def transform(vector: Vector3): Vector3 =
      val col1: Array[Float] = Array(m(0), m(4), m(8), m(12))
      val col2: Array[Float] = Array(m(1), m(5), m(9), m(13))
      val col3: Array[Float] = Array(m(2), m(6), m(10), m(14))

      Vector3(
        x = col1(0) * vector.x + col1(1) * vector.y + col1(2) * vector.z + col1(3),
        y = col2(0) * vector.x + col2(1) * vector.y + col2(2) * vector.z + col2(3),
        z = col3(0) * vector.x + col3(1) * vector.y + col3(2) * vector.z + col3(3)
      )

  def identity: CheapMatrix4 =
    // Allocates zero's by default.
    val m = new Float32Array(16)
    m(0) = 1
    // m(1) = 0
    // m(2) = 0
    // m(3) = 0
    // m(4) = 0
    m(5) = 1
    // m(6) = 0
    // m(7) = 0
    // m(8) = 0
    // m(9) = 0
    m(10) = 1
    // m(11) = 0
    // m(12) = 0
    // m(13) = 0
    // m(14) = 0
    m(15) = 1
    m

  def orthographic(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): CheapMatrix4 =
    val m = new Float32Array(16)
    m(0) = 2 / (right - left)
    // m(1) = 0
    // m(2) = 0
    // m(3) = 0
    // m(4) = 0
    m(5) = 2 / (top - bottom)
    // m(6) = 0
    // m(7) = 0
    // m(8) = 0
    // m(9) = 0
    m(10) = 2 / (near - far)
    // m(11) = 0
    m(12) = (left + right) / (left - right)
    m(13) = (bottom + top) / (bottom - top)
    m(14) = (near + far) / (near - far)
    m(15) = 1
    m

  def orthographic(width: Float, height: Float): CheapMatrix4 =
    orthographic(0, width, height, 0, -1, Int.MaxValue.toFloat)

  def orthographic(x: Float, y: Float, width: Float, height: Float): CheapMatrix4 =
    orthographic(x, x + width, y + height, y, -1, Int.MaxValue.toFloat)

  inline def apply(matrix: Float32Array): CheapMatrix4 =
    matrix

  /** SHOULD ONLY BE USED BY TESTS
    */
  def apply(
      row0: (Float, Float, Float, Float),
      row1: (Float, Float, Float, Float),
      row2: (Float, Float, Float, Float),
      row3: (Float, Float, Float, Float)
  ): CheapMatrix4 =
    new Float32Array(
      js.Array(row0._1, row0._2, row0._3, row0._4) ++
        js.Array(row1._1, row1._2, row1._3, row1._4) ++
        js.Array(row2._1, row2._2, row2._3, row2._4) ++
        js.Array(row3._1, row3._2, row3._3, row3._4)
    )
