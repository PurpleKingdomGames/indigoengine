package indigo.core.config

import indigoengine.shared.datatypes.RGBA

/** The knobs and dials of the engine - render-time settings, batching, and shader bootstrapping.
  *
  * @param clearColor
  *   Default background colour. Defaults to Black.
  * @param transparentBackground
  *   Make the background transparent.
  * @param batchSize
  *   How many scene nodes to batch together between draws, defaults to 256.
  * @param autoLoadStandardShaders
  *   Should all the standard shaders be made available by default? They can be added individually / manually if you
  *   prefer. Defaults to true, to include them.
  */
final case class EngineConfig(
    clearColor: RGBA,
    transparentBackground: Boolean,
    batchSize: Int,
    autoLoadStandardShaders: Boolean
) derives CanEqual:

  lazy val asString: String =
    s"""
       |Engine settings
       |- Clear color:             {red: ${clearColor.r.toString()}, green: ${clearColor.g
        .toString()}, blue: ${clearColor.b
        .toString()}, alpha: ${clearColor.a.toString()}}
       |- Transparent background:  ${transparentBackground.toString}
       |- Render batch size:       ${batchSize.toString}
       |- Auto-Load Shaders:       ${autoLoadStandardShaders.toString}
       |""".stripMargin

  def withClearColor(clearColor: RGBA): EngineConfig =
    this.copy(clearColor = clearColor)

  def withTransparentBackground(enabled: Boolean): EngineConfig =
    this.copy(transparentBackground = enabled)
  def useTransparentBackground: EngineConfig =
    withTransparentBackground(true)
  def noTransparentBackground: EngineConfig =
    withTransparentBackground(false)

  def withBatchSize(size: Int): EngineConfig =
    this.copy(batchSize = size)

  def withAutoLoadStandardShaders(autoLoad: Boolean): EngineConfig =
    this.copy(autoLoadStandardShaders = autoLoad)

object EngineConfig:

  val default: EngineConfig =
    EngineConfig(
      clearColor = RGBA.Black,
      transparentBackground = false,
      batchSize = 256,
      autoLoadStandardShaders = true
    )
