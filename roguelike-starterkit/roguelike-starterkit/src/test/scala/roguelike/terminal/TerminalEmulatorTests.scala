package roguelike.terminal

import indigo.Point
import indigo.RGB
import indigo.RGBA
import indigo.Size
import roguelike.Tile

class TerminalEmulatorTests extends munit.FunSuite {

  test("should be able to put and get an element at a given position") {
    val console =
      TerminalEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val expected =
      Option(MapTile(Tile.`@`, RGB.White, RGBA.Zero))

    val actual =
      console.get(Point(1))

    assertEquals(expected, actual)
  }

  test("trying to get at an empty location returns None") {
    val console =
      TerminalEmulator(Size(3))
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
      TerminalEmulator(Size(10))
        .put(list)

    assert(
      List(Point(8, 2), Point(0, 0), Point(9, 9)).forall { v =>
        clue(console.get(clue(v))) == list.find(p => p._1 == v).map(_._2)
      }
    )
  }

  test("should be able insert a line of text") {
    val console =
      TerminalEmulator(Size(10))
        .putLine(Point(1, 3), "Hello", RGB.Red, RGBA.Blue)

    val actual =
      List(
        console.get(Point(1, 3)),
        console.get(Point(2, 3)),
        console.get(Point(3, 3)),
        console.get(Point(4, 3)),
        console.get(Point(5, 3))
      ).collect { case Some(s) => s }

    val expected =
      List(
        MapTile(Tile.`H`, RGB.Red, RGBA.Blue),
        MapTile(Tile.`e`, RGB.Red, RGBA.Blue),
        MapTile(Tile.`l`, RGB.Red, RGBA.Blue),
        MapTile(Tile.`l`, RGB.Red, RGBA.Blue),
        MapTile(Tile.`o`, RGB.Red, RGBA.Blue)
      )

    assertEquals(actual, expected)
  }

  test("continuous list (empty)") {
    val console =
      TerminalEmulator(Size(3))

    val actual =
      console.toTileList(MapTile(Tile.`.`))

    val expected =
      List.fill(9)(MapTile(Tile.`.`))

    assertEquals(actual.length, expected.length)
    assertEquals(actual.toList, expected)
  }

  test("continuous list (full)") {
    val coords =
      (0 to 2).flatMap { y =>
        (0 to 2).map { x =>
          Point(x, y)
        }
      }.toList

    val items: List[(Point, MapTile)] =
      coords.zip(List.fill(8)(MapTile(Tile.`!`)) :+ MapTile(Tile.`@`))

    val console =
      TerminalEmulator(Size(3))
        .put(items)

    val actual =
      console.toTileList(MapTile(Tile.`.`))

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
    assertEquals(actual.toList, expected)
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
      TerminalEmulator(Size(3))
        .put(itemsWithCoords)

    val actual =
      console.toTileList(MapTile(Tile.`.`))

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
    assert(actual.forall(expected.contains))
  }

  test("combine") {
    val consoleA =
      TerminalEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val consoleB =
      TerminalEmulator(Size(3))
        .put(Point(2, 2), Tile.`!`)

    val combined =
      consoleA combine consoleB

    assert(combined.get(Point(1)).get == MapTile(Tile.`@`))
    assert(combined.get(Point(2)).get == MapTile(Tile.`!`))
  }

  test("toList") {
    val consoleA =
      TerminalEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val consoleB =
      TerminalEmulator(Size(3))
        .put(Point(2, 2), Tile.`!`)

    val expected =
      List(MapTile(Tile.`@`), MapTile(Tile.`!`))

    val actual =
      (consoleA combine consoleB).toList

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("toPositionedList") {
    val consoleA =
      TerminalEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val consoleB =
      TerminalEmulator(Size(3))
        .put(Point(2, 2), Tile.`!`)

    val expected =
      List((Point(1), MapTile(Tile.`@`)), (Point(2), MapTile(Tile.`!`)))

    val actual =
      (consoleA combine consoleB).toPositionedList

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("placing something in the center works.") {
    val console =
      TerminalEmulator(Size(80, 50))
        .put(Point(40, 25), Tile.`@`)

    val expected =
      Option(MapTile(Tile.`@`, RGB.White, RGBA.Zero))

    val actual =
      console.get(Point(40, 25))

    assertEquals(expected, actual)

    val list =
      console.toPositionedList

    assert(list.contains((Point(40, 25), MapTile(Tile.`@`))))

    val drawn =
      console.toTileList(MapTile(Tile.LIGHT_SHADE))

    val foundAt =
      drawn.zipWithIndex.find(p => p._1 == MapTile(Tile.`@`)).map(_._2)

    assert(drawn.contains(MapTile(Tile.`@`)))
    assert(drawn.filter(_ == MapTile(Tile.`@`)).length == 1)
    assert(drawn.length == 80 * 50)
    assert(foundAt.nonEmpty)
    assert(clue(foundAt.get) == 2040)

  }

}
