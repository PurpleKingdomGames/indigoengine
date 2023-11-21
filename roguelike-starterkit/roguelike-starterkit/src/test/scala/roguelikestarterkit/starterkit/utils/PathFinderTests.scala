package roguelikestarterkit.utils

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.dice.Dice
import roguelikestarterkit.utils.GridSquare.Blocked
import roguelikestarterkit.utils.GridSquare.Walkable

class PathFinderTests extends munit.FunSuite {

  val scoreAs: GridSquare => Int = _ => 1

  val coords: Point = Point(0, 0)

  test("Finding an unobscured path.should be able to find a route") {
    val start: Point      = Point(2, 1)
    val end: Point        = Point(0, 2)
    val impassable: Point = Point(1, 0)

    val searchGrid = PathFinder.fromImpassable(Rectangle(Size(3, 3)), Batch(impassable))

    val actual   = searchGrid.locatePath(start, end, scoreAs).toList
    val expected = List(start, Point(1, 1), Point(0, 1), end)

    assertEquals(actual, expected)
  }

  test("Finding an unobscured path.should be able to find a route (from walkable)") {
    val start: Point = Point(2, 1)
    val end: Point   = Point(0, 2)
    val walkable: Batch[Point] =
      Batch(
        Point(0, 0),
        // Point(1, 0), // Impassable
        Point(2, 0),
        Point(0, 1),
        Point(1, 1),
        Point(2, 1),
        Point(0, 2),
        Point(1, 2),
        Point(2, 2)
      )

    val searchGrid = PathFinder.fromWalkable(walkable)

    val path: Batch[Point] = searchGrid.locatePath(start, end, scoreAs)

    assertEquals(path.toList, List(start, Point(1, 1), Point(0, 1), end))
  }

  test("Scoring the grid.should be able to score a grid") {
    val start: Point      = Point(2, 1)
    val end: Point        = Point(0, 2)
    val impassable: Point = Point(1, 0)

    val searchGrid = PathFinder.fromImpassable(Rectangle(Size(3, 3)), Batch(impassable))

    val expected: Batch[GridSquare] =
      Batch(
        Walkable(0, Point(0, 0), 2),
        Blocked(1, Point(1, 0)),
        Walkable(
          2,
          Point(2, 0),
          -1
        ), // Unscored squares are returned to keep sampleAt working correctly
        Walkable(3, Point(0, 1), 1),
        Walkable(4, Point(1, 1), 2),
        Walkable(5, Point(2, 1), 3), // start
        Walkable(6, Point(0, 2), 0), // end
        Walkable(7, Point(1, 2), 1),
        Walkable(8, Point(2, 2), 2)
      )

    val actual =
      PathFinder.scoreGridSquares(start, end, searchGrid, scoreAs)

    assertEquals(actual, expected)

  }

  test("Sampling the grid.should be able to take a sample in the middle of the map") {
    val impassable: Point = Point(2, 2)

    val searchGrid = PathFinder.fromImpassable(Rectangle(Size(4, 3)), Batch(impassable))

    val expected: Batch[GridSquare] =
      Batch(
        Walkable(2, Point(2, 0), -1),
        Walkable(5, Point(1, 1), -1),
        // Sample point
        Walkable(7, Point(3, 1), -1),
        Blocked(10, Point(2, 2))
      )

    assertEquals(PathFinder.sampleAt(searchGrid, Point(2, 1), searchGrid.area.size.width), expected)
  }

  test("Sampling the grid.should be able to take a sample at the edge of the map") {
    val impassable: Point = Point(2, 2)

    val searchGrid = PathFinder.fromImpassable(Rectangle(Size(4, 3)), Batch(impassable))

    val expected: Batch[GridSquare] =
      Batch(
        Walkable(3, Point(3, 0), -1),
        Walkable(6, Point(2, 1), -1),
        // Sample point
        Walkable(11, Point(3, 2), -1)
      )

    assertEquals(PathFinder.sampleAt(searchGrid, Point(3, 1), searchGrid.area.size.width), expected)
  }

  test("Sampling the grid.should be able to take a sample at the top left of the map") {
    val impassable: Point = Point(2, 2)

    val searchGrid = PathFinder.fromImpassable(Rectangle(Size(4, 3)), Batch(impassable))

    val expected: Batch[GridSquare] =
      Batch(
        // Sample point
        Walkable(1, Point(1, 0), -1),
        Walkable(4, Point(0, 1), -1)
      )

    assertEquals(PathFinder.sampleAt(searchGrid, Point(0, 0), searchGrid.area.size.width), expected)
  }

  test(
    "Point.should be able to convert zero indexed coordinates into a one dimensional array position"
  ) {

    assertEquals(GridSquare.toIndex(Point(0, 0), 4), 0)
    assertEquals(GridSquare.toIndex(Point(1, 1), 4), 5)
    assertEquals(GridSquare.toIndex(Point(2, 3), 4), 14)
    assertEquals(GridSquare.toIndex(Point(2, 2), 3), 8)

  }

  test("Point.should be able to convert a zero indexed array position into coordinates") {

    assertEquals(GridSquare.fromIndex(0, 4), Point(0, 0))
    assertEquals(GridSquare.fromIndex(5, 4), Point(1, 1))
    assertEquals(GridSquare.fromIndex(14, 4), Point(2, 3))
    assertEquals(GridSquare.fromIndex(8, 3), Point(2, 2))

  }

  val start: Point      = Point(1, 1)
  val end: Point        = Point(3, 2)
  val impassable: Point = Point(2, 2)

  val searchGrid = PathFinder.fromImpassable(Rectangle(Size(4, 3)), Batch(impassable))

  test("Generating a grid.should be able to generate a simple search grid.impassable") {
    assertEquals(searchGrid.grid(GridSquare.toIndex(impassable, 4)), Blocked(10, impassable))
  }

  test("Real path") {
    val start: Point = Point(20, 23) - Point(17, 21)
    val end: Point   = Point(19, 26) - Point(17, 21)
    val walkable: Batch[Point] =
      Batch(
        Point(4, 6),
        Point(3, 6),
        Point(4, 5),
        Point(3, 5),
        Point(4, 4),
        Point(3, 4),
        Point(4, 3),
        Point(3, 3),
        Point(3, 2),
        Point(2, 6),
        Point(1, 6),
        Point(0, 6),
        Point(2, 5),
        Point(1, 5),
        Point(0, 5),
        Point(2, 4),
        Point(1, 4),
        Point(2, 3),
        Point(1, 3),
        Point(0, 4),
        Point(0, 3),
        Point(2, 2),
        Point(3, 1),
        Point(3, 0),
        Point(2, 1),
        Point(2, 0)
      )

    val searchGrid = PathFinder.fromWalkable(walkable)

    val actual: Batch[Point] =
      searchGrid.locatePath(start, end, scoreAs)

    val expected: List[Point] =
      List(Point(3, 2), Point(3, 3), Point(3, 4), Point(2, 4), Point(2, 5))

    assertEquals(actual.toList, expected)
  }

  test("fromRectangles") {
    // TODO
    assert(1 == 2)
  }

}
