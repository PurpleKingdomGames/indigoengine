package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import indigo.*
import indigo.scenes.*
import ultraviolet.syntax.*

import scala.annotation.nowarn

object TexelScene extends Scene[SandboxGameModel] {

  type SceneModel = SandboxGameModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("texel")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext,
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def present(
      context: SceneContext,
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] = {
    val outline = TexelOutlineMaterial(SandboxAssets.dots)

    val background =
      Graphic(790, 380, SandboxAssets.foliageMaterial)

    val wholeTexture =
      Graphic(32, 32, outline)
        .moveTo(10, 10)
        .scaleBy(3.0, 3.0)

    val croppedTexture =
      Graphic(Size(16), outline)
        .withCrop(16, 16, 16, 16)
        .moveTo(116, 10)
        .scaleBy(6.0, 6.0)

    val spriteFrame =
      model.dude.dude.sprite
        .withMaterial(TexelOutlineMaterial(SandboxAssets.dudeName))
        .withRef(0, 0)
        .moveTo(222, 10)
        .scaleBy(3.0, 3.0)

    val noTexture =
      BlankEntity(328, 10, 32, 32, ShaderData(TexelShaders.borderId))
        .scaleBy(3.0, 3.0)

    val stripes =
      BlankEntity(0, 112, 360, 144, ShaderData(TexelShaders.stripesId))

    val magnifiedDots =
      Graphic(32, 32, Material.Bitmap(SandboxAssets.dots))
        .moveTo(8, 30)

    val magnifiedCrate =
      Graphic(32, 32, Material.Bitmap(SandboxAssets.cratesDiffuseName))
        .moveTo(50, 30)

    Outcome(
      SceneUpdateFragment.empty
        .addLayers(
          LayerEntry(LayerKey("texel background"), Layer.Content(background), Magnification.x1),
          LayerEntry(
            LayerKey("texel entities"),
            Layer.Content(stripes, wholeTexture, croppedTexture, spriteFrame, noTexture),
            Magnification.x1
          ),
          LayerEntry(
            LayerKey("texel blend"),
            Layer.Content(magnifiedDots, magnifiedCrate).withBlendMaterial(TexelBlendMaterial),
            Magnification.x4
          )
        )
    )
  }

}

final case class TexelOutlineMaterial(texture: AssetName) extends Material derives CanEqual:
  lazy val toShaderData: ShaderData =
    ShaderData(TexelShaders.outlineId).withChannel0(texture)

case object TexelBlendMaterial extends BlendMaterial.SrcAndDst derives CanEqual {
  lazy val toShaderData: ShaderData =
    ShaderData(TexelShaders.edgeBlendId)
}

object TexelShaders:

  val outlineId: ShaderId =
    ShaderId("texel outline")

  // TEXEL_SIZE is in UV space, so a neighbouring pixel is found in UV space and then converted to atlas coordinates.
  // SIZE / ATLAS_SIZE is used because CHANNEL_0_SIZE covers the whole texture, rather than the frame.
  @nowarn("msg=unused")
  inline def outlineFragment: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { env =>
      def insideFrame(uv: vec2): Float =
        step(0.0f, uv.x) * step(uv.x, 1.0f) * step(0.0f, uv.y) * step(uv.y, 1.0f)

      def alphaAt(uv: vec2): Float =
        insideFrame(uv) * texture2D(env.SRC_CHANNEL, env.CHANNEL_0_POSITION + uv * (env.SIZE / env.ATLAS_SIZE)).a

      def fragment(color: vec4): vec4 =
        val horizontal = max(
          alphaAt(env.UV + vec2(env.TEXEL_SIZE.x, 0.0f)),
          alphaAt(env.UV - vec2(env.TEXEL_SIZE.x, 0.0f))
        )

        val vertical = max(
          alphaAt(env.UV + vec2(0.0f, env.TEXEL_SIZE.y)),
          alphaAt(env.UV - vec2(0.0f, env.TEXEL_SIZE.y))
        )

        val outline = max(horizontal, vertical) * (1.0f - env.CHANNEL_0.a)

        vec4(env.CHANNEL_0.rgb * env.CHANNEL_0.a + vec3(1.0f, 0.0f, 1.0f) * outline, env.CHANNEL_0.a + outline)
    }

  val outline: UltravioletShader =
    UltravioletShader.entityFragment(
      outlineId,
      EntityShader.fragment(outlineFragment, FragmentEnv.reference)
    )

  val borderId: ShaderId =
    ShaderId("texel border")

  @nowarn("msg=unused")
  inline def borderFragment: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { env =>
      def fragment(color: vec4): vec4 =
        val insideX = step(env.TEXEL_SIZE.x, env.UV.x) * step(env.UV.x, 1.0f - env.TEXEL_SIZE.x)
        val insideY = step(env.TEXEL_SIZE.y, env.UV.y) * step(env.UV.y, 1.0f - env.TEXEL_SIZE.y)
        val border  = 1.0f - insideX * insideY

        vec4(vec3(0.0f, 1.0f, 0.0f) * border, border)
    }

  val border: UltravioletShader =
    UltravioletShader.entityFragment(
      borderId,
      EntityShader.fragment(borderFragment, FragmentEnv.reference)
    )

  val stripesId: ShaderId =
    ShaderId("texel stripes")

  // Vertical black and white bars, each 8 screen pixels wide, for the blend layer to shift.
  @nowarn("msg=unused")
  inline def stripesFragment: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { env =>
      def fragment(color: vec4): vec4 =
        val pixelX = env.UV.x / env.TEXEL_SIZE.x

        vec4(vec3(step(0.5f, fract(pixelX / 16.0f))), 1.0f)
    }

  val stripes: UltravioletShader =
    UltravioletShader.entityFragment(
      stripesId,
      EntityShader.fragment(stripesFragment, FragmentEnv.reference)
    )

  val edgeBlendId: ShaderId =
    ShaderId("texel edge blend")

  // SRC is a magnified layer, so its outline is `magnification` screen pixels thick. DST is screen sized, so an offset
  // of 8 DST texels is half a period of the bars behind the layer, and inverts them exactly. The layer is x4 because
  // the bars would also invert at an odd magnification if the offset was wrongly scaled by it. At x4 they'd line up.
  @nowarn("msg=unused")
  inline def edgeBlendFragment: Shader[BlendFragmentEnv, Unit] =
    Shader[BlendFragmentEnv] { env =>
      def alphaAt(uv: vec2): Float =
        texture2D(env.SRC_CHANNEL, uv).a

      def fragment(color: vec4): vec4 =
        val horizontal = max(
          alphaAt(env.UV + vec2(env.SRC_TEXEL_SIZE.x, 0.0f)),
          alphaAt(env.UV - vec2(env.SRC_TEXEL_SIZE.x, 0.0f))
        )

        val vertical = max(
          alphaAt(env.UV + vec2(0.0f, env.SRC_TEXEL_SIZE.y)),
          alphaAt(env.UV - vec2(0.0f, env.SRC_TEXEL_SIZE.y))
        )

        val outline = max(horizontal, vertical) * (1.0f - env.SRC.a)
        val shifted = texture2D(env.DST_CHANNEL, env.UV + vec2(env.DST_TEXEL_SIZE.x * 8.0f, 0.0f))
        val inside  = shifted.rgb * env.SRC.a

        vec4(inside + vec3(0.0f, 1.0f, 1.0f) * outline, env.SRC.a + outline)
    }

  val edgeBlend: UltravioletShader =
    UltravioletShader.blendFragment(
      edgeBlendId,
      BlendShader.fragment(edgeBlendFragment, BlendFragmentEnv.reference)
    )
