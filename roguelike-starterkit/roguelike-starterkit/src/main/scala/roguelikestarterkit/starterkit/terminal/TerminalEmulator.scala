package roguelikestarterkit.terminal

import indigo.*
import indigoextras.trees.QuadTree
import indigoextras.trees.QuadTree.QuadBranch
import indigoextras.trees.QuadTree.QuadEmpty
import indigoextras.trees.QuadTree.QuadLeaf
import roguelikestarterkit.FOV
import roguelikestarterkit.Tile

import scala.annotation.tailrec

/** TerminalEmulator represents an immutable, sparsely populated terminal.
  */
final case class TerminalEmulator(size: Size, charMap: QuadTree[MapTile]) extends Terminal:

  private lazy val coordsBatch: Batch[Point] =
    Batch.fromIndexedSeq((0 until size.height).flatMap { y =>
      (0 until size.width).map { x =>
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
  def put(tiles: Batch[(Point, MapTile)], offset: Point): TerminalEmulator =
    this.copy(
      charMap = charMap.insertElements(
        tiles.map(p => (p._2, Vertex.fromPoint(p._1 + offset)))
      )
    )
  def put(tiles: (Point, MapTile)*): TerminalEmulator =
    put(Batch.fromSeq(tiles))
  def put(coords: Point, mapTile: MapTile): TerminalEmulator =
    put(Batch(coords -> mapTile))

  def fill(mapTile: MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.insertElements(coordsBatch.map(pt => mapTile -> pt.toVertex))
    )
  def fill(tile: Tile, foregroundColor: RGBA, backgroundColor: RGBA): TerminalEmulator =
    fill(MapTile(tile, foregroundColor, backgroundColor))

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
    this.copy(charMap = QuadTree.empty[MapTile](size.width.toDouble, size.height.toDouble))

  def optimise: TerminalEmulator =
    this.copy(charMap = charMap.prune)

  /** Returns all MapTiles, guarantees order, inserts a default where there is a gap. */
  def toTileBatch: Batch[MapTile] =
    coordsBatch.map(pt => get(pt).getOrElse(Terminal.EmptyTile))

  /** Returns all MapTiles in a given region, guarantees order, inserts a default where there is a
    * gap.
    */
  def toTileBatch(region: Rectangle): Batch[MapTile] =
    coordsBatch.filter(pt => region.contains(pt)).map { pt =>
      get(pt).getOrElse(Terminal.EmptyTile)
    }

  /** Returns all MapTiles, does not guarantee order, does not fill in gaps. */
  def toBatch: Batch[MapTile] =
    charMap.toBatch

  /** Returns all MapTiles in a given region, does not guarantee order, does not fill in gaps. */
  def toBatch(region: Rectangle): Batch[MapTile] =
    charMap.searchByBoundingBox(region.toBoundingBox)

  /** Returns all MapTiles with their grid positions, does not guarantee order (but the position is
    * given), does not fill in gaps.
    */
  def toPositionedBatch: Batch[(Point, MapTile)] =
    charMap.toBatchWithPosition.map(p => p._1.toPoint -> p._2)

  /** Returns all MapTiles with their grid positions in a given region, does not guarantee order
    * (but the position is given), does not fill in gaps.
    */
  def toPositionedBatch(region: Rectangle): Batch[(Point, MapTile)] =
    charMap.toBatchWithPosition
      .filter(p => region.contains(p._1.toPoint))
      .map(p => p._1.toPoint -> p._2)

  def |+|(otherConsole: Terminal): TerminalEmulator =
    combine(otherConsole)
  def combine(otherConsole: Terminal): TerminalEmulator =
    this.copy(
      charMap = charMap.insertElements(
        otherConsole.toPositionedBatch.map(p => (p._2, Vertex.fromPoint(p._1)))
      )
    )

  def inset(otherConsole: Terminal, offset: Point): TerminalEmulator =
    put(otherConsole.toPositionedBatch, offset)

  def toRogueTerminalEmulator: RogueTerminalEmulator =
    RogueTerminalEmulator(size).inset(this, Point.zero)

  def modifyAt(position: Point)(modifier: MapTile => MapTile): TerminalEmulator =
    get(position) match
      case None =>
        this

      case Some(value) =>
        put(position, modifier(value))

  def map(modifier: (Point, MapTile) => MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.toBatchWithPosition.foldLeft(charMap) { case (acc, (pos, char)) =>
        acc.insertElement(modifier(pos.toPoint, char), pos)
      }
    )

  def mapRectangle(region: Rectangle)(modifier: (Point, MapTile) => MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.toBatchWithPosition.foldLeft(charMap) { case (acc, (pos, char)) =>
        if region.contains(pos.toPoint) then acc.insertElement(modifier(pos.toPoint, char), pos)
        else acc
      }
    )

  def fillRectangle(region: Rectangle, mapTile: MapTile): TerminalEmulator =
    mapRectangle(region)((_, _) => mapTile)
  def fillRectangle(region: Rectangle, tile: Tile): TerminalEmulator =
    mapRectangle(region)((_, mt) => mt.withChar(tile))
  def fillRectangle(region: Rectangle, tile: Tile, foreground: RGBA): TerminalEmulator =
    mapRectangle(region)((_, mt) => MapTile(tile, foreground, mt.background))
  def fillRectangle(
      region: Rectangle,
      tile: Tile,
      foreground: RGBA,
      background: RGBA
  ): TerminalEmulator =
    mapRectangle(region)((_, mt) => MapTile(tile, foreground, background))

  def mapCircle(circle: Circle)(modifier: (Point, MapTile) => MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.toBatchWithPosition.foldLeft(charMap) { case (acc, (pos, char)) =>
        if circle.contains(pos.toPoint) then acc.insertElement(modifier(pos.toPoint, char), pos)
        else acc
      }
    )

  def fillCircle(circle: Circle, mapTile: MapTile): TerminalEmulator =
    mapCircle(circle)((_, _) => mapTile)
  def fillCircle(circle: Circle, tile: Tile): TerminalEmulator =
    mapCircle(circle)((_, mt) => mt.withChar(tile))
  def fillCircle(circle: Circle, tile: Tile, foreground: RGBA): TerminalEmulator =
    mapCircle(circle)((_, mt) => MapTile(tile, foreground, mt.background))
  def fillCircle(circle: Circle, tile: Tile, foreground: RGBA, background: RGBA): TerminalEmulator =
    mapCircle(circle)((_, mt) => MapTile(tile, foreground, background))

  def mapLine(from: Point, to: Point)(modifier: (Point, MapTile) => MapTile): TerminalEmulator =
    this.copy(
      charMap = FOV.bresenhamLine(from, to).foldLeft(charMap) { case (acc, pos) =>
        get(pos) match
          case None =>
            acc

          case Some(value) =>
            acc.insertElement(modifier(pos, value), pos.toVertex)
      }
    )

  def mapLine(line: LineSegment)(modifier: (Point, MapTile) => MapTile): TerminalEmulator =
    mapLine(line.start.toPoint, line.end.toPoint)(modifier)
  def fillLine(line: LineSegment, mapTile: MapTile): TerminalEmulator =
    mapLine(line.start.toPoint, line.end.toPoint)((_, _) => mapTile)
  def fillLine(line: LineSegment, tile: Tile): TerminalEmulator =
    mapLine(line.start.toPoint, line.end.toPoint)((_, mt) => mt.withChar(tile))
  def fillLine(line: LineSegment, tile: Tile, foreground: RGBA): TerminalEmulator =
    mapLine(line.start.toPoint, line.end.toPoint)((_, mt) =>
      MapTile(tile, foreground, mt.background)
    )
  def fillLine(
      line: LineSegment,
      tile: Tile,
      foreground: RGBA,
      background: RGBA
  ): TerminalEmulator =
    mapLine(line.start.toPoint, line.end.toPoint)((_, mt) => MapTile(tile, foreground, background))
  def fillLine(from: Point, to: Point, mapTile: MapTile): TerminalEmulator =
    mapLine(from, to)((_, _) => mapTile)
  def fillLine(from: Point, to: Point, tile: Tile): TerminalEmulator =
    mapLine(from, to)((_, mt) => mt.withChar(tile))
  def fillLine(from: Point, to: Point, tile: Tile, foreground: RGBA): TerminalEmulator =
    mapLine(from, to)((_, mt) => MapTile(tile, foreground, mt.background))
  def fillLine(
      from: Point,
      to: Point,
      tile: Tile,
      foreground: RGBA,
      background: RGBA
  ): TerminalEmulator =
    mapLine(from, to)((_, mt) => MapTile(tile, foreground, background))

object TerminalEmulator:
  def apply(screenSize: Size): TerminalEmulator =
    TerminalEmulator(
      screenSize,
      QuadTree.empty[MapTile](screenSize.width.toDouble, screenSize.height.toDouble)
    )
