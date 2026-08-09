package indigo.core.datatypes.mutable

import indigo.core.datatypes.Matrix4
import indigoengine.shared.datatypes.Radians

/*
In these tests, Matrix4 is acting as the reference implementation.
The implementations are, however, intentionally not identical, so
we're saying that the cheap version needs to say what the real one
says, where applicable. Full multiplication for example, isn't
identical (but that's not a problem for our use case).
 */
class CheapMatrix4Tests extends munit.FunSuite {

  test("identity") {

    val expected =
      Matrix4.identity

    assert(clue(CheapMatrix4.identity.toMatrix4) ~== clue(expected))

  }

  test("translate x") {

    val expected =
      Matrix4.identity.translate(2.0, 0, 0)

    assert(clue(CheapMatrix4.identity.translate(2.0, 0, 0).toMatrix4) ~== clue(expected))

  }

  test("translate y") {

    val expected =
      Matrix4.identity.translate(0, 2.0, 0)

    assert(clue(CheapMatrix4.identity.translate(0, 2.0, 0).toMatrix4) ~== clue(expected))

  }

  test("translate z") {

    val expected =
      Matrix4.identity.translate(0, 0, 2.0)

    assert(clue(CheapMatrix4.identity.translate(0, 0, 2.0).toMatrix4) ~== clue(expected))

  }

  test("rotation") {

    val expected =
      Matrix4.identity.rotate(Radians.PI)

    assert(clue(CheapMatrix4.identity.rotate(Radians.PI.toFloat).toMatrix4) ~== clue(expected))
  }

  test("scale") {

    val expected =
      Matrix4.identity.scale(2.0, 3.0, 1.0)

    assert(clue(CheapMatrix4.identity.scale(2.0, 3.0, 1.0).toMatrix4) ~== clue(expected))

  }

  test("a more realistic transformation") {

    val expected =
      Matrix4.identity
        .scale(2.0, 3.0, 1.0)
        .rotate(Radians.TAUby4)
        .translate(100, 0.0, 0.0)
        .rotate(Radians.TAUby2)

    val actual =
      CheapMatrix4.identity
        .scale(2.0, 3.0, 1.0)
        .rotate(Radians.TAUby4.toFloat)
        .translate(100, 0.0, 0.0)
        .rotate(Radians.TAUby2.toFloat)

    assert(clue(actual.toMatrix4) ~== clue(expected))

  }

  test("orthographic") {

    val expected =
      Matrix4.orthographic(320, 240)

    val actual =
      CheapMatrix4.orthographic(320, 240)

    assert(clue(actual.toMatrix4) ~== clue(expected))

  }

  /*
  Below, the generic `*` acts as the reference implementation. `translate`, `rotate` and
  `scale` are specialised forms of multiplying by a mostly-identity matrix, so each one has
  to agree with what `*` would have produced given the matrix it stands in for.

  Both `*` and the specialised methods mutate their receiver, so every assertion builds two
  separate subjects. The subjects are `def`s for the same reason.
   */

  def translationMatrix(byX: Float, byY: Float, byZ: Float): CheapMatrix4 =
    CheapMatrix4((1, 0, 0, 0), (0, 1, 0, 0), (0, 0, 1, 0), (byX, byY, byZ, 1))

  def rotationMatrix(angle: Float): CheapMatrix4 =
    val c = Math.cos(angle).toFloat
    val s = Math.sin(angle).toFloat
    CheapMatrix4((c, s, 0, 0), (-s, c, 0, 0), (0, 0, 1, 0), (0, 0, 0, 1))

  def scaleMatrix(byX: Float, byY: Float, byZ: Float): CheapMatrix4 =
    CheapMatrix4((byX, 0, 0, 0), (0, byY, 0, 0), (0, 0, byZ, 0), (0, 0, 0, 1))

  // `translate` reads the w column, which `*` never writes, so a subject with a non-trivial w
  // column is the case that catches an implementation assuming it is always (0, 0, 1).
  val subjects: List[(String, () => CheapMatrix4)] =
    List(
      "identity"      -> (() => CheapMatrix4.identity),
      "orthographic"  -> (() => CheapMatrix4.orthographic(320, 240)),
      "full w column" -> (() => CheapMatrix4((1, 2, 3, 4), (5, 6, 7, 8), (9, 10, 11, 12), (13, 14, 15, 16))),
      "chained"       -> (() => CheapMatrix4.identity.scale(2, 3, 1).rotate(Radians.TAUby4.toFloat))
    )

  val translations: List[(Float, Float, Float)] =
    List((0, 0, 0), (2, 0, 0), (0, 2, 0), (0, 0, 2), (10, -5, 3), (-1, -2, -3))

  val rotations: List[Float] =
    List(0.0f, Radians.TAUby4.toFloat, Radians.TAUby2.toFloat, Radians.PI.toFloat, 0.7f, -1.3f)

  val scales: List[(Float, Float, Float)] =
    List((1, 1, 1), (2, 3, 1), (0, 1, 1), (-1, 2, -3), (0.5f, 0.25f, 2))

  subjects.foreach { case (name, subject) =>

    translations.foreach { case (x, y, z) =>
      test(s"translate matches multiplication - $name - ($x, $y, $z)") {

        val expected = subject() * translationMatrix(x, y, z)
        val actual   = subject().translate(x, y, z)

        assert(clue(actual.toMatrix4) ~== clue(expected.toMatrix4))

      }
    }

    rotations.foreach { angle =>
      test(s"rotate matches multiplication - $name - $angle") {

        val expected = subject() * rotationMatrix(angle)
        val actual   = subject().rotate(angle)

        assert(clue(actual.toMatrix4) ~== clue(expected.toMatrix4))

      }
    }

    scales.foreach { case (x, y, z) =>
      test(s"scale matches multiplication - $name - ($x, $y, $z)") {

        val expected = subject() * scaleMatrix(x, y, z)
        val actual   = subject().scale(x, y, z)

        assert(clue(actual.toMatrix4) ~== clue(expected.toMatrix4))

      }
    }

  }

  test("transformations mutate and return the same instance") {

    val m = CheapMatrix4.identity

    assert(m.translate(1, 2, 3).toFloat32Array eq m.toFloat32Array)
    assert(m.rotate(0.5f).toFloat32Array eq m.toFloat32Array)
    assert(m.scale(2, 2, 2).toFloat32Array eq m.toFloat32Array)

  }

}
