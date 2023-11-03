package io.indigoengine.roguelike.starterkit.terminal

import indigo.*
import indigoextras.trees.QuadTree
import indigoextras.trees.QuadTree.QuadBranch
import indigoextras.trees.QuadTree.QuadEmpty
import indigoextras.trees.QuadTree.QuadLeaf
import io.indigoengine.roguelike.starterkit.Tile

import scala.annotation.tailrec

final case class TerminalEmulator(screenSize: Size, charMap: QuadTree[MapTile]):

  private lazy val coordsBatch: Batch[Point] =
    Batch.fromIndexedSeq((0 until screenSize.height).flatMap { y =>
      (0 until screenSize.width).map { x =>
        Point(x, y)
      }
    })

  def put(coords: Point, tile: Tile, fgColor: RGBA, bgColor: RGBA): TerminalEmulator =
    this.copy(charMap =
      charMap.insertElement(MapTile(tile, fgColor, bgColor), Vertex.fromPoint(coords))
    )
  def put(coords: Point, tile: Tile, fgColor: RGBA): TerminalEmulator =
    this.copy(charMap = charMap.insertElement(MapTile(tile, fgColor), Vertex.fromPoint(coords)))
  def put(coords: Point, tile: Tile): TerminalEmulator =
    this.copy(charMap = charMap.insertElement(MapTile(tile), Vertex.fromPoint(coords)))

  def put(tiles: Batch[(Point, MapTile)]): TerminalEmulator =
    this.copy(charMap = charMap.insertElements(tiles.map(p => (p._2, Vertex.fromPoint(p._1)))))
  def put(tiles: (Point, MapTile)*): TerminalEmulator =
    put(Batch.fromSeq(tiles))
  def put(coords: Point, mapTile: MapTile): TerminalEmulator =
    put(Batch(coords -> mapTile))

  def fill(mapTile: MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.insertElements(coordsBatch.map(pt => mapTile -> pt.toVertex))
    )

  // TODO: Wrap text if too long for line
  def putLine(startCoords: Point, text: String, fgColor: RGBA, bgColor: RGBA): TerminalEmulator =
    val tiles: Batch[(Point, MapTile)] =
      Batch.fromArray(text.toCharArray).zipWithIndex.map { case (c, i) =>
        Tile.charCodes.get(if c == '\\' then "\\" else c.toString) match
          case None =>
            // Couldn't find character, skip it.
            startCoords + Point(i, 0) -> MapTile(Tile.SPACE, fgColor, bgColor)

          case Some(char) =>
            startCoords + Point(i, 0) -> MapTile(Tile(char), fgColor, bgColor)
      }
    put(tiles)

  def putLines(
      startCoords: Point,
      textLines: Batch[String],
      fgColor: RGBA,
      bgColor: RGBA
  ): TerminalEmulator =
    @tailrec
    def rec(remaining: List[String], yOffset: Int, term: TerminalEmulator): TerminalEmulator =
      remaining match
        case Nil =>
          term

        case x :: xs =>
          rec(
            xs,
            yOffset + 1,
            term.putLine(startCoords + Point(0, yOffset), x, fgColor, bgColor)
          )

    rec(textLines.toList, 0, this)

  def get(coords: Point): Option[MapTile] =
    charMap.fetchElementAt(Vertex.fromPoint(coords))

  def delete(coords: Point): TerminalEmulator =
    this.copy(charMap = charMap.removeElement(Vertex.fromPoint(coords)))

  def clear: TerminalEmulator =
    this.copy(charMap =
      QuadTree.empty[MapTile](screenSize.width.toDouble, screenSize.height.toDouble)
    )

  def optimise: TerminalEmulator =
    this.copy(charMap = charMap.prune)

  def toTileBatch(default: MapTile): Batch[MapTile] =
    coordsBatch.map(pt => get(pt).getOrElse(default))

  def draw(
      tileSheet: AssetName,
      charSize: Size,
      default: MapTile,
      maxTileCount: Int
  ): TerminalEntity =
    TerminalEntity(tileSheet, screenSize, charSize, toTileBatch(default), maxTileCount)

  private def toCloneTileData(
      position: Point,
      charCrops: Batch[(Int, Int, Int, Int)],
      data: Batch[(Point, MapTile)]
  ): Batch[CloneTileData] =
    data.map { case (pt, t) =>
      val crop = charCrops(t.char.toInt)
      CloneTileData(
        (pt.x * crop._3) + position.x,
        (pt.y * crop._4) + position.y,
        crop._1,
        crop._2,
        crop._3,
        crop._4
      )
    }

  def toCloneTiles(
      idPrefix: CloneId,
      position: Point,
      charCrops: Batch[(Int, Int, Int, Int)]
  )(makeBlank: (RGBA, RGBA) => Cloneable): TerminalClones =
    val makeId: (RGBA, RGBA) => CloneId = (fg, bg) =>
      CloneId(s"""${idPrefix.toString}_${fg.hashCode}_${bg.hashCode}""")

    val combinations: Batch[((CloneId, RGBA, RGBA), Batch[(Point, MapTile)])] =
      Batch.fromMap(
        toPositionedBatch
          .groupBy(p =>
            (makeId(p._2.foreground, p._2.background), p._2.foreground, p._2.background)
          )
      )

    val stuff =
      combinations.map { c =>
        (
          CloneBlank(c._1._1, makeBlank(c._1._2, c._1._3)),
          CloneTiles(c._1._1, toCloneTileData(position, charCrops, c._2))
        )
      }

    TerminalClones(stuff.map(_._1), stuff.map(_._2))

  def toBatch: Batch[MapTile] =
    @tailrec
    def rec(open: List[QuadTree[MapTile]], acc: Batch[MapTile]): Batch[MapTile] =
      open match
        case Nil =>
          acc

        case x :: xs =>
          x match {
            case _: QuadEmpty[MapTile] =>
              rec(xs, acc)

            case l: QuadLeaf[MapTile] =>
              rec(xs, l.value :: acc)

            case b: QuadBranch[MapTile] if b.isEmpty =>
              rec(xs, acc)

            case QuadBranch(_, a, b, c, d) =>
              val next =
                (if a.isEmpty then Nil else List(a)) ++
                  (if b.isEmpty then Nil else List(b)) ++
                  (if c.isEmpty then Nil else List(c)) ++
                  (if d.isEmpty then Nil else List(d))

              rec(xs ++ next, acc)
          }

    rec(List(charMap), Batch.empty)

  def toPositionedBatch: Batch[(Point, MapTile)] =
    @tailrec
    def rec(open: List[QuadTree[MapTile]], acc: Batch[(Point, MapTile)]): Batch[(Point, MapTile)] =
      open match
        case Nil =>
          acc

        case x :: xs =>
          x match {
            case _: QuadEmpty[MapTile] =>
              rec(xs, acc)

            case l: QuadLeaf[MapTile] =>
              rec(xs, (l.exactPosition.toPoint, l.value) :: acc)

            case b: QuadBranch[MapTile] if b.isEmpty =>
              rec(xs, acc)

            case QuadBranch(_, a, b, c, d) =>
              val next =
                (if a.isEmpty then Nil else List(a)) ++
                  (if b.isEmpty then Nil else List(b)) ++
                  (if c.isEmpty then Nil else List(c)) ++
                  (if d.isEmpty then Nil else List(d))

              rec(xs ++ next, acc)
          }

    rec(List(charMap), Batch.empty)

  def |+|(otherConsole: TerminalEmulator): TerminalEmulator =
    combine(otherConsole)
  def combine(otherConsole: TerminalEmulator): TerminalEmulator =
    this.copy(
      charMap = charMap.insertElements(
        otherConsole.toPositionedBatch.map(p => (p._2, Vertex.fromPoint(p._1)))
      )
    )

  def inset(otherConsole: TerminalEmulator, offset: Point): TerminalEmulator =
    this.copy(
      charMap = charMap.insertElements(
        otherConsole.toPositionedBatch.map(p => (p._2, Vertex.fromPoint(p._1 + offset)))
      )
    )

object TerminalEmulator:
  def apply(screenSize: Size): TerminalEmulator =
    TerminalEmulator(
      screenSize,
      QuadTree.empty[MapTile](screenSize.width.toDouble, screenSize.height.toDouble)
    )

final case class TerminalClones(blanks: Batch[CloneBlank], clones: Batch[CloneTiles]):
  def |+|(other: TerminalClones): TerminalClones =
    combine(other)
  def combine(other: TerminalClones): TerminalClones =
    TerminalClones(blanks ++ other.blanks, clones ++ other.clones)

object TerminalClones:
  def empty: TerminalClones =
    TerminalClones(Batch.empty, Batch.empty)
