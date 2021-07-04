package roguelike.terminal

import indigo._
import indigo.ShaderPrimitive._

import roguelike.DfTiles

final case class TerminalEntity(
    tileSheet: AssetName,
    gridSize: Size,
    charSize: Size,
    mask: RGBA,
    map: List[MapTile],
    position: Point,
    depth: Depth
) extends EntityNode:
  def flip: Flip        = Flip.default
  def ref: Point        = Point.zero
  def rotation: Radians = Radians.zero
  def scale: Vector2    = Vector2.one
  def size: Size        = gridSize * charSize

  def moveTo(pt: Point): TerminalEntity =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): TerminalEntity =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): TerminalEntity =
    moveTo(newPosition)

  def moveBy(pt: Point): TerminalEntity =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): TerminalEntity =
    moveBy(Point(x, y))

  def withTileSheet(newTileSheet: AssetName): TerminalEntity =
    this.copy(tileSheet = newTileSheet)

  def withGridSize(newGridSize: Size): TerminalEntity =
    this.copy(gridSize = newGridSize)

  def withCharSize(newCharSize: Size): TerminalEntity =
    this.copy(charSize = newCharSize)

  def withMask(newColor: RGBA): TerminalEntity =
    this.copy(mask = newColor)
  def withMask(newColor: RGB): TerminalEntity =
    this.copy(mask = newColor.toRGBA)

  def withMap(newMap: List[MapTile]): TerminalEntity =
    this.copy(map = newMap)

  def withDepth(newDepth: Depth): TerminalEntity =
    this.copy(depth = newDepth)

  private val count       = gridSize.width * gridSize.height
  private val total       = 4096
  private val emptyColors = Array.fill(total - count)(vec4(0.0f, 0.0f, 0.0f, 0.0f))

  private lazy val fgArray =
    (map.map { t =>
      val color = t.foreground
      vec4(t.char.toInt.toFloat, color.r.toFloat, color.g.toFloat, color.b.toFloat)
    } ++ emptyColors).toArray

  private lazy val bgArray =
    (map.map { t =>
      val color = t.background
      vec4(color.r.toFloat, color.g.toFloat, color.b.toFloat, color.a.toFloat)
    } ++ emptyColors).toArray

  def toShaderData: ShaderData =
    ShaderData(
      TerminalEntity.shaderId,
      UniformBlock(
        "RogueLikeData",
        List(
          Uniform("GRID_DIMENSIONS_CHAR_SIZE") -> vec4(
            gridSize.width.toFloat,
            gridSize.height.toFloat,
            charSize.width.toFloat,
            charSize.height.toFloat
          ),
          Uniform("MASK") -> vec4(mask.r, mask.g, mask.b, mask.a)
        )
      ),
      UniformBlock(
        "RogueLikeMapForeground",
        List(
          Uniform("CHAR_FOREGROUND") -> array(total, fgArray)
        )
      ),
      UniformBlock(
        "RogueLikeMapBackground",
        List(
          Uniform("BACKGROUND") -> array(total, bgArray)
        )
      )
    ).withChannel0(tileSheet)

object TerminalEntity:

  def apply(tileSheet: AssetName, gridSize: Size, charSize: Size): TerminalEntity =
    TerminalEntity(tileSheet, gridSize, charSize, RGBA.Magenta, Nil, Point.zero, Depth(1))

  def apply(tileSheet: AssetName, gridSize: Size, charSize: Size, map: List[MapTile]): TerminalEntity =
    TerminalEntity(tileSheet, gridSize, charSize, RGBA.Magenta, map, Point.zero, Depth(1))

  val shaderId: ShaderId =
    ShaderId("map shader")

  def shader(fragProgram: AssetName): EntityShader =
    EntityShader
      .External(shaderId)
      .withFragmentProgram(fragProgram)

final case class MapTile(char: DfTiles.Tile, foreground: RGB, background: RGBA)
object MapTile:
  def apply(char: DfTiles.Tile): MapTile =
    MapTile(char, RGB.White, RGBA.Zero)

  def apply(char: DfTiles.Tile, foreground: RGB): MapTile =
    MapTile(char, foreground, RGBA.Zero)
