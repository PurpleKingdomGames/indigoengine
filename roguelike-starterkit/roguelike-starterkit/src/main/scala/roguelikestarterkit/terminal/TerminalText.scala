package roguelikestarterkit.terminal

import indigo.*
import indigo.syntax.shaders.*

/** The original Terminal text material, designed for use with `Text` entities. Supports approximate
  * drop shadows.
  */
final case class TerminalText(
    tileMap: AssetName,
    foreground: RGBA,
    background: RGBA,
    shadow: RGBA,
    mask: RGBA,
    shaderId: Option[ShaderId]
) extends Material:

  def withForeground(newColor: RGBA): TerminalText =
    this.copy(foreground = newColor)
  def withForeground(newColor: RGB): TerminalText =
    withForeground(newColor.toRGBA)

  def withBackground(newColor: RGBA): TerminalText =
    this.copy(background = newColor)
  def withBackground(newColor: RGB): TerminalText =
    withBackground(newColor.toRGBA)

  def withDropShadow(newColor: RGBA): TerminalText =
    this.copy(shadow = newColor)
  def withDropShadow(newColor: RGB): TerminalText =
    withDropShadow(newColor.toRGBA)

  def withMask(newColor: RGBA): TerminalText =
    this.copy(mask = newColor)
  def withMask(newColor: RGB): TerminalText =
    withMask(newColor.toRGBA)

  def withShaderId(newShaderId: ShaderId): TerminalText =
    this.copy(shaderId = Option(newShaderId))

  def toShaderData: ShaderData =
    ShaderData(
      shaderId.getOrElse(TerminalText.shaderId),
      Batch(
        UniformBlock(
          UniformBlockName("RogueLikeTextData"),
          Batch(
            Uniform("FOREGROUND") -> foreground.asVec4,
            Uniform("BACKGROUND") -> background.asVec4,
            Uniform("SHADOW")     -> shadow.asVec4,
            Uniform("MASK")       -> mask.asVec4
          )
        )
      ),
      Some(tileMap),
      None,
      None,
      None
    )

object TerminalText:

  val defaultMask: RGBA =
    RGBA.Magenta

  val shaderId: ShaderId =
    ShaderId("roguelike standard terminal text")

  def standardShader: UltravioletShader =
    UltravioletShader(
      shaderId,
      EntityShader.vertex(
        ShaderImpl.vert,
        VertexEnv.reference
      ),
      EntityShader.fragment[ShaderImpl.Env](
        ShaderImpl.frag,
        ShaderImpl.Env.ref
      )
    )

  def apply(tileMap: AssetName): TerminalText =
    TerminalText(tileMap, RGBA.White, RGBA.Zero, RGBA.Zero, defaultMask, None)

  def apply(tileMap: AssetName, color: RGBA): TerminalText =
    TerminalText(tileMap, color, RGBA.Zero, RGBA.Zero, defaultMask, None)

  def apply(tileMap: AssetName, foreground: RGBA, background: RGBA): TerminalText =
    TerminalText(tileMap, foreground, background, background, defaultMask, None)

  def apply(tileMap: AssetName, foreground: RGBA, background: RGBA, shadow: RGBA): TerminalText =
    TerminalText(tileMap, foreground, background, shadow, defaultMask, None)

  def apply(
      tileMap: AssetName,
      foreground: RGBA,
      background: RGBA,
      shadow: RGBA,
      mask: RGBA
  ): TerminalText =
    TerminalText(tileMap, foreground, background, shadow, mask, None)

  object ShaderImpl:

    import ultraviolet.syntax.*

    final case class Env(
        FOREGROUND: vec4,
        BACKGROUND: vec4,
        SHADOW: vec4,
        MASK: vec4
    ) extends FragmentEnvReference

    object Env:
      val ref =
        Env(vec4(0.0f), vec4(0.0f), vec4(0.0f), vec4(0.0f))

    final case class RogueLikeTextData(
        FOREGROUND: vec4,
        BACKGROUND: vec4,
        SHADOW: vec4,
        MASK: vec4
    )

    @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
    inline def vert: Shader[VertexEnv, Unit] =
      Shader { env =>
        @out var v_shadowLookupCoord: vec2 = null

        def vertex(v: vec4): vec4 =
          // Create a look-up pointing at the pixel diagonally up and left from the current one,
          // and pass it to the fragment shader.
          val onePixel: vec2 = vec2(1.0f) / env.SIZE
          val adjustedUV     = clamp(env.UV - (onePixel * vec2(1.0f, 1.0f)), vec2(0.0), vec2(1.0))

          v_shadowLookupCoord = (adjustedUV * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET

          v
      }

    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    inline def frag: Shader[Env, Unit] =
      Shader[Env] { env =>
        ubo[RogueLikeTextData]

        @in val v_shadowLookupCoord: vec2 = null

        def isBackgroundColor(color: vec4): Boolean =
          abs(color.x - env.MASK.x) < 0.001f &&
            abs(color.y - env.MASK.y) < 0.001f &&
            abs(color.z - env.MASK.z) < 0.001f &&
            abs(color.w - env.MASK.w) < 0.001f

        def isForgroundColor(color: vec4): Boolean =
          abs(color.x - env.MASK.x) > 0.001f ||
            abs(color.y - env.MASK.y) > 0.001f ||
            abs(color.z - env.MASK.z) > 0.001f ||
            abs(color.w - env.MASK.w) > 0.001f

        def fragment(color: vec4): vec4 =
          val isBackground: Boolean = isBackgroundColor(env.CHANNEL_0)

          val isShadow: Boolean =
            isBackground && isForgroundColor(texture2D(env.SRC_CHANNEL, v_shadowLookupCoord))

          if isShadow then vec4(env.SHADOW.rgb * env.SHADOW.a, env.SHADOW.a)
          else if isBackground then vec4(env.BACKGROUND.rgb * env.BACKGROUND.a, env.BACKGROUND.a)
          else
            val alpha = env.CHANNEL_0.a * env.FOREGROUND.a
            vec4(env.CHANNEL_0.rgb * (env.FOREGROUND.rgb * alpha), alpha)
      }
