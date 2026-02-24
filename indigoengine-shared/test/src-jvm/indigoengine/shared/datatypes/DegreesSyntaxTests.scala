package indigoengine.shared.datatypes

class DegreesSyntaxTests extends munit.FunSuite {
  
  test("Degrees can do simple math with Doubles") {
    assertEquals(Degrees(10) + 2, Degrees(12))
    assertEquals(Degrees(10) - 2, Degrees(8))
    assertEquals(Degrees(10) * 2, Degrees(20))
    assertEquals(Degrees(10) / 2, Degrees(5))
  }

}
