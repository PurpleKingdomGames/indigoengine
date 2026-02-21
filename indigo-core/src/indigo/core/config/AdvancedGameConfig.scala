package indigo.core.config

import indigoengine.shared.datatypes.Millis

/** Additional settings to help tune aspects of your game's performance.
  *
  * @param renderingTechnology
  *   Use WebGL 1.0 or 2.0? Defaults to 2.0 with fallback to 1.0.
  * @param antiAliasing
  *   Smooth the rendered view? Defaults to false.
  * @param batchSize
  *   How many scene nodes to batch together between draws, defaults to 256.
  * @param premultipliedAlpha
  *   Should the renderer use premultiplied alpha? All the standard shaders expect the answer to be yes! Disable with
  *   caution, defaults to true.
  * @param autoLoadStandardShaders
  *   Should all the standard shaders be made available by default? They can be added individually / manually if you
  *   prefer. Defaults to true, to include them.
  * @param disableContextMenu
  *   By default, context menu on right-click is disable for the canvas.
  */
final case class AdvancedGameConfig(
    antiAliasing: Boolean,
    batchSize: Int,
    premultipliedAlpha: Boolean,
    autoLoadStandardShaders: Boolean,
    disableContextMenu: Boolean,
    clickTime: Millis
) derives CanEqual {

  def withAntiAliasing(enabled: Boolean): AdvancedGameConfig =
    this.copy(antiAliasing = enabled)
  def useAntiAliasing: AdvancedGameConfig =
    withAntiAliasing(true)
  def noAntiAliasing: AdvancedGameConfig =
    withAntiAliasing(false)

  def withPremultipliedAlpha(enabled: Boolean): AdvancedGameConfig =
    this.copy(premultipliedAlpha = enabled)
  def usePremultipliedAlpha: AdvancedGameConfig =
    withPremultipliedAlpha(true)
  def noPremultipliedAlpha: AdvancedGameConfig =
    withPremultipliedAlpha(false)

  def withBatchSize(size: Int): AdvancedGameConfig =
    this.copy(batchSize = size)

  def withAutoLoadStandardShaders(autoLoad: Boolean): AdvancedGameConfig =
    this.copy(autoLoadStandardShaders = autoLoad)

  def withContextMenu: AdvancedGameConfig =
    this.copy(disableContextMenu = false)
  def noContextMenu: AdvancedGameConfig =
    this.copy(disableContextMenu = true)

  def withClickTime(millis: Millis): AdvancedGameConfig =
    this.copy(clickTime = millis)

  val asString: String =
    s"""
       |Advanced settings
       |- AntiAliasing enabled:        ${antiAliasing.toString}
       |- Render batch size:           ${batchSize.toString}
       |- Pre-Multiplied Alpha:        ${premultipliedAlpha.toString}
       |- Auto-Load Shaders:           ${autoLoadStandardShaders.toString}
       |- Disable Context Menu:        ${disableContextMenu.toString}
       |- Click Time (ms):             ${clickTime.toString}
       |""".stripMargin
}

object AdvancedGameConfig {
  val default: AdvancedGameConfig =
    AdvancedGameConfig(
      antiAliasing = false,
      premultipliedAlpha = true,
      batchSize = 256,
      autoLoadStandardShaders = true,
      disableContextMenu = true,
      clickTime = Millis(250)
    )
}
