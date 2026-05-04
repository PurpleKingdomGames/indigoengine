package indigo.core.config

import indigoengine.shared.datatypes.RGBA

// TODO: Is this just RenderConfig now? There is something called that, but it's a little different.

/** All the base settings needed to get a game up and running.
  *
  * @param clearColor
  *   Default background colour. Defaults to Black.
  * @param transparentBackground
  *   Make the canvas background transparent.
  * @param batchSize
  *   How many scene nodes to batch together between draws, defaults to 256.
  * @param autoLoadStandardShaders
  *   Should all the standard shaders be made available by default? They can be added individually / manually if you
  *   prefer. Defaults to true, to include them.
  */
final case class GameConfig(
    clearColor: RGBA,
    transparentBackground: Boolean,
    batchSize: Int,
    autoLoadStandardShaders: Boolean
) derives CanEqual:

  lazy val asString: String =
    s"""
       |Standard settings
       |- Clear color:             {red: ${clearColor.r.toString()}, green: ${clearColor.g
        .toString()}, blue: ${clearColor.b
        .toString()}, alpha: ${clearColor.a.toString()}}
       |- Transparent background:  ${transparentBackground.toString}
       |- Render batch size:       ${batchSize.toString}
       |- Auto-Load Shaders:       ${autoLoadStandardShaders.toString}
       |""".stripMargin

  def withClearColor(clearColor: RGBA): GameConfig =
    this.copy(clearColor = clearColor)

  def withTransparentBackground(enabled: Boolean): GameConfig =
    this.copy(transparentBackground = enabled)
  def useTransparentBackground: GameConfig =
    withTransparentBackground(true)
  def noTransparentBackground: GameConfig =
    withTransparentBackground(false)

  def withBatchSize(size: Int): GameConfig =
    this.copy(batchSize = size)

  def withAutoLoadStandardShaders(autoLoad: Boolean): GameConfig =
    this.copy(autoLoadStandardShaders = autoLoad)

object GameConfig:

  val default: GameConfig =
    GameConfig(
      clearColor = RGBA.Black,
      transparentBackground = false,
      batchSize = 256,
      autoLoadStandardShaders = true
    )
