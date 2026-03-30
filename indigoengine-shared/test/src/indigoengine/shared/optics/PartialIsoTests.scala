package indigoengine.shared.optics

class PartialIsoTests extends munit.FunSuite {

  final case class Z(value: Long)
  final case class A(value: Int)
  final case class B(value: Long)
  final case class C(value: Int)

  val isoAB: PartialIso[A, B] =
    PartialIso(a => Some(B(a.value.toLong)), b => Some(A(b.value.toInt)))
  val isoBC: PartialIso[B, C] =
    PartialIso(b => Some(C(b.value.toInt)), c => Some(B(c.value.toLong)))

  test("basic usage") {
    assertEquals(isoAB.to(A(10)), Some(B(10L)))
    assertEquals(isoAB.from(B(10L)), Some(A(10)))
  }

  test("basic usage - filtered") {
    val isoAB: PartialIso[A, B] =
      PartialIso(a => Some(B(a.value.toLong)), _ => None)

    assertEquals(isoAB.to(A(10)), Some(B(10L)))
    assertEquals(isoAB.from(B(10L)), None)
  }

  test("identity") {
    val iso = PartialIso.identity[Int]

    assertEquals(iso.to(10), Some(10))
    assertEquals(iso.from(10), Some(10))
  }

  test("reverse") {
    assertEquals(isoAB.reverse.to(B(10L)), Some(A(10)))
    assertEquals(isoAB.reverse.from(A(10)), Some(B(10L)))
  }

  test("andThen") {
    assertEquals((isoAB >=> isoBC).to(A(10)), Some(C(10)))
    assertEquals((isoAB >=> isoBC).from(C(10)), Some(A(10)))
  }

  test("compose") {
    assertEquals((isoBC <=< isoAB).to(A(10)), Some(C(10)))
    assertEquals((isoBC <=< isoAB).from(C(10)), Some(A(10)))
  }

}
