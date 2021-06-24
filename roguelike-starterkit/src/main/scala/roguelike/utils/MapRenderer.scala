package roguelike.utils

import indigo._
import indigo.ShaderPrimitive._

final case class MapRenderer(
    tileSheet: AssetName,
    gridSize: Size,
    charSize: Size,
    map: List[Int],
    position: Point,
    depth: Depth
) extends EntityNode:
  def flip: Flip        = Flip.default
  def ref: Point        = Point.zero
  def rotation: Radians = Radians.zero
  def scale: Vector2    = Vector2.one
  def size: Size        = gridSize * charSize

  def moveTo(pt: Point): MapRenderer =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): MapRenderer =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): MapRenderer =
    moveTo(newPosition)

  def moveBy(pt: Point): MapRenderer =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): MapRenderer =
    moveBy(Point(x, y))

  def withTileSheet(newTileSheet: AssetName): MapRenderer =
    this.copy(tileSheet = newTileSheet)

  def withGridSize(newGridSize: Size): MapRenderer =
    this.copy(gridSize = newGridSize)

  def withCharSize(newCharSize: Size): MapRenderer =
    this.copy(charSize = newCharSize)

  def withMap(newMap: List[Int]): MapRenderer =
    this.copy(map = newMap)

  def withDepth(newDepth: Depth): MapRenderer =
    this.copy(depth = newDepth)

  def toShaderData: ShaderData =
    ShaderData(
      MapRenderer.shaderId,
      UniformBlock(
        "RogueLikeMapData",
        List(
          Uniform("GRID_DIMENSIONS") -> vec2(gridSize.width.toFloat, gridSize.height.toFloat),
          Uniform("CHAR_SIZE")       -> vec2(charSize.width.toFloat, charSize.height.toFloat),
          Uniform("CHARS") -> array(gridSize.width * gridSize.height)(
            map.map(c => float(c.toFloat)): _*
          )
        )
      )
    ).withChannel0(tileSheet)

object MapRenderer:

  def apply(tileSheet: AssetName, gridSize: Size, charSize: Size): MapRenderer =
    MapRenderer(tileSheet, gridSize, charSize, Nil, Point.zero, Depth(1))

  val shaderId: ShaderId =
    ShaderId("map shader")

  def shader(vertProgram: AssetName, fragProgram: AssetName): EntityShader =
    EntityShader
      .External(shaderId)
      .withVertexProgram(vertProgram)
      .withFragmentProgram(fragProgram)
