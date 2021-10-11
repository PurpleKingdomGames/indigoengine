package roguelike.utils

import roguelike.utils.GridSquare.{Walkable, Blocked}

import indigo.shared.dice.Dice
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Size

class PathFinderTests extends munit.FunSuite {

  val scoreAs: GridSquare => Int = _ => 1

  val coords: Point = Point(0, 0)

  test("Finding an unobscured path.should be able to find a route") {
    val start: Point      = Point(2, 1)
    val end: Point        = Point(0, 2)
    val impassable: Point = Point(1, 0)

    val searchGrid = PathFinder.fromImpassable(Size(3, 3), List(impassable))

    val path: List[Point] = searchGrid.locatePath(Dice.fromSeed(0), start, end, scoreAs)

    val possiblePaths: List[List[Point]] = List(
      List(start, Point(2, 2), Point(1, 2), end),
      List(start, Point(1, 1), Point(0, 1), end),
      List(start, Point(1, 1), Point(1, 2), end)
    )

    assertEquals(possiblePaths.contains(path), true)
  }

  test("Finding an unobscured path.should be able to find a route (from walkable)") {
    val start: Point = Point(2, 1)
    val end: Point   = Point(0, 2)
    val walkable: List[Point] =
      List(
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

    val searchGrid = PathFinder.fromWalkable(Size(3, 3), walkable)

    val path: List[Point] = searchGrid.locatePath(Dice.fromSeed(0), start, end, scoreAs)

    val possiblePaths: List[List[Point]] = List(
      List(start, Point(2, 2), Point(1, 2), end),
      List(start, Point(1, 1), Point(0, 1), end),
      List(start, Point(1, 1), Point(1, 2), end)
    )

    assertEquals(possiblePaths.contains(path), true)
  }

  test("Scoring the grid.should be able to score a grid") {
    val start: Point      = Point(2, 1)
    val end: Point        = Point(0, 2)
    val impassable: Point = Point(1, 0)

    val searchGrid = PathFinder.fromImpassable(Size(3, 3), List(impassable))

    val expected: List[GridSquare] =
      List(
        Walkable(0, Point(0, 0), 2),
        Blocked(1, Point(1, 0)),
        Walkable(
          2,
          Point(2, 0),
          -1
        ), // Unscored squares are returned to keep sampleAt working correctly
        Walkable(3, Point(0, 1), 1),
        Walkable(4, Point(1, 1), 2),
        Walkable(5, Point(2, 1), 3), //start
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

    val searchGrid = PathFinder.fromImpassable(Size(4, 3), List(impassable))

    val expected: List[GridSquare] =
      List(
        Walkable(2, Point(2, 0), -1),
        Walkable(5, Point(1, 1), -1),
        //Sample point
        Walkable(7, Point(3, 1), -1),
        Blocked(10, Point(2, 2))
      )

    assertEquals(PathFinder.sampleAt(searchGrid, Point(2, 1), searchGrid.size.width), expected)
  }

  test("Sampling the grid.should be able to take a sample at the edge of the map") {
    val impassable: Point = Point(2, 2)

    val searchGrid = PathFinder.fromImpassable(Size(4, 3), List(impassable))

    val expected: List[GridSquare] =
      List(
        Walkable(3, Point(3, 0), -1),
        Walkable(6, Point(2, 1), -1),
        //Sample point
        Walkable(11, Point(3, 2), -1)
      )

    assertEquals(PathFinder.sampleAt(searchGrid, Point(3, 1), searchGrid.size.width), expected)
  }

  test("Sampling the grid.should be able to take a sample at the top left of the map") {
    val impassable: Point = Point(2, 2)

    val searchGrid = PathFinder.fromImpassable(Size(4, 3), List(impassable))

    val expected: List[GridSquare] =
      List(
        //Sample point
        Walkable(1, Point(1, 0), -1),
        Walkable(4, Point(0, 1), -1)
      )

    assertEquals(PathFinder.sampleAt(searchGrid, Point(0, 0), searchGrid.size.width), expected)
  }

  test(
    "Point.should be able to convert zero indexed coordinates into a one dimensional array position"
  ) {

    assertEquals(Coords.toGridPosition(Point(0, 0), 4), 0)
    assertEquals(Coords.toGridPosition(Point(1, 1), 4), 5)
    assertEquals(Coords.toGridPosition(Point(2, 3), 4), 14)
    assertEquals(Coords.toGridPosition(Point(2, 2), 3), 8)

  }

  test("Point.should be able to convert a zero indexed array position into coordinates") {

    assertEquals(Coords.fromIndex(0, 4), Point(0, 0))
    assertEquals(Coords.fromIndex(5, 4), Point(1, 1))
    assertEquals(Coords.fromIndex(14, 4), Point(2, 3))
    assertEquals(Coords.fromIndex(8, 3), Point(2, 2))

  }

  val start: Point      = Point(1, 1)
  val end: Point        = Point(3, 2)
  val impassable: Point = Point(2, 2)

  val searchGrid = PathFinder.fromImpassable(Size(4, 3), List(impassable))

  test("Generating a grid.should be able to generate a simple search grid.impassable") {
    assertEquals(searchGrid.grid(Coords.toGridPosition(impassable, 4)), Blocked(10, impassable))
  }

  test("Real path") {
    val start: Point = Point(20, 23) - Point(17, 21)
    val end: Point   = Point(19, 26) - Point(17, 21)
    val walkable: List[Point] =
      List(
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

    val searchGrid = PathFinder.fromWalkable(Size(5, 7), walkable)

    val actual: List[Point] =
      searchGrid.locatePath(Dice.fromSeed(0), start, end, scoreAs)

    val possiblePaths: List[List[Point]] = List(
      List(Point(3, 2), Point(3, 3), Point(3, 4), Point(2, 4), Point(2, 5)),
      List(Point(3, 2), Point(2, 2), Point(2, 3), Point(2, 4), Point(2, 5))
    )

    assert(possiblePaths.contains(actual), true)
  }

}
