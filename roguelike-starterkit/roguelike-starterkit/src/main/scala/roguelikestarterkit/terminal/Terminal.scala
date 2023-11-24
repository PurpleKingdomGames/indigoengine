package roguelikestarterkit.terminal

import indigo.*
import roguelikestarterkit.Tile

trait Terminal:

  /** Put the tile at the given point
    */
  def put(
      coords: Point,
      tile: Tile,
      foregroundColor: RGBA,
      backgroundColor: RGBA
  ): Terminal

  /** Put the tile at the given point
    */
  def put(coords: Point, tile: Tile, foregroundColor: RGBA): Terminal

  /** Put the Tile at the given point
    */
  def put(coords: Point, tile: Tile): Terminal

  /** Put a batch of tiles into the terminal at the positions specified
    */
  def put(tiles: Batch[(Point, MapTile)]): Terminal

  /** Put a batch of tiles into the terminal at the positions specified, with an offset.
    */
  def put(tiles: Batch[(Point, MapTile)], offset: Point): Terminal

  /** Put a batch of tiles into the terminal at the positions specified
    */
  def put(tiles: (Point, MapTile)*): Terminal

  /** Put the MapTile at the given point
    */
  def put(coords: Point, mapTile: MapTile): Terminal

  /** Fill the whole terminal with the supplied value
    */
  def fill(tile: Tile, foregroundColor: RGBA, backgroundColor: RGBA): Terminal

  /** Fill the whole terminal with the supplied value
    */
  def fill(mapTile: MapTile): Terminal

  /** Adds a line of text to the terminal. No attempt is made to wrap text.
    */
  def putLine(
      startCoords: Point,
      text: String,
      foregroundColor: RGBA,
      backgroundColor: RGBA
  ): Terminal

  /** Add many lines to the terminal
    */
  def putLines(
      startCoords: Point,
      textLines: Batch[String],
      foregroundColor: RGBA,
      backgroundColor: RGBA
  ): Terminal

  /** Retrieves the MapTile at a given point, if one is present.
    */
  def get(coords: Point): Option[MapTile]

  /** Removes an entry from the terminal, implementations vary.
    */
  def delete(coords: Point): Terminal

  /** Remove all values from the terminal, behaviour varies by implementation.
    */
  def clear: Terminal

  /** Creates a `TerminalClones` instance of the given map. */
  def toCloneTiles(
      idPrefix: CloneId,
      position: Point,
      charCrops: Batch[(Int, Int, Int, Int)]
  )(makeBlank: (RGBA, RGBA) => Cloneable): TerminalClones =
    Terminal.toCloneTilesFromBatch(
      idPrefix,
      position,
      charCrops,
      toPositionedBatch,
      makeBlank,
      Terminal.toCloneTileData
    )

  /** Creates a `TerminalClones` instance of a defined region of the given map. */
  def toCloneTiles(
      idPrefix: CloneId,
      position: Point,
      charCrops: Batch[(Int, Int, Int, Int)],
      region: Rectangle
  )(makeBlank: (RGBA, RGBA) => Cloneable): TerminalClones =
    Terminal.toCloneTilesFromBatch(
      idPrefix,
      position,
      charCrops,
      toPositionedBatch(region),
      makeBlank,
      Terminal.toCloneTileData
    )

  /** Export the terminal so that it can be rendered as CloneTiles, and supply a modifier funtion to
    * alter the relative position, rotation, and scale of the tile.
    */
  def toCloneTiles(
      idPrefix: CloneId,
      position: Point,
      charCrops: Batch[(Int, Int, Int, Int)],
      modifier: (Point, MapTile) => (Point, Radians, Vector2)
  )(makeBlank: (RGBA, RGBA) => Cloneable): TerminalClones =
    Terminal.toCloneTilesFromBatch(
      idPrefix,
      position,
      charCrops,
      toPositionedBatch,
      makeBlank,
      Terminal.toCloneTileDataWithModifier(modifier)
    )

  /** Export the terminal so that it can be rendered as CloneTiles for a defined region of the given
    * map, and supply a modifier funtion to alter the relative position, rotation, and scale of the
    * tile.
    */
  def toCloneTiles(
      idPrefix: CloneId,
      position: Point,
      charCrops: Batch[(Int, Int, Int, Int)],
      region: Rectangle,
      modifier: (Point, MapTile) => (Point, Radians, Vector2)
  )(makeBlank: (RGBA, RGBA) => Cloneable): TerminalClones =
    Terminal.toCloneTilesFromBatch(
      idPrefix,
      position,
      charCrops,
      toPositionedBatch(region),
      makeBlank,
      Terminal.toCloneTileDataWithModifier(modifier)
    )

  /** Export the terminal as a batch of MapTiles.
    */
  def toBatch: Batch[MapTile]

  /** Export the terminal as a batch of MapTiles for a given region.
    */
  def toBatch(region: Rectangle): Batch[MapTile]

  /** Export terminal as a complete grid of tiles, missing tiles will be given the Tile.Null values
    * and RGBA.Zero colors.
    */
  def toTileBatch: Batch[MapTile]

  /** Export terminal as a complete grid of tiles for a given region, missing tiles will be given
    * the Tile.Null values and RGBA.Zero colors.
    */
  def toTileBatch(region: Rectangle): Batch[MapTile]

  /** Export the terminal as a batch of maptiles tupled with with their positions.
    */
  def toPositionedBatch: Batch[(Point, MapTile)]

  /** Export the terminal as a batch of maptiles tupled with with their positions, in a given
    * region.
    */
  def toPositionedBatch(region: Rectangle): Batch[(Point, MapTile)]

  /** Merge two terminals together
    */
  def |+|(otherConsole: Terminal): Terminal

  /** Merge two terminals together
    */
  def combine(otherConsole: Terminal): Terminal

  /** Inset one terminal inside another at some position offset.
    */
  def inset(otherConsole: Terminal, offset: Point): Terminal

  def modifyAt(position: Point)(modifier: MapTile => MapTile): Terminal

  def map(modifier: (Point, MapTile) => MapTile): Terminal

  def mapRectangle(region: Rectangle)(modifier: (Point, MapTile) => MapTile): Terminal
  def fillRectangle(region: Rectangle, mapTile: MapTile): Terminal
  def fillRectangle(region: Rectangle, tile: Tile): Terminal
  def fillRectangle(region: Rectangle, tile: Tile, foreground: RGBA): Terminal
  def fillRectangle(region: Rectangle, tile: Tile, foreground: RGBA, background: RGBA): Terminal

  def mapCircle(circle: Circle)(modifier: (Point, MapTile) => MapTile): Terminal
  def fillCircle(circle: Circle, mapTile: MapTile): Terminal
  def fillCircle(circle: Circle, tile: Tile): Terminal
  def fillCircle(circle: Circle, tile: Tile, foreground: RGBA): Terminal
  def fillCircle(circle: Circle, tile: Tile, foreground: RGBA, background: RGBA): Terminal

  def mapLine(from: Point, to: Point)(modifier: (Point, MapTile) => MapTile): Terminal
  def mapLine(line: LineSegment)(modifier: (Point, MapTile) => MapTile): Terminal
  def fillLine(line: LineSegment, mapTile: MapTile): Terminal
  def fillLine(line: LineSegment, tile: Tile): Terminal
  def fillLine(line: LineSegment, tile: Tile, foreground: RGBA): Terminal
  def fillLine(line: LineSegment, tile: Tile, foreground: RGBA, background: RGBA): Terminal
  def fillLine(from: Point, to: Point, mapTile: MapTile): Terminal
  def fillLine(from: Point, to: Point, tile: Tile): Terminal
  def fillLine(from: Point, to: Point, tile: Tile, foreground: RGBA): Terminal
  def fillLine(from: Point, to: Point, tile: Tile, foreground: RGBA, background: RGBA): Terminal

