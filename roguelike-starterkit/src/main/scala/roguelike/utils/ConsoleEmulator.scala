package roguelike.utils

import indigo._
import indigoextras.trees.QuadTree
import indigoextras.geometry.Vertex

import roguelike.DfTiles

final case class ConsoleEmulator(screenSize: Size, charMap: QuadTree[MapTile]):

  private val coordsList: List[Point] =
    (0 until screenSize.width).flatMap { x =>
      (0 until screenSize.height).map { y =>
        Point(x, y)
      }
    }.toList

  def put(coords: Point, tile: DfTiles.Tile, fgColor: RGB, bgColor: RGBA): ConsoleEmulator =
    this.copy(charMap = charMap.insertElement(MapTile(tile, fgColor, bgColor), Vertex.fromPoint(coords)))
  def put(coords: Point, tile: DfTiles.Tile, fgColor: RGB): ConsoleEmulator =
    this.copy(charMap = charMap.insertElement(MapTile(tile, fgColor), Vertex.fromPoint(coords)))
  def put(coords: Point, tile: DfTiles.Tile): ConsoleEmulator =
    this.copy(charMap = charMap.insertElement(MapTile(tile), Vertex.fromPoint(coords)))

  def put(tiles: List[(Point, MapTile)]): ConsoleEmulator =
    this.copy(charMap = charMap.insertElements(tiles.map(p => (p._2, Vertex.fromPoint(p._1)))))
  def put(tiles: (Point, MapTile)*): ConsoleEmulator =
    put(tiles.toList)

  def get(coords: Point): Option[MapTile] =
    charMap.fetchElementAt(Vertex.fromPoint(coords))

  def toFullList(default: MapTile): List[MapTile] =
    coordsList.map(pt => get(pt).getOrElse(default))

object ConsoleEmulator:
  def apply(screenSize: Size): ConsoleEmulator =
    ConsoleEmulator(screenSize, QuadTree.empty[MapTile](screenSize.width.toDouble, screenSize.height.toDouble))
