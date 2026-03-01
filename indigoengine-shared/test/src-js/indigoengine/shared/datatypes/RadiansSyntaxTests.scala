package indigoengine.shared.datatypes

class RadiansSyntaxTests extends munit.FunSuite {

  test("Radians can do simple math with Doubles") {
    assertEquals(Radians(10) + 2, Radians(12))
    assertEquals(Radians(10) - 2, Radians(8))
    assertEquals(Radians(10) * 2, Radians(20))
    assertEquals(Radians(10) / 2, Radians(5))
  }

}
