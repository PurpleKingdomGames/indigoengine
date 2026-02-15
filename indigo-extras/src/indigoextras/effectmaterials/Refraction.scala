package indigoextras.effectmaterials

import indigo.core.assets.AssetName
import indigo.core.datatypes.Rectangle
import indigo.scenegraph.Blend
import indigo.scenegraph.Blending
import indigo.scenegraph.materials.BlendMaterial
import indigo.scenegraph.materials.FillType
import indigo.scenegraph.materials.Material
import indigo.shaders.BlendShader
import indigo.shaders.EntityShader
import indigo.shaders.ShaderData
import indigo.shaders.ShaderId
import indigo.shaders.ShaderPrimitive.float
import indigo.shaders.ShaderPrimitive.rawBatch
import indigo.shaders.ShaderProgram
import indigo.shaders.UltravioletShader
import indigo.shaders.Uniform
import indigo.shaders.UniformBlock
import indigo.shaders.UniformBlockName
import indigo.shaders.library.NoOp
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.RGBA
import indigoextras.effectmaterials.shaders.RefractionShaders

object Refraction:

  val entityShader: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigoextras_engine_normal_minus_blue]"),
      EntityShader.vertex(NoOp.vertex, ()),
      EntityShader.fragment(
        RefractionShaders.normalMinusBlue,
        RefractionShaders.FragEnv.reference
      )
    )

  val blendShader: UltravioletShader =
    UltravioletShader(
      ShaderId("[indigoextras_engine_blend_refraction]"),
      BlendShader.vertex(NoOp.vertex, ()),
      BlendShader.fragment(
        RefractionShaders.refractionFragment,
        RefractionShaders.BlendEnv.reference
      )
    )

  val shaders: Set[ShaderProgram] =
    Set(entityShader, blendShader)

  /** Replicates Indigo's original refraction/distortion layer behaviour
    *
    * The problem with this method is that we have no "entity blend shader" capability to allow use to control how
    * individual entities blend onto the layer below. As a result we have to use the same sort of mechanism we use for
    * lighting to combine the entities - but this results in a weaker effect than we would like.
    *
    * @param distance
    *   Max distance in pixels
    */
  def blending(distance: Double): Blending =
    Blending(Blend.Normal, Blend.Normal, RefractionBlend(distance), Option(RGBA.Zero))

final case class RefractionEntity(diffuse: AssetName, fillType: FillType) extends Material derives CanEqual:

  def withDiffuse(newDiffuse: AssetName): RefractionEntity =
    this.copy(diffuse = newDiffuse)

  def withFillType(newFillType: FillType): RefractionEntity =
    this.copy(fillType = newFillType)
  def normal: RefractionEntity =
    withFillType(FillType.Normal)
  def stretch: RefractionEntity =
    withFillType(FillType.Stretch)
  def tile: RefractionEntity =
    withFillType(FillType.Tile)
  def nineSlice(center: Rectangle): RefractionEntity =
    withFillType(FillType.NineSlice(center))
  def nineSlice(top: Int, right: Int, bottom: Int, left: Int): RefractionEntity =
    withFillType(FillType.NineSlice(top, right, bottom, left))

  lazy val toShaderData: ShaderData =
    val imageFillType: Double =
      fillType match
        case FillType.Normal       => 0.0
        case FillType.Stretch      => 1.0
        case FillType.Tile         => 2.0
        case FillType.NineSlice(_) => 3.0

    val nineSliceCenter: Batch[Float] =
      fillType match
        case FillType.NineSlice(center) =>
          Batch(
            center.x.toFloat,
            center.y.toFloat,
            center.width.toFloat,
            center.height.toFloat
          )

        case _ =>
          Batch(0.0f, 0.0f, 0.0f, 0.0f)

    val uniformBlock: UniformBlock =
      UniformBlock(
        UniformBlockName("IndigoBitmapData"),
        Batch(
          Uniform("FILLTYPE")          -> float(imageFillType),
          Uniform("NINE_SLICE_CENTER") -> rawBatch(nineSliceCenter)
        )
      )

    ShaderData(
      Refraction.entityShader.id,
      Batch(uniformBlock),
      Some(diffuse),
      None,
      None,
      None
    )

object RefractionEntity:
  def apply(diffuse: AssetName): RefractionEntity =
    RefractionEntity(diffuse, FillType.Normal)

final case class RefractionBlend(multiplier: Double) extends BlendMaterial derives CanEqual:
  lazy val toShaderData: ShaderData =
    ShaderData(
      Refraction.blendShader.id,
      Batch(
        UniformBlock(
          UniformBlockName("IndigoRefractionBlendData"),
          Batch(
            Uniform("REFRACTION_AMOUNT") -> float(multiplier)
          )
        )
      )
    )
