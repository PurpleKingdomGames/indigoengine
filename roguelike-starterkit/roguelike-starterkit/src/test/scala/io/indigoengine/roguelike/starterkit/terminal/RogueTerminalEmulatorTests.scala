package io.indigoengine.roguelike.starterkit.terminal

import indigo.*
import io.indigoengine.roguelike.starterkit.Tile

class RogueTerminalEmulatorTests extends munit.FunSuite {

  test("should be able to put and get an element at a given position") {
    val console =
      RogueTerminalEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val expected =
      MapTile(Tile.`@`, RGBA.White, RGBA.Zero)

    val actual =
      console.get(Point(1)).get

    assertEquals(expected, actual)
  }

  test("trying to get at an empty location returns None") {
    val console =
      RogueTerminalEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val expected: Option[MapTile] =
      None

    val actual =
      console.get(Point(0))

    assertEquals(expected, actual)
  }

  test("should be able insert multiple items") {
    val list =
      Batch(
        (Point(8, 2), MapTile(Tile.`@`)),
        (Point(0, 0), MapTile(Tile.`!`)),
        (Point(9, 9), MapTile(Tile.`?`))
      )

    val console =
      RogueTerminalEmulator(Size(10))
        .put(list)

    assert(
      Batch(Point(8, 2), Point(0, 0), Point(9, 9)).forall { v =>
        clue(v)

        val actual =
          console.get(v).get

        val expected =
          list.find(p => p._1 == v).map(_._2).get

        clue(actual) == clue(expected)
      }
    )
  }

  test("should be able insert a line of text") {
    val console =
      RogueTerminalEmulator(Size(10))
        .putLine(Point(1, 3), "Hello", RGBA.Red, RGBA.Blue)

    val actual =
      Batch(
        console.get(Point(1, 3)).get,
        console.get(Point(2, 3)).get,
        console.get(Point(3, 3)).get,
        console.get(Point(4, 3)).get,
        console.get(Point(5, 3)).get
      )

    val expected =
      Batch(
        MapTile(Tile.`H`, RGBA.Red, RGBA.Blue),
        MapTile(Tile.`e`, RGBA.Red, RGBA.Blue),
        MapTile(Tile.`l`, RGBA.Red, RGBA.Blue),
        MapTile(Tile.`l`, RGBA.Red, RGBA.Blue),
        MapTile(Tile.`o`, RGBA.Red, RGBA.Blue)
      )

    assertEquals(actual, expected)
  }

  test("continuous list (empty)") {
    val console =
      RogueTerminalEmulator(Size(3)).fill(MapTile(Tile.`.`))

    val actual =
      console.toTileBatch

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

    val items: Batch[(Point, MapTile)] =
      Batch.fromList(coords.zip(List.fill(8)(MapTile(Tile.`!`)) :+ MapTile(Tile.`@`)))

    val console =
      RogueTerminalEmulator(Size(3))
        .put(items)

    val actual =
      console.toTileBatch

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

    val itemsWithCoords: Batch[(Point, MapTile)] =
      Batch.fromList(coords.zip(items))

    val console =
      RogueTerminalEmulator(Size(3))
        .put(itemsWithCoords)

    val actual =
      console.toTileBatch

    val expected =
      List(
        MapTile(Tile.`a`),
        Terminal.EmptyTile,
        Terminal.EmptyTile,
        MapTile(Tile.`b`),
        MapTile(Tile.`c`),
        Terminal.EmptyTile,
        MapTile(Tile.`d`),
        MapTile(Tile.`e`),
        MapTile(Tile.`f`)
      )

    assertEquals(clue(actual.length), clue(expected.length))
    assert(actual.forall(expected.contains))
  }

  test("combine") {
    val consoleA =
      RogueTerminalEmulator(Size(3))
        .put(Point(1), Tile.`@`)

    val consoleB =
      RogueTerminalEmulator(Size(3))
        .put(Point(2), Tile.`!`)

    val combined =
      consoleA combine consoleB

    assert(clue(combined.get(Point(1))).get == clue(MapTile(Tile.`@`)))
    assert(clue(combined.get(Point(2))).get == clue(MapTile(Tile.`!`)))
  }

