package indigo.scenegraph

import indigo.scenegraph.registers.AnimationsRegister
import indigo.scenegraph.registers.BoundaryLocator
import indigo.scenegraph.registers.FontRegister
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Point
import indigo.core.datatypes.Fill
import indigo.core.datatypes.Stroke
import indigoengine.shared.datatypes.RGBA
import indigoengine.shared.collections.Batch

class ShapeTests extends munit.FunSuite:

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, new FontRegister)

  test("Bounds calculation - box") {

    val s: Shape.Box =
      Shape.Box(
        dimensions = Rectangle(15, 25, 100, 200),
        fill = Fill.None,
        stroke = Stroke(8, RGBA.Red)
      )

    val actual =
      boundaryLocator.findBounds(s)

    val expected =
      Option(Rectangle(15 - 4, 25 - 4, 100 + 8, 200 + 8))

    assertEquals(actual, expected)
  }

  test("Bounds calculation - circle") {

    val s: Shape.Circle =
      Shape.Circle(
        center = Point(50, 50),
        radius = 17,
        fill = Fill.None,
        stroke = Stroke(7, RGBA.Red)
      )

    val actual =
      boundaryLocator.findBounds(s)

    val expected =
      Option(Rectangle(50 - 17 - 3, 50 - 17 - 3, 17 + 17 + 7, 17 + 17 + 7))

    assertEquals(actual, expected)
  }

  test("Bounds calculation - line") {
    // There is an almost duplicate of this test in BoundaryLocatorTests
    // that has a better explaination of the calculation.

    val s: Shape.Line =
      Shape.Line(
        start = Point(50, 10),
        end = Point(75, 60),
        stroke = Stroke(5, RGBA.Red)
      )

    val actual =
      boundaryLocator.findBounds(s)

    val expected =
      Option(Rectangle(50 - 2, 10 - 2, 25 + 5 + 2, 50 + 5 + 2))

    assertEquals(actual, expected)
  }

  test("Bounds calculation - polygon") {

    val verts =
      Batch(
        Point(50, 10),
        Point(75, 60),
        Point(25, 60)
      )

    val s: Shape.Polygon =
      Shape.Polygon(
        vertices = verts,
        fill = Fill.None,
        stroke = Stroke(4, RGBA.Red)
      )

    val actual =
      boundaryLocator.findBounds(s)

    val expected =
      Option(Rectangle(25 - 2, 10 - 2, 50 + 4, 50 + 4))

    assertEquals(actual, expected)

  }
