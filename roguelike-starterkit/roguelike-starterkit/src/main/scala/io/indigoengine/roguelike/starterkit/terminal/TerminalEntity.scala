package io.indigoengine.roguelike.starterkit.terminal

import indigo.ShaderPrimitive.*
import indigo.*
import indigo.shared.datatypes.RGBA
import io.indigoengine.roguelike.starterkit.Tile

final case class TerminalEntity(
    tileSheet: AssetName,
    gridSize: Size,
    charSize: Size,
    mask: RGBA,
    map: Batch[MapTile],
    position: Point,
    depth: Depth,
    maxTileCount: Int
) extends EntityNode[TerminalEntity]:
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

  def withMap(newMap: Batch[MapTile]): TerminalEntity =
    this.copy(map = newMap)

  def withDepth(newDepth: Depth): TerminalEntity =
    this.copy(depth = newDepth)

  private val count       = gridSize.width * gridSize.height
  private val emptyColors = Batch.fromArray(Array.fill((maxTileCount - count) * 4)(0.0f))

  private lazy val fgArray: scalajs.js.Array[Float] =
    (map.flatMap { t =>
      val color = t.foreground
      Batch(t.char.toFloat, color.r.toFloat, color.g.toFloat, color.b.toFloat)
    } ++ emptyColors).toJSArray

  private lazy val bgArray: scalajs.js.Array[Float] =
    (map.flatMap { t =>
      val color = t.background
      Batch(color.r.toFloat, color.g.toFloat, color.b.toFloat, color.a.toFloat)
    } ++ emptyColors).toJSArray

  def toShaderData: ShaderData =
    ShaderData(
      TerminalEntity.shaderId,
      UniformBlock(
        UniformBlockName("RogueLikeData"),
        Batch(
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
        UniformBlockName("RogueLikeMapForeground"),
        Batch(
          Uniform("CHAR_FOREGROUND") -> rawJSArray(fgArray)
        )
      ),
      UniformBlock(
        UniformBlockName("RogueLikeMapBackground"),
        Batch(
          Uniform("BACKGROUND") -> rawJSArray(bgArray)
        )
      )
    ).withChannel0(tileSheet)

  def eventHandler: ((TerminalEntity, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)
  def eventHandlerEnabled: Boolean                                         = false

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
      Batch.empty,
      Point.zero,
      Depth.zero,
      maxTileCount
    )

  def apply(
      tileSheet: AssetName,
      gridSize: Size,
      charSize: Size,
      map: Batch[MapTile],
      maxTileCount: Int
  ): TerminalEntity =
    TerminalEntity(
      tileSheet,
      gridSize,
      charSize,
      RGBA.Magenta,
      map,
      Point.zero,
      Depth.zero,
      maxTileCount
    )

  val shaderId: ShaderId =
    ShaderId("roguelike terminal map shader")

  def shader(maxTileCount: Int): UltravioletShader =
    UltravioletShader.entityFragment(
      shaderId,
      EntityShader.fragment[ShaderImpl.Env](
        ShaderImpl.frag,
        ShaderImpl.Env.ref
      )
    )
     // TODO: Remove old shader + shader gen code.

  object ShaderImpl:

    import ultraviolet.syntax.*

    final case class Env(
        GRID_DIMENSIONS_CHAR_SIZE: vec4,
        MASK: vec4,
        CHAR_FOREGROUND: array[4000, vec4], // TODO: should be MAX_TILE_COUNT
        BACKGROUND: array[4000, vec4]       // TODO: should be MAX_TILE_COUNT
    ) extends FragmentEnvReference

    object Env:
      val ref =
        Env(
          vec4(0.0f),
          vec4(0.0f),
          array[4000, vec4](), // TODO: should be MAX_TILE_COUNT
          array[4000, vec4]()  // TODO: should be MAX_TILE_COUNT
        )

    final case class RogueLikeData(
        GRID_DIMENSIONS_CHAR_SIZE: vec4,
        MASK: vec4
    )

    final case class RogueLikeMapForeground(
        CHAR_FOREGROUND: array[4000, vec4] // TODO: should be MAX_TILE_COUNT
    )

    final case class RogueLikeMapBackground(
        BACKGROUND: array[4000, vec4] // TODO: should be MAX_TILE_COUNT
    )

    inline def frag: Shader[Env, Unit] =
      Shader[Env] { env =>
        @define val MAX_TILE_COUNT: Int = 4000

        ubo[RogueLikeData]
        ubo[RogueLikeMapForeground]
        ubo[RogueLikeMapBackground]

        def fragment(color: vec4): vec4 =
          val GRID_DIMENSIONS: vec2 = env.GRID_DIMENSIONS_CHAR_SIZE.xy
          val CHAR_SIZE: vec2       = env.GRID_DIMENSIONS_CHAR_SIZE.zw
          val ONE_TEXEL: vec2       = env.CHANNEL_0_SIZE / env.TEXTURE_SIZE

          // Which grid square am I in on the map? e.g. 3x3, coords (1,1)
          val gridSquare: vec2 = env.UV * GRID_DIMENSIONS

          // Which sequential box is that? e.g. 4 of 9
          val index: Int = (floor(gridSquare.y) * GRID_DIMENSIONS.x + floor(gridSquare.x)).toInt

          // Which character is that? e.g. position 4 in the array is for char 64, which is '@'
          val charIndex: Int = env.CHAR_FOREGROUND(index).x.toInt

          // Where on the texture is the top left of the relevant character cell?
          val cellX: Float = (charIndex % 16).toFloat / 16.0f  // TODO: Problem with modulus?
          val cellY: Float = floor(charIndex.toFloat / 16.0f) * (1.0f / 16.0f)
          val cell: vec2   = vec2(cellX, cellY)

          // What are the relative UV coords?
          val tileSize: vec2 = ONE_TEXEL * CHAR_SIZE
          val relUV: vec2 =
            env.CHANNEL_0_POSITION + (cell * env.CHANNEL_0_SIZE) + (tileSize * fract(gridSquare))

          val c: vec4 = texture2D(env.SRC_CHANNEL, relUV)

          val maskDiff: Boolean = abs(c.x - env.MASK.x) < 0.001f &&
            abs(c.y - env.MASK.y) < 0.001f &&
            abs(c.z - env.MASK.z) < 0.001f &&
            abs(c.w - env.MASK.w) < 0.001f

          if (maskDiff) {
            env.BACKGROUND(index)
          } else {
            vec4(c.rgb * (env.CHAR_FOREGROUND(index).gba * c.a), c.a)
          }

      }

final case class MapTile(char: Tile, foreground: RGBA, background: RGBA):
  def withChar(newChar: Tile): MapTile =
    this.copy(char = newChar)

  def withForegroundColor(newColor: RGBA): MapTile =
    this.copy(foreground = newColor)

  def withBackgroundColor(newColor: RGBA): MapTile =
    this.copy(background = newColor)

object MapTile:
  def apply(char: Tile): MapTile =
    MapTile(char, RGBA.White, RGBA.Zero)

  def apply(char: Tile, foreground: RGBA): MapTile =
    MapTile(char, foreground, RGBA.Zero)
