package roguelike

import indigo._
import indigo.ShaderPrimitive._

final case class MapRenderer(
    position: Point,
    size: Size,
    depth: Depth
) extends EntityNode:
  def flip: Flip        = Flip.default
  def ref: Point        = Point.zero
  def rotation: Radians = Radians.zero
  def scale: Vector2    = Vector2.one

  def withDepth(newDepth: Depth): MapRenderer =
    this.copy(depth = newDepth)

  def toShaderData: ShaderData =
    ShaderData(
      MapRenderer.shaderId,
      UniformBlock(
        "MapData",
        List(
          Uniform("MAP_SIZE")  -> vec2(3, 3),
          Uniform("CHAR_SIZE") -> vec2(10, 10),
          Uniform("CHARS") -> array(9)(
            float(176),
            float(176),
            float(176),
            float(176),
            float(64),
            float(176),
            float(176),
            float(176),
            float(176)
          )
        )
      )
    ).withChannel0(Assets.charsName)

object MapRenderer:

  val shaderId: ShaderId =
    ShaderId("map shader")

  def shader(vertProgram: AssetName, fragProgram: AssetName): EntityShader =
    EntityShader
      .External(shaderId)
      .withVertexProgram(vertProgram)
      .withFragmentProgram(fragProgram)
