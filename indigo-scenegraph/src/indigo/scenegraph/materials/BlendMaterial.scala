package indigo.scenegraph.materials

import indigo.core.datatypes.Fill
import indigo.shaders.ShaderData
import indigo.shaders.ShaderPrimitive.rawBatch
import indigo.shaders.StandardShaders
import indigo.shaders.Uniform
import indigo.shaders.UniformBlock
import indigo.shaders.UniformBlockName
import indigo.shaders.UniformDataHelpers
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.RGB
import indigoengine.shared.datatypes.RGBA

/** BlendMaterials describe how Indigo should blend or composite the current layer onto the accumulated scene so far.
  */
sealed trait BlendMaterial:

  /** The ShaderData that tells the rendering pipeline what you need to render using this BlendMaterial.
    */
  def toShaderData: ShaderData

object BlendMaterial {

  extension (bm: BlendMaterial)
    /** Signals if this BlendMaterial can read from the destination, or not.
      */
    def supportsDestinationRead: Boolean =
      bm match
        case _: SrcAndDst => true
        case _: SrcOnly   => false

  /** When in doubt, extend `SrcAndDst`, this blend material uses the default render pipeline.
    *
    * A `SrcAndDst` blend material's shader samples the destination (the result composited so far, via `DST_CHANNEL`).
    * Compositing it requires the full render path, including a copy of the accumulated scene for the shader to read.
    */
  trait SrcAndDst extends BlendMaterial

  /** When in doubt, extend `SrcAndDst`. `SrcOnly` allows for faster rendering at the expense of not being able to read
    * from the destination / target texture buffer.
    *
    * A `SrcOnly` blend material's shader does **not** sample the destination - its output depends only on the source
    * layer. This lets the renderer blend it straight onto the scene in place, skipping the destination copy that
    * `SrcAndDst` materials need. Only extend `SrcOnly` if the shader never reads `DST_CHANNEL`.
    *
    * Note: The `DST_CHANNEL` and `DST` variables are still available to the shader via the environment, but they are
    * not set in a SrcOnly shader and may error if you attempt to reference them.
    */
  trait SrcOnly extends BlendMaterial

  case object Normal extends BlendMaterial.SrcOnly derives CanEqual {
    lazy val toShaderData: ShaderData =
      ShaderData(StandardShaders.NormalBlend.id)
  }

  final case class Lighting(ambient: RGBA) extends BlendMaterial.SrcAndDst derives CanEqual {
    lazy val toShaderData: ShaderData =
      ShaderData(
        StandardShaders.LightingBlend.id,
        Batch(
          UniformBlock(
            UniformBlockName("IndigoLightingBlendData"),
            Batch(
              Uniform("AMBIENT_LIGHT_COLOR") -> rawBatch(
                ambient.r.toFloat,
                ambient.g.toFloat,
                ambient.b.toFloat,
                ambient.a.toFloat
              )
            )
          )
        )
      )
  }

  final case class BlendEffects(
      alpha: Double,
      tint: RGBA,
      overlay: Fill,
      saturation: Double,
      affectsBackground: Boolean
  ) extends BlendMaterial.SrcAndDst derives CanEqual {

    def withAlpha(newAlpha: Double): BlendEffects =
      this.copy(alpha = newAlpha)

    def withTint(newTint: RGBA): BlendEffects =
      this.copy(tint = newTint)
    def withTint(newTint: RGB): BlendEffects =
      this.copy(tint = newTint.toRGBA)

    def withOverlay(newOverlay: Fill): BlendEffects =
      this.copy(overlay = newOverlay)

    def withSaturation(newSaturation: Double): BlendEffects =
      this.copy(saturation = newSaturation)

    def withAffectBackground(affectsBg: Boolean): BlendMaterial =
      this.copy(affectsBackground = affectsBg)
    def applyToBackground: BlendMaterial =
      this.copy(affectsBackground = true)
    def ignoreBackground: BlendMaterial =
      this.copy(affectsBackground = false)

    lazy val toShaderData: ShaderData = {
      val overlayType: Float =
        overlay match {
          case _: Fill.Color          => 0.0
          case _: Fill.LinearGradient => 1.0
          case _: Fill.RadialGradient => 2.0
        }

      ShaderData(
        StandardShaders.BlendEffects.id,
        Batch(
          UniformBlock(
            UniformBlockName("IndigoBlendEffectsData"),
            Batch(
              // ALPHA_SATURATION_OVERLAYTYPE_BG (vec4), TINT (vec4)
              Uniform("BlendEffects_DATA") -> rawBatch(
                Batch(
                  alpha.toFloat,
                  saturation.toFloat,
                  overlayType,
                  if (affectsBackground) 1.0f else 0.0f,
                  tint.r.toFloat,
                  tint.g.toFloat,
                  tint.b.toFloat,
                  tint.a.toFloat
                )
              )
            ) ++ UniformDataHelpers.fillToUniformData(overlay, "BlendEffects")
          )
        )
      )
    }
  }
  object BlendEffects {
    val None: BlendEffects =
      BlendEffects(1.0, RGBA.None, Fill.Color.default, 1.0, false)

    def apply(alpha: Double): BlendEffects =
      BlendEffects(alpha, RGBA.None, Fill.Color.default, 1.0, false)
  }

}
