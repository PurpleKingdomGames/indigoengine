package indigo.core.config

/** Additional settings to help tune aspects of your game's performance.
  *
  * @param batchSize
  *   How many scene nodes to batch together between draws, defaults to 256.
  * @param autoLoadStandardShaders
  *   Should all the standard shaders be made available by default? They can be added individually / manually if you
  *   prefer. Defaults to true, to include them.
  */
final case class AdvancedGameConfig(
    batchSize: Int,
    autoLoadStandardShaders: Boolean
) derives CanEqual {

  def withBatchSize(size: Int): AdvancedGameConfig =
    this.copy(batchSize = size)

  def withAutoLoadStandardShaders(autoLoad: Boolean): AdvancedGameConfig =
    this.copy(autoLoadStandardShaders = autoLoad)

  val asString: String =
    s"""
       |Advanced settings
       |- Render batch size:           ${batchSize.toString}
       |- Auto-Load Shaders:           ${autoLoadStandardShaders.toString}
       |""".stripMargin
}

object AdvancedGameConfig {
  val default: AdvancedGameConfig =
    AdvancedGameConfig(
      batchSize = 256,
      autoLoadStandardShaders = true
    )
}
