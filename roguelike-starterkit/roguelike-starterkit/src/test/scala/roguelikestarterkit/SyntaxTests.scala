package roguelikestarterkit

import indigo.*
import roguelikestarterkit.syntax.toPoints

class SyntaxTests extends munit.FunSuite {
  
  test("Can convert a rectangle to a grid of points") {
    val actual = Rectangle(0,0,4,3).toPoints

    val expected =
      Batch(
        Point(0, 0),
        Point(1, 0),
        Point(2, 0),
        Point(3, 0),
        Point(0, 1),
        Point(1, 1),
        Point(2, 1),
        Point(3, 1),
        Point(0, 2),
        Point(1, 2),
        Point(2, 2),
        Point(3, 2),
      )

    assertEquals(actual, expected)
  }
  
  test("Can convert a offset rectangle to a grid of points") {
    val actual = Rectangle(10,20,4,3).toPoints

    val expected =
      Batch(
        Point(10 + 0, 20 + 0),
        Point(10 + 1, 20 + 0),
        Point(10 + 2, 20 + 0),
        Point(10 + 3, 20 + 0),
        Point(10 + 0, 20 + 1),
        Point(10 + 1, 20 + 1),
        Point(10 + 2, 20 + 1),
        Point(10 + 3, 20 + 1),
        Point(10 + 0, 20 + 2),
        Point(10 + 1, 20 + 2),
        Point(10 + 2, 20 + 2),
        Point(10 + 3, 20 + 2),
      )

    assertEquals(actual, expected)
  }

}
