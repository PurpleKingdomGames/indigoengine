package roguelikestarterkit.terminal

import indigo.*
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.UniformBlockName
import indigo.shared.shader.library.Lighting
import ultraviolet.syntax.*

import scala.annotation.nowarn

/** `TerminalMaterial` is a revised and leaner version of `TerminalText`, aimed at use with
  * `CloneTiles`. It removes the dubious drop shadow functionality, requires less shader data, and
  * has simpler shader logic.
  */
final case class TerminalMaterial(
    tileMap: AssetName,
    foreground: RGBA,
    background: RGBA,
    mask: RGBA,
    lighting: LightingModel,
    shaderId: Option[ShaderId]
) extends Material:

  def withColors(newForeground: RGBA, newBackground: RGBA): TerminalMaterial =
    this.copy(foreground = newForeground, background = newBackground)
  def withForeground(newForeground: RGB, newBackground: RGB): TerminalMaterial =
    withColors(newForeground.toRGBA, newBackground.toRGBA)

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

  def withLighting(newLighting: LightingModel): TerminalMaterial =
    this.copy(lighting = newLighting)
  def modifyLighting(modifier: LightingModel => LightingModel): TerminalMaterial =
    this.copy(lighting = modifier(lighting))

  def enableLighting: TerminalMaterial =
    withLighting(lighting.enableLighting)
  def disableLighting: TerminalMaterial =
    withLighting(lighting.disableLighting)

  def withShaderId(newShaderId: ShaderId): TerminalMaterial =
    this.copy(shaderId = Option(newShaderId))

  def toShaderData: ShaderData =
    val uniformBlock =
      UniformBlock(
        UniformBlockName("RogueLikeTextData"),
        Batch(
          Uniform("FOREGROUND") -> ShaderPrimitive.vec4.fromRGBA(foreground),
          Uniform("BACKGROUND") -> ShaderPrimitive.vec4.fromRGBA(background),
          Uniform("MASK")       -> ShaderPrimitive.vec4.fromRGBA(mask)
        )
      )

    lighting match
      case LightingModel.Unlit =>
        ShaderData(
          shaderId.getOrElse(TerminalMaterial.shaderId),
          Batch(uniformBlock),
          Some(tileMap),
          None,
          None,
          None
        )

      case l: LightingModel.Lit =>
        l.toShaderData(
          shaderId.getOrElse(TerminalMaterial.litShaderId),
          Some(tileMap),
          Batch(uniformBlock)
        )

object TerminalMaterial:

  val defaultMask: RGBA =
    RGBA.Magenta

  val shaderId: ShaderId =
    ShaderId("roguelike standard terminal material")

  val standardShader: UltravioletShader =
    UltravioletShader.entityFragment(
      shaderId,
      EntityShader.fragment[ShaderImpl.Env](
        ShaderImpl.frag,
        ShaderImpl.Env.ref
      )
    )

  val litShaderId: ShaderId =
    ShaderId("roguelike standard terminal material lit")

  val standardShaderLit: UltravioletShader =
    UltravioletShader.entityFragment(
      litShaderId,
      EntityShader.fragment[ShaderImpl.Env](
        ShaderImpl.frag,
        Lighting.prepare,
        Lighting.light,
        Lighting.composite,
        ShaderImpl.Env.ref
      )
    )

  val shaders: Set[ShaderProgram] =
    Set(standardShader, standardShaderLit)

  def apply(tileMap: AssetName): TerminalMaterial =
    TerminalMaterial(tileMap, RGBA.White, RGBA.Zero, defaultMask, LightingModel.Unlit, None)

  def apply(tileMap: AssetName, color: RGBA): TerminalMaterial =
    TerminalMaterial(tileMap, color, RGBA.Zero, defaultMask, LightingModel.Unlit, None)

  def apply(tileMap: AssetName, foreground: RGBA, background: RGBA): TerminalMaterial =
    TerminalMaterial(tileMap, foreground, background, defaultMask, LightingModel.Unlit, None)

  def apply(
      tileMap: AssetName,
      foreground: RGBA,
      background: RGBA,
      mask: RGBA
  ): TerminalMaterial =
    TerminalMaterial(tileMap, foreground, background, mask, LightingModel.Unlit, None)

  def apply(
      tileMap: AssetName,
      foreground: RGBA,
      background: RGBA,
      mask: RGBA,
      lightingModel: LightingModel
  ): TerminalMaterial =
    TerminalMaterial(tileMap, foreground, background, mask, lightingModel, None)

  object ShaderImpl:

    final case class Env(
        FOREGROUND: vec4,
        BACKGROUND: vec4,
        MASK: vec4
    ) extends FragmentEnvReference
        with Lighting.LightEnv

    object Env:
      val ref =
        Env(vec4(0.0f), vec4(0.0f), vec4(0.0f))

    final case class RogueLikeTextData(
        FOREGROUND: vec4,
        BACKGROUND: vec4,
        MASK: vec4
    )

    @nowarn("msg=unused")
    inline def frag: Shader[Env, Unit] =
      Shader[Env] { env =>
        ubo[RogueLikeTextData]

        // Calculate the distance from the color to the mask, and snap to 0.0 or 1.0 over a low threshold.
        def maskAmount(color: vec4, mask: vec4): Float =
          val v = abs(color - mask)
          step(0.001f, v.x + v.y + v.z + v.w)

        // Calculate the distance to greyscale, so that we can preserve colours.
        // 0 means use the tint, 1 means use the original colour.
        def distanceToGreyscale(color: vec4): Float =
          step(0.001f, abs(((color.x + color.y + color.z) / 3.0f) - color.x))

        def fragment(color: vec4): vec4 =
          val bg = vec4(env.BACKGROUND.rgb * env.BACKGROUND.a, env.BACKGROUND.a)
          val tint =
            mix(env.CHANNEL_0 * env.FOREGROUND, env.CHANNEL_0, distanceToGreyscale(env.CHANNEL_0))
          val fg = vec4(tint.rgb * tint.a, tint.a)

          mix(bg, fg, maskAmount(env.CHANNEL_0, env.MASK))
      }
