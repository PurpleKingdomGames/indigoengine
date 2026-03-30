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

  test("modify rectangle") {

    val iso: Iso[Rectangle, (Point, Point)] =
      Iso(
        (r: Rectangle) => (r.topLeft, r.bottomRight),
        (t: (Point, Point)) => Rectangle.fromPoints(t._1, t._2)
      )

    def translate(by: Point) =
      (pts: (Point, Point)) => (pts._1.moveBy(by), pts._2.moveBy(by))

    val actual =
      iso.modify(translate(Point(20, 30)))(Rectangle(10, 10, 10, 10))

    val expected =
      Rectangle(30, 40, 10, 10)

    assertEquals(actual, expected)
  }

  final case class Point(x: Int, y: Int):
    def moveBy(pt: Point): Point =
      this.copy(
        x = x + pt.x,
        y = y + pt.y
      )
    def toSize: Size =
      Size(x, y)
    def -(other: Point): Point =
      moveBy(other.invert)
    def +(other: Point): Point =
      moveBy(other)
    def invert: Point =
      this.copy(
        x = -x,
        y = -y
      )
  final case class Size(w: Int, h: Int):
    def toPoint: Point =
      Point(w, h)
  final case class Rectangle(p: Point, s: Size):
    def topLeft: Point     = p
    def bottomRight: Point = p + s.toPoint
  object Rectangle:
    def apply(x: Int, y: Int, w: Int, h: Int): Rectangle =
      Rectangle(Point(x, y), Size(w, h))

    def fromPoints(pt1: Point, pt2: Point): Rectangle =
      val x = Math.min(pt1.x, pt2.x)
      val y = Math.min(pt1.y, pt2.y)
      val w = Math.max(pt1.x, pt2.x) - x
      val h = Math.max(pt1.y, pt2.y) - y

      Rectangle(x, y, w, h)

}
