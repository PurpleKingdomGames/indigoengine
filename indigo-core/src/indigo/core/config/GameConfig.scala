package indigo.core.config

import indigo.core.datatypes.Rectangle
import indigoengine.shared.datatypes.RGBA

/** All the base settings needed to get a game up and running.
  *
  * @param viewport
  *   How big is the window initially? Defaults to 550 x 400 pixels.
  * @param clearColor
  *   Default background colour. Defaults to Black.
  * @param magnification
  *   Pixel magnification level. Defaults to 1.
  * @param resizePolicy
  *   Sets the policy for how Indigo games should resize themselves.
  * @param transparentBackground
  *   Make the canvas background transparent.
  * @param advanced
  *   Additional settings to help tune your game.
  */
final case class GameConfig(
    viewport: GameViewport,
    clearColor: RGBA,
    magnification: Int,
    resizePolicy: ResizePolicy,
    transparentBackground: Boolean,
    advanced: AdvancedGameConfig
) derives CanEqual:

  def screenDimensions: Rectangle =
    viewport.giveDimensions(magnification)

  lazy val asString: String =
    s"""
       |Standard settings
       |- Viewpoint:       [${viewport.width.toString()}, ${viewport.height.toString()}]
       |- Clear color:     {red: ${clearColor.r.toString()}, green: ${clearColor.g.toString()}, blue: ${clearColor.b
        .toString()}, alpha: ${clearColor.a.toString()}}
       |- Magnification:   ${magnification.toString()}
       |- Resize Policy:   ${resizePolicy.toString()}
       |${advanced.asString}
       |""".stripMargin

  def withViewport(width: Int, height: Int): GameConfig =
    this.copy(viewport = GameViewport(width, height))
  def withViewport(newViewport: GameViewport): GameConfig =
    this.copy(viewport = newViewport)

  def withClearColor(clearColor: RGBA): GameConfig =
    this.copy(clearColor = clearColor)
  def withMagnification(magnification: Int): GameConfig =
    this.copy(magnification = magnification)

  def withAdvancedSettings(advanced: AdvancedGameConfig): GameConfig =
    this.copy(advanced = advanced)
  def modifyAdvancedSettings(modify: AdvancedGameConfig => AdvancedGameConfig): GameConfig =
    this.copy(advanced = modify(advanced))

  def withTransparentBackground(enabled: Boolean): GameConfig =
    this.copy(transparentBackground = enabled)
  def useTransparentBackground: GameConfig =
    withTransparentBackground(true)
  def noTransparentBackground: GameConfig =
    withTransparentBackground(false)

  def withResizePolicy(resizePolicy: ResizePolicy): GameConfig =
    this.copy(resizePolicy = resizePolicy)
  def noResize: GameConfig =
    withResizePolicy(ResizePolicy.NoResize)
  def autoResize: GameConfig =
    withResizePolicy(ResizePolicy.Resize)
  def autoResizePreserveAspect: GameConfig =
    withResizePolicy(ResizePolicy.ResizePreserveAspect)

object GameConfig:

  val default: GameConfig =
    GameConfig(
      viewport = GameViewport(550, 400),
      clearColor = RGBA.Black,
      magnification = 1,
      transparentBackground = false,
      resizePolicy = ResizePolicy.Resize,
      advanced = AdvancedGameConfig.default
    )

  def apply(width: Int, height: Int): GameConfig =
    GameConfig(
      viewport = GameViewport(width, height),
      clearColor = RGBA.Black,
      magnification = 1,
      transparentBackground = false,
      resizePolicy = ResizePolicy.Resize,
      advanced = AdvancedGameConfig.default
    )

  def apply(viewport: GameViewport, clearColor: RGBA, magnification: Int): GameConfig =
    GameConfig(
      viewport = viewport,
      clearColor = clearColor,
      magnification = magnification,
      transparentBackground = false,
      resizePolicy = ResizePolicy.Resize,
      advanced = AdvancedGameConfig.default
    )

  def apply(width: Int, height: Int, clearColor: RGBA, magnification: Int): GameConfig =
    GameConfig(
      viewport = GameViewport(width, height),
      clearColor = clearColor,
      magnification = magnification,
      transparentBackground = false,
      resizePolicy = ResizePolicy.Resize,
      advanced = AdvancedGameConfig.default
    )

/** ResizePolicy instructs Indigo on how you would like the game to handle a change in viewport size.
  */
enum ResizePolicy derives CanEqual:
  case NoResize, Resize, ResizePreserveAspect