object Terminal:

  private[terminal] lazy val EmptyTile: MapTile =
    MapTile(Tile.NULL, RGBA.Zero, RGBA.Zero)

  private[terminal] def toCloneTilesFromBatch(
      idPrefix: CloneId,
      position: Point,
      charCrops: Batch[(Int, Int, Int, Int)],
      positionedBatch: Batch[(Point, MapTile)],
      makeBlank: (RGBA, RGBA) => Cloneable,
      toCloneTileData: (
          Point,
          Batch[(Int, Int, Int, Int)],
          Batch[(Point, MapTile)]
      ) => Batch[CloneTileData]
  ): TerminalClones =
    val makeId: (RGBA, RGBA) => CloneId = (fg, bg) =>
      CloneId(s"""${idPrefix.toString}_${fg.hashCode}_${bg.hashCode}""")

    val combinations: Batch[((CloneId, RGBA, RGBA), Batch[(Point, MapTile)])] =
      Batch.fromMap(
        positionedBatch
          .groupBy(p =>
            (makeId(p._2.foreground, p._2.background), p._2.foreground, p._2.background)
          )
      )

    val results =
      combinations.map { c =>
        (
          CloneBlank(c._1._1, makeBlank(c._1._2, c._1._3)),
          CloneTiles(c._1._1, toCloneTileData(position, charCrops, c._2))
        )
      }

    TerminalClones(results.map(_._1), results.map(_._2))

  private[terminal] def toCloneTileData(
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

  private[terminal] def toCloneTileDataWithModifier(
      modifier: (Point, MapTile) => (Point, Radians, Vector2)
  )(
      position: Point,
      charCrops: Batch[(Int, Int, Int, Int)],
      data: Batch[(Point, MapTile)]
  ): Batch[CloneTileData] =
    data.map { case (pt, t) =>
      val crop      = charCrops(t.char.toInt)
      val (p, r, s) = modifier(pt, t)
      CloneTileData(
        (pt.x * crop._3) + position.x + p.x,
        (pt.y * crop._4) + position.y + p.y,
        r,
        s.x,
        s.y,
        crop._1,
        crop._2,
        crop._3,
        crop._4
      )
    }
