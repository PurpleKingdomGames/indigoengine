package io.indigoengine.roguelike.starterkit.terminal

import indigo.*

final case class TerminalText(
    tileMap: AssetName,
    foreground: RGB,
    background: RGBA,
    mask: RGBA,
    shaderId: Option[ShaderId]
) extends Material:

  def withForeground(newColor: RGB): TerminalText =
    this.copy(foreground = newColor)

  def withBackground(newColor: RGBA): TerminalText =
    this.copy(background = newColor)
  def withBackground(newColor: RGB): TerminalText =
    this.copy(background = newColor.toRGBA)

  def withMask(newColor: RGBA): TerminalText =
    this.copy(mask = newColor)
  def withMask(newColor: RGB): TerminalText =
    this.copy(mask = newColor.toRGBA)

  def withShaderId(newShaderId: ShaderId): TerminalText =
    this.copy(shaderId = Option(newShaderId))

  def toShaderData: ShaderData =
    ShaderData(
      shaderId.getOrElse(TerminalText.shaderId),
      Batch(
        UniformBlock(
          UniformBlockName("RogueLikeTextData"),
          Batch(
            Uniform("FOREGROUND") -> vec3(foreground.r, foreground.g, foreground.b),
            Uniform("BACKGROUND") -> vec4(background.r, background.g, background.b, background.a),
            Uniform("MASK")       -> vec4(mask.r, mask.g, mask.b, mask.a)
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
    UltravioletShader.entityFragment(
      shaderId,
      EntityShader.fragment[ShaderImpl.Env](
        ShaderImpl.frag,
        ShaderImpl.Env.ref
      )
    )

  def apply(tileMap: AssetName): TerminalText =
    TerminalText(tileMap, RGB.White, RGBA.Zero, defaultMask, None)

  def apply(tileMap: AssetName, color: RGB): TerminalText =
    TerminalText(tileMap, color, RGBA.Zero, defaultMask, None)

  def apply(tileMap: AssetName, foreground: RGB, background: RGBA): TerminalText =
    TerminalText(tileMap, foreground, background, defaultMask, None)

  def apply(tileMap: AssetName, foreground: RGB, background: RGBA, mask: RGBA): TerminalText =
    TerminalText(tileMap, foreground, background, mask, None)

  object ShaderImpl:

    import ultraviolet.syntax.*

    final case class Env(
        FOREGROUND: vec3,
        BACKGROUND: vec4,
        MASK: vec4
    ) extends FragmentEnvReference

    object Env:
      val ref =
        Env(vec3(0.0f), vec4(0.0f), vec4(0.0f))

    final case class RogueLikeTextData(
        FOREGROUND: vec3,
        BACKGROUND: vec4,
        MASK: vec4
    )

    inline def frag: Shader[Env, Unit] =
      Shader[Env] { env =>
        ubo[RogueLikeTextData]

        def fragment(color: vec4): vec4 =
          val maskDiff: Boolean = abs(env.CHANNEL_0.x - env.MASK.x) < 0.001f &&
            abs(env.CHANNEL_0.y - env.MASK.y) < 0.001f &&
            abs(env.CHANNEL_0.z - env.MASK.z) < 0.001f &&
            abs(env.CHANNEL_0.w - env.MASK.w) < 0.001f

          if (maskDiff) {
            env.BACKGROUND
          } else {
            vec4(env.CHANNEL_0.rgb * (env.FOREGROUND.rgb * env.CHANNEL_0.a), env.CHANNEL_0.a)
          }
      }
