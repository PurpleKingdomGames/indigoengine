package indigo.core.render

class MagnificationTests extends munit.FunSuite {

  test("apply clamps to range") {
    assertEquals(Magnification(3).toInt, 3)

    assertEquals(Magnification(0).toInt, 1)
    assertEquals(Magnification(-5).toInt, 1)

    assertEquals(Magnification(17).toInt, 16)
    assertEquals(Magnification(100).toInt, 16)
  }

  test("named magnifications") {
    assertEquals(Magnification.x1.toInt, 1)
    assertEquals(Magnification.x2.toInt, 2)
    assertEquals(Magnification.x3.toInt, 3)
    assertEquals(Magnification.x4.toInt, 4)

    assertEquals(Magnification.default.toInt, 1)
    assertEquals(Magnification.Min.toInt, 1)
    assertEquals(Magnification.Max.toInt, 16)
  }

  test("increase") {
    assertEquals(Magnification.x1.increase, Magnification.x2)
    assertEquals(Magnification.x3.increase.toInt, 4)

    assertEquals(Magnification.Max.increase, Magnification.Max)
  }

  test("decrease") {
    assertEquals(Magnification.x4.decrease.toInt, 3)
    assertEquals(Magnification.x2.decrease, Magnification.x1)

    assertEquals(Magnification.Min.decrease, Magnification.Min)
  }

  test("addition") {
    assertEquals((Magnification.x2 + Magnification.x3).toInt, 5)
    assertEquals((Magnification.x2 + 3).toInt, 5)

    assertEquals((Magnification(10) + Magnification(10)).toInt, 16)
    assertEquals((Magnification(10) + 10).toInt, 16)
  }

  test("subtraction") {
    assertEquals((Magnification.x4 - Magnification.x1).toInt, 3)
    assertEquals((Magnification.x4 - 1).toInt, 3)

    assertEquals((Magnification.x2 - Magnification.x4).toInt, 1)
    assertEquals((Magnification.x2 - 4).toInt, 1)
  }

  test("multiplication") {
    assertEquals((Magnification.x2 * Magnification.x3).toInt, 6)
    assertEquals((Magnification.x2 * 3).toInt, 6)

    assertEquals((Magnification.x4 * Magnification.x4).toInt, 16)
    assertEquals((Magnification(8) * 4).toInt, 16)
  }

  test("division") {
    assertEquals((Magnification(8) / Magnification.x2).toInt, 4)
    assertEquals((Magnification(8) / 2).toInt, 4)

    assertEquals((Magnification.x2 / Magnification.x4).toInt, 1)
    assertEquals((Magnification.x2 / 4).toInt, 1)

    // Divide by zero guard
    assertEquals((Magnification(8) / 0).toInt, 8)
  }

}
