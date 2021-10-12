package roguelike.terminal

import indigo.ShaderPrimitive._
import indigo._
import roguelike.TerminalShaders
import roguelike.Tile

final case class TerminalEntity(
    tileSheet: AssetName,
    gridSize: Size,
    charSize: Size,
    mask: RGBA,
    map: Array[MapTile],
    position: Point,
    depth: Depth,
    maxTileCount: Int
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

  def withMap(newMap: Array[MapTile]): TerminalEntity =
    this.copy(map = newMap)

  def withDepth(newDepth: Depth): TerminalEntity =
    this.copy(depth = newDepth)

  private val count       = gridSize.width * gridSize.height
  private val emptyColors = Array.fill((maxTileCount - count) * 4)(0.0f)

  private lazy val fgArray: Array[Float] =
    (map.flatMap { t =>
      val color = t.foreground
      Array(t.char.toFloat, color.r.toFloat, color.g.toFloat, color.b.toFloat)
    } ++ emptyColors).toArray

  private lazy val bgArray: Array[Float] =
    (map.flatMap { t =>
      val color = t.background
      Array(color.r.toFloat, color.g.toFloat, color.b.toFloat, color.a.toFloat)
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
          Uniform("CHAR_FOREGROUND") -> rawArray(fgArray)
        )
      ),
      UniformBlock(
        "RogueLikeMapBackground",
        List(
          Uniform("BACKGROUND") -> rawArray(bgArray)
        )
      )
    ).withChannel0(tileSheet)

object TerminalEntity:

  def apply(
      tileSheet: AssetName,
      gridSize: Size,
      charSize: Size,
      maxTileCount: Int
  ): TerminalEntity =
    TerminalEntity(
      tileSheet,
      gridSize,
      charSize,
      RGBA.Magenta,
      Array(),
      Point.zero,
      Depth(1),
      maxTileCount
    )

  def apply(
      tileSheet: AssetName,
      gridSize: Size,
      charSize: Size,
      map: Array[MapTile],
      maxTileCount: Int
  ): TerminalEntity =
    TerminalEntity(
      tileSheet,
      gridSize,
      charSize,
      RGBA.Magenta,
      map,
      Point.zero,
      Depth(1),
      maxTileCount
    )

  val shaderId: ShaderId =
    ShaderId("roguelike terminal map shader")

  def shader(maxTileCount: Int): EntityShader =
    EntityShader
      .Source(shaderId)
      .withFragmentProgram(TerminalShaders.TerminalFragment(maxTileCount.toString))

final case class MapTile(char: Tile, foreground: RGB, background: RGBA):
  def withChar(newChar: Tile): MapTile =
    this.copy(char = newChar)

  def withForegroundColor(newColor: RGB): MapTile =
    this.copy(foreground = newColor)

  def withBackgroundColor(newColor: RGBA): MapTile =
    this.copy(background = newColor)

object MapTile:
  def apply(char: Tile): MapTile =
    MapTile(char, RGB.White, RGBA.Zero)

  def apply(char: Tile, foreground: RGB): MapTile =
    MapTile(char, foreground, RGBA.Zero)
