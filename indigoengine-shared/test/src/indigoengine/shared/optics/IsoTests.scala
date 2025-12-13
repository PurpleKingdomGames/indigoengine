package indigoengine.shared.optics

class IsoTests extends munit.FunSuite {

  final case class Z(value: Long)
  final case class A(value: Int)
  final case class B(value: Long)
  final case class C(value: Int)

  val isoAB: Iso[A, B] =
    Iso(a => B(a.value.toLong), b => A(b.value.toInt))
  val isoBC: Iso[B, C] =
    Iso(b => C(b.value.toInt), c => B(c.value.toLong))

  test("basic usage") {
    assertEquals(isoAB.to(A(10)), B(10L))
    assertEquals(isoAB.from(B(10L)), A(10))
  }

  test("identity") {
    val iso = Iso.identity[Int]

    assertEquals(iso.to(10), 10)
    assertEquals(iso.from(10), 10)
  }

  test("reverse") {
    assertEquals(isoAB.reverse.to(B(10L)), A(10))
    assertEquals(isoAB.reverse.from(A(10)), B(10L))
  }

  test("andThen") {
    assertEquals((isoAB >=> isoBC).to(A(10)), C(10))
    assertEquals((isoAB >=> isoBC).from(C(10)), A(10))
  }

  test("compose") {
    assertEquals((isoBC <=< isoAB).to(A(10)), C(10))
    assertEquals((isoBC <=< isoAB).from(C(10)), A(10))
  }

  test("modify") {
    assertEquals(isoAB.modify(b => B(b.value * 2))(A(10)), A(20))
  }

}
