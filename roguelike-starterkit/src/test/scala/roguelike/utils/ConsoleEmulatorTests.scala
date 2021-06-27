package roguelike.utils

import indigo.RGB
import indigo.RGBA
import indigo.Point
import indigo.Size

import roguelike.DfTiles.Tile

class ConsoleEmulatorTests extends munit.FunSuite {

  test("should be able to put and get an element at a given position") {
    val console =
      ConsoleEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val expected =
      Option(MapTile(Tile.`@`, RGB.White, RGBA.Zero))

    val actual =
      console.get(Point(1))

    assertEquals(expected, actual)
  }

  test("trying to get at an empty location returns None") {
    val console =
      ConsoleEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val expected: Option[MapTile] =
      None

    val actual =
      console.get(Point(0))

    assertEquals(expected, actual)
  }

  test("should be able insert multiple items") {
    val list =
      List(
        (Point(8, 2), MapTile(Tile.`@`)),
        (Point(0, 0), MapTile(Tile.`!`)),
        (Point(9, 9), MapTile(Tile.`?`))
      )

    val console =
      ConsoleEmulator(Size(10))
        .put(list)

    assert(
      List(Point(8, 2), Point(0, 0), Point(9, 9)).forall { v =>
        clue(console.get(clue(v))) == list.find(p => p._1 == v).map(_._2)
      }
    )
  }

  test("continuous list (empty)") {
    val console =
      ConsoleEmulator(Size(3))

    val actual =
      console.toFullList(MapTile(Tile.`.`))

    val expected =
      List.fill(9)(MapTile(Tile.`.`))

    assertEquals(actual.length, expected.length)
    assertEquals(actual, expected)
  }

  test("continuous list (full)") {
    val coords =
      (0 to 2).flatMap { x =>
        (0 to 2).map { y =>
          Point(x, y)
        }
      }.toList

    val items: List[(Point, MapTile)] =
      coords.zip(List.fill(8)(MapTile(Tile.`!`)) :+ MapTile(Tile.`@`))

    val console =
      ConsoleEmulator(Size(3))
        .put(items)

    val actual =
      console.toFullList(MapTile(Tile.`.`))

    val expected =
      List(
        MapTile(Tile.`!`),
        MapTile(Tile.`!`),
        MapTile(Tile.`!`),
        MapTile(Tile.`!`),
        MapTile(Tile.`!`),
        MapTile(Tile.`!`),
        MapTile(Tile.`!`),
        MapTile(Tile.`!`),
        MapTile(Tile.`@`)
      )

    assertEquals(actual.length, expected.length)
    assertEquals(actual, expected)
  }

  test("continuous list (sparse)") {
    val coords =
      List(
        Point(0, 0),
        // Point(0, 1),
        // Point(0, 2),
        Point(1, 0),
        Point(1, 1),
        // Point(1, 2),
        Point(2, 0),
        Point(2, 1),
        Point(2, 2)
      )

    val items: List[MapTile] =
      List(
        MapTile(Tile.`a`),
        MapTile(Tile.`b`),
        MapTile(Tile.`c`),
        MapTile(Tile.`d`),
        MapTile(Tile.`e`),
        MapTile(Tile.`f`)
      )

    val itemsWithCoords: List[(Point, MapTile)] =
      coords.zip(items)

    val console =
      ConsoleEmulator(Size(3))
        .put(itemsWithCoords)

    val actual =
      console.toFullList(MapTile(Tile.`.`))

    val expected =
      List(
        MapTile(Tile.`a`),
        MapTile(Tile.`.`),
        MapTile(Tile.`.`),
        MapTile(Tile.`b`),
        MapTile(Tile.`c`),
        MapTile(Tile.`.`),
        MapTile(Tile.`d`),
        MapTile(Tile.`e`),
        MapTile(Tile.`f`)
      )

    assertEquals(actual.length, expected.length)
    assertEquals(actual, expected)
  }

}