  test("toList") {
    val consoleA =
      RogueTerminalEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val consoleB =
      RogueTerminalEmulator(Size(3))
        .put(Point(2, 2), Tile.`!`)

    val expected =
      List(MapTile(Tile.`@`), MapTile(Tile.`!`))

    val actual =
      (consoleA combine consoleB).toBatch

    assert(clue(actual.length) == clue(9))
    assert(clue(expected).forall(actual.contains))
  }

  test("toPositionedList") {
    val consoleA =
      RogueTerminalEmulator(Size(3))
        .put(Point(1, 1), Tile.`@`)

    val consoleB =
      RogueTerminalEmulator(Size(3))
        .put(Point(2, 2), Tile.`!`)

    val expected =
      List((Point(1), MapTile(Tile.`@`)), (Point(2), MapTile(Tile.`!`)))

    val actual =
      (consoleA combine consoleB).toPositionedBatch

    assert(clue(actual.length) == clue(9))
    assert(clue(expected).forall(actual.contains))
  }

  test("placing something in the center works.") {
    val console =
      RogueTerminalEmulator(Size(80, 50))
        .put(Point(40, 25), Tile.`@`)

    val expected =
      MapTile(Tile.`@`, RGBA.White, RGBA.Zero)

    val actual =
      console.get(Point(40, 25)).get

    assertEquals(expected, actual)

    val list =
      console.toPositionedBatch

    assert(list.contains((Point(40, 25), MapTile(Tile.`@`))))

    val drawn =
      console.toTileBatch

    val foundAt =
      drawn.zipWithIndex.find(p => p._1 == MapTile(Tile.`@`)).map(_._2)

    assert(drawn.contains(MapTile(Tile.`@`)))
    assert(drawn.filter(_ == MapTile(Tile.`@`)).length == 1)
    assert(drawn.length == 80 * 50)
    assert(foundAt.nonEmpty)
    assert(clue(foundAt.get) == 2040)

  }

  test("Terminal clones are monoids") {
    val actual =
      List(
        TerminalClones(
          Batch(CloneBlank(CloneId("a"), Graphic(1, 1, Material.Bitmap(AssetName("a"))))),
          Batch(CloneTiles(CloneId("a"), CloneTileData(0, 0, Radians.zero, 0, 0, 0, 0)))
        ),
        TerminalClones(
          Batch(CloneBlank(CloneId("b"), Graphic(1, 1, Material.Bitmap(AssetName("b"))))),
          Batch(CloneTiles(CloneId("b"), CloneTileData(0, 0, Radians.zero, 0, 0, 0, 0)))
        )
      ).foldLeft(TerminalClones.empty)(_ |+| _)

    val expected =
      TerminalClones(
        Batch(
          CloneBlank(CloneId("a"), Graphic(1, 1, Material.Bitmap(AssetName("a")))),
          CloneBlank(CloneId("b"), Graphic(1, 1, Material.Bitmap(AssetName("b"))))
        ),
        Batch(
          CloneTiles(CloneId("a"), CloneTileData(0, 0, Radians.zero, 0, 0, 0, 0)),
          CloneTiles(CloneId("b"), CloneTileData(0, 0, Radians.zero, 0, 0, 0, 0))
        )
      )

    assert(actual.blanks.length == expected.blanks.length)
    assert(actual.clones.length == expected.clones.length)
    assertEquals(actual.blanks.map(_.id), expected.blanks.map(_.id))
    assertEquals(actual.clones.map(_.id), expected.clones.map(_.id))
  }

  test("RogueTerminalEmulator can be cloned") {

    val a = RogueTerminalEmulator(Size(3, 3)).put(Point(1), Tile.`@`)
    val b = a.clone()

    assert(clue(a.get(Point(1))) == clue(b.get(Point(1))))

    b.put(Point(1), Tile.`!`)

    assert(clue(a.get(Point(1)).map(_.char)) == clue(Some(Tile.`@`)))
    assert(clue(b.get(Point(1)).map(_.char)) == clue(Some(Tile.`!`)))

  }

}
