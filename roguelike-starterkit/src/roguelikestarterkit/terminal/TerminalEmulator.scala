package roguelikestarterkit.terminal

import indigo.*
import indigoextras.datatypes.SparseGrid
import roguelikestarterkit.Tile

import scala.annotation.tailrec

/** TerminalEmulator represents an immutable, sparsely populated terminal.
  */
final case class TerminalEmulator(size: Size, charMap: SparseGrid[MapTile]) extends Terminal:

  private lazy val coordsBatch: Batch[Point] =
    Batch.fromIndexedSeq((0 until size.height).flatMap { y =>
      (0 until size.width).map { x =>
        Point(x, y)
      }
    })

  def put(coords: Point, tile: Tile, fgColor: RGBA, bgColor: RGBA): TerminalEmulator =
    this.copy(charMap = charMap.put(coords, MapTile(tile, fgColor, bgColor)))
  def put(coords: Point, tile: Tile, fgColor: RGBA): TerminalEmulator =
    this.copy(charMap = charMap.put(coords, MapTile(tile, fgColor)))
  def put(coords: Point, tile: Tile): TerminalEmulator =
    this.copy(charMap = charMap.put(coords, MapTile(tile)))

  def put(tiles: Batch[(Point, MapTile)]): TerminalEmulator =
    this.copy(charMap = charMap.put(tiles.map(p => (p._1, p._2))))
  def put(tiles: Batch[(Point, MapTile)], offset: Point): TerminalEmulator =
    this.copy(
      charMap = charMap.put(
        tiles.map(p => (p._1 + offset, p._2))
      )
    )
  def put(tiles: (Point, MapTile)*): TerminalEmulator =
    put(Batch.fromSeq(tiles))
  def put(coords: Point, mapTile: MapTile): TerminalEmulator =
    put(Batch(coords -> mapTile))

  def fill(mapTile: MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.put(coordsBatch.map(pt => pt -> mapTile))
    )
  def fill(tile: Tile, foregroundColor: RGBA, backgroundColor: RGBA): TerminalEmulator =
    fill(MapTile(tile, foregroundColor, backgroundColor))

  def putLine(startCoords: Point, text: String, fgColor: RGBA, bgColor: RGBA): TerminalEmulator =
    val tiles: Batch[(Point, MapTile)] =
      TerminalEmulator.stringToTileBatch(text).zipWithIndex.map { case (t, i) =>
        startCoords + Point(i, 0) -> MapTile(t, fgColor, bgColor)
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

  def putTileLine(
      startCoords: Point,
      tiles: Batch[Tile],
      fgColor: RGBA,
      bgColor: RGBA
  ): TerminalEmulator =
    val res: Batch[(Point, MapTile)] =
      tiles.zipWithIndex.map { case (t, i) =>
        startCoords + Point(i, 0) -> MapTile(t, fgColor, bgColor)
      }
    put(res)

  def putTileLines(
      startCoords: Point,
      textLines: Batch[Batch[Tile]],
      fgColor: RGBA,
      bgColor: RGBA
  ): TerminalEmulator =
    @tailrec
    def rec(remaining: List[Batch[Tile]], yOffset: Int, term: TerminalEmulator): TerminalEmulator =
      remaining match
        case Nil =>
          term

        case x :: xs =>
          rec(
            xs,
            yOffset + 1,
            term.putTileLine(startCoords + Point(0, yOffset), x, fgColor, bgColor)
          )

    rec(textLines.toList, 0, this)

  def get(coords: Point): Option[MapTile] =
    charMap.get(coords)

  def delete(coords: Point): TerminalEmulator =
    this.copy(charMap = charMap.remove(coords))

  def clear: TerminalEmulator =
    this.copy(charMap = charMap.clear)

  /** Returns all MapTiles, guarantees order, inserts a default where there is a gap. */
  def toTileBatch: Batch[MapTile] =
    charMap.toBatch(Terminal.EmptyTile)

  /** Returns all MapTiles in a given region, guarantees order, inserts a default where there is a
    * gap.
    */
  def toTileBatch(region: Rectangle): Batch[MapTile] =
    charMap.toBatch(region, Terminal.EmptyTile)

  /** Returns all MapTiles, does not guarantee order, does not fill in gaps. */
  def toBatch: Batch[MapTile] =
    charMap.toBatch.collect { case Some(v) => v }

  /** Returns all MapTiles in a given region, does not guarantee order, does not fill in gaps. */
  def toBatch(region: Rectangle): Batch[MapTile] =
    charMap.toBatch(region)

  /** Returns all MapTiles with their grid positions, does not guarantee order (but the position is
    * given), does not fill in gaps.
    */
  def toPositionedBatch: Batch[(Point, MapTile)] =
    charMap.toPositionedBatch

  /** Returns all MapTiles with their grid positions in a given region, does not guarantee order
    * (but the position is given), does not fill in gaps.
    */
  def toPositionedBatch(region: Rectangle): Batch[(Point, MapTile)] =
    charMap.toPositionedBatch(region)

  def |+|(otherConsole: Terminal): TerminalEmulator =
    combine(otherConsole)
  def combine(otherConsole: Terminal): TerminalEmulator =
    this.copy(
      charMap = charMap.put(
        otherConsole.toPositionedBatch
      )
    )

  def inset(otherConsole: Terminal, offset: Point): TerminalEmulator =
    put(otherConsole.toPositionedBatch, offset)

  def toRogueTerminalEmulator: RogueTerminalEmulator =
    RogueTerminalEmulator(size).inset(this, Point.zero)

  private def liftModifier(
      modifier: (Point, MapTile) => MapTile
  ): (Point, Option[MapTile]) => Option[MapTile] =
    (pt, opt) =>
      opt match
        case None        => None
        case Some(value) => Option(modifier(pt, value))

  def modifyAt(position: Point)(modifier: MapTile => MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.modifyAt(position)(_.map(modifier))
    )

  def map(modifier: (Point, MapTile) => MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.map(liftModifier(modifier))
    )

  def mapRectangle(region: Rectangle)(modifier: (Point, MapTile) => MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.mapRectangle(region)(liftModifier(modifier))
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
    mapRectangle(region)((_, _) => MapTile(tile, foreground, background))

  def mapCircle(circle: Circle)(modifier: (Point, MapTile) => MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.mapCircle(circle)(liftModifier(modifier))
    )

  def fillCircle(circle: Circle, mapTile: MapTile): TerminalEmulator =
    mapCircle(circle)((_, _) => mapTile)
  def fillCircle(circle: Circle, tile: Tile): TerminalEmulator =
    mapCircle(circle)((_, mt) => mt.withChar(tile))
  def fillCircle(circle: Circle, tile: Tile, foreground: RGBA): TerminalEmulator =
    mapCircle(circle)((_, mt) => MapTile(tile, foreground, mt.background))
  def fillCircle(circle: Circle, tile: Tile, foreground: RGBA, background: RGBA): TerminalEmulator =
    mapCircle(circle)((_, _) => MapTile(tile, foreground, background))

  def mapLine(from: Point, to: Point)(modifier: (Point, MapTile) => MapTile): TerminalEmulator =
    this.copy(
      charMap = charMap.mapLine(from, to)(liftModifier(modifier))
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
    mapLine(line.start.toPoint, line.end.toPoint)((_, _) => MapTile(tile, foreground, background))
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
    mapLine(from, to)((_, _) => MapTile(tile, foreground, background))

  def toASCII(nullReplacement: Tile): String =
    charMap
      .toBatch(MapTile(Tile.fromInt(nullReplacement.toInt)))
      .map(_.char.toInt)
      .map {
        case 0 => nullReplacement.toInt.toChar
        case c => c.toInt.toChar
      }
      .toJSArray
      .sliding(size.width, size.width)
      .map(_.mkString)
      .mkString("\n")

  def toASCII: String =
    toASCII(Tile.SPACE)

object TerminalEmulator:
  def apply(screenSize: Size): TerminalEmulator =
    TerminalEmulator(
      screenSize,
      SparseGrid[MapTile](screenSize)
    )

  def stringToTileBatch(text: String): Batch[Tile] =
    Batch.fromArray(text.toCharArray).map { c =>
      Tile.charCodes.get(if c == '\\' then "\\" else c.toString).fold(Tile.SPACE)(Tile.apply)
    }
