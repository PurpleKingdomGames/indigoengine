package roguelikestarterkit.terminal

import indigo.*
import indigo.syntax.shaders.*

/** `TerminalMaterial` is a revised and leaner version of `TerminalText`, aimed at use with
  * `CloneTiles`. It removes the dubious drop shadow functionality, requires less shader data, and
  * has simpler shader logic.
  */
final case class TerminalMaterial(
    tileMap: AssetName,
    foreground: RGBA,
    background: RGBA,
    mask: RGBA,
    shaderId: Option[ShaderId]
) extends Material:

  def withForeground(newColor: RGBA): TerminalMaterial =
    this.copy(foreground = newColor)
  def withForeground(newColor: RGB): TerminalMaterial =
    withForeground(newColor.toRGBA)

  def withBackground(newColor: RGBA): TerminalMaterial =
    this.copy(background = newColor)
  def withBackground(newColor: RGB): TerminalMaterial =
    withBackground(newColor.toRGBA)

  def withMask(newColor: RGBA): TerminalMaterial =
    this.copy(mask = newColor)
  def withMask(newColor: RGB): TerminalMaterial =
    withMask(newColor.toRGBA)

  def withShaderId(newShaderId: ShaderId): TerminalMaterial =
    this.copy(shaderId = Option(newShaderId))

  def toShaderData: ShaderData =
    ShaderData(
      shaderId.getOrElse(TerminalMaterial.shaderId),
      Batch(
        UniformBlock(
          UniformBlockName("RogueLikeTextData"),
          Batch(
            Uniform("FOREGROUND") -> foreground.asVec4,
            Uniform("BACKGROUND") -> background.asVec4,
            Uniform("MASK")       -> mask.asVec4
          )
        )
      ),
      Some(tileMap),
      None,
      None,
      None
    )

object TerminalMaterial:

  val defaultMask: RGBA =
    RGBA.Magenta

  val shaderId: ShaderId =
    ShaderId("roguelike standard terminal material")

  def standardShader: UltravioletShader =
    UltravioletShader.entityFragment(
      shaderId,
      EntityShader.fragment[ShaderImpl.Env](
        ShaderImpl.frag,
        ShaderImpl.Env.ref
      )
    )

  def apply(tileMap: AssetName): TerminalMaterial =
    TerminalMaterial(tileMap, RGBA.White, RGBA.Zero, defaultMask, None)

  def apply(tileMap: AssetName, color: RGBA): TerminalMaterial =
    TerminalMaterial(tileMap, color, RGBA.Zero, defaultMask, None)

  def apply(tileMap: AssetName, foreground: RGBA, background: RGBA): TerminalMaterial =
    TerminalMaterial(tileMap, foreground, background, defaultMask, None)

  def apply(
      tileMap: AssetName,
      foreground: RGBA,
      background: RGBA,
      mask: RGBA
  ): TerminalMaterial =
    TerminalMaterial(tileMap, foreground, background, mask, None)

  object ShaderImpl:

    import ultraviolet.syntax.*

    final case class Env(
        FOREGROUND: vec4,
        BACKGROUND: vec4,
        MASK: vec4
    ) extends FragmentEnvReference

    object Env:
      val ref =
        Env(vec4(0.0f), vec4(0.0f), vec4(0.0f))

    final case class RogueLikeTextData(
        FOREGROUND: vec4,
        BACKGROUND: vec4,
        MASK: vec4
    )

    inline def frag: Shader[Env, Unit] =
      Shader[Env] { env =>
        ubo[RogueLikeTextData]

        // Calculate the distance from the color to the mask, and snap to 0.0 or 1.0 over a low threshold.
        def maskAmount(color: vec4, mask: vec4): Float =
          val v = abs(color - mask)
          step(0.001f, v.x + v.y + v.z + v.w)

        def fragment(color: vec4): vec4 =
          val bg   = vec4(env.BACKGROUND.rgb * env.BACKGROUND.a, env.BACKGROUND.a)
          val tint = env.CHANNEL_0 * env.FOREGROUND
          val fg   = vec4(tint.rgb * tint.a, tint.a)

          mix(bg, fg, maskAmount(env.CHANNEL_0, env.MASK))
      }
