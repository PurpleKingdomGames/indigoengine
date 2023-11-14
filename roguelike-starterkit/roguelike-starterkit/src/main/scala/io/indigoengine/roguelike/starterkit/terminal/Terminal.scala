package io.indigoengine.roguelike.starterkit.terminal

import indigo.*
import io.indigoengine.roguelike.starterkit.Tile

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

  /** Export the terminal so that it can be rendered as CloneTiles
    */
  def toCloneTiles(
      idPrefix: CloneId,
      position: Point,
      charCrops: Batch[(Int, Int, Int, Int)]
  )(makeBlank: (RGBA, RGBA) => Cloneable): TerminalClones

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

object Terminal:

  private[terminal] lazy val EmptyTile: MapTile =
    MapTile(Tile.NULL, RGBA.Zero, RGBA.Zero)
