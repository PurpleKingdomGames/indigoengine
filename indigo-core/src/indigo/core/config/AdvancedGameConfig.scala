package indigo.core.config

/** Additional settings to help tune aspects of your game's performance.
  *
  * @param batchSize
  *   How many scene nodes to batch together between draws, defaults to 256.
  * @param autoLoadStandardShaders
  *   Should all the standard shaders be made available by default? They can be added individually / manually if you
  *   prefer. Defaults to true, to include them.
  * @param disableContextMenu
  *   By default, context menu on right-click is disable for the canvas.
  */
final case class AdvancedGameConfig(
    batchSize: Int,
    autoLoadStandardShaders: Boolean,
    disableContextMenu: Boolean
) derives CanEqual {

  def withBatchSize(size: Int): AdvancedGameConfig =
    this.copy(batchSize = size)

  def withAutoLoadStandardShaders(autoLoad: Boolean): AdvancedGameConfig =
    this.copy(autoLoadStandardShaders = autoLoad)

  def withContextMenu: AdvancedGameConfig =
    this.copy(disableContextMenu = false)
  def noContextMenu: AdvancedGameConfig =
    this.copy(disableContextMenu = true)

  val asString: String =
    s"""
       |Advanced settings
       |- Render batch size:           ${batchSize.toString}
       |- Auto-Load Shaders:           ${autoLoadStandardShaders.toString}
       |- Disable Context Menu:        ${disableContextMenu.toString}
       |""".stripMargin
}

object AdvancedGameConfig {
  val default: AdvancedGameConfig =
    AdvancedGameConfig(
      batchSize = 256,
      autoLoadStandardShaders = true,
      disableContextMenu = true
    )
}
