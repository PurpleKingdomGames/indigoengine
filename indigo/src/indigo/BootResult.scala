package indigo

import indigo.shaders.ShaderProgram
import indigo.shared.subsystems.SubSystem

/** The game bootstrapping process results in a `BootResult`, which only occurs once on initial game load. The boot
  * result describes all of the initial values of your game such as it's configuration, data, animations, assets, fonts,
  * subsystems, and shaders. You can add additional assets, animations, fonts, and shaders later during the setup
  * process, so it is recommended that you only load the bare minimum needed to get your game going during the boot
  * phase.
  */
final case class BootResult[BootData, Model](
    gameConfig: GameConfig,
    bootData: BootData,
    animations: Set[Animation],
    assets: Set[AssetType],
    fonts: Set[FontInfo],
    subSystems: Set[SubSystem[Model]],
    shaders: Set[ShaderProgram]
) derives CanEqual {

  def addAnimations(newAnimations: Set[Animation]): BootResult[BootData, Model] =
    this.copy(animations = animations ++ newAnimations)
  def addAnimations(newAnimations: Animation*): BootResult[BootData, Model] =
    addAnimations(newAnimations.toSet)
  def withAnimations(newAnimations: Set[Animation]): BootResult[BootData, Model] =
    this.copy(animations = newAnimations)
  def withAnimations(newAnimations: Animation*): BootResult[BootData, Model] =
    withAnimations(newAnimations.toSet)

  def addAssets(newAssets: Set[AssetType]): BootResult[BootData, Model] =
    this.copy(assets = assets ++ newAssets)
  def addAssets(newAssets: AssetType*): BootResult[BootData, Model] =
    addAssets(newAssets.toSet)
  def withAssets(newAssets: Set[AssetType]): BootResult[BootData, Model] =
    this.copy(assets = newAssets)
  def withAssets(newAssets: AssetType*): BootResult[BootData, Model] =
    withAssets(newAssets.toSet)

  def addFonts(newFonts: Set[FontInfo]): BootResult[BootData, Model] =
    this.copy(fonts = fonts ++ newFonts)
  def addFonts(newFonts: FontInfo*): BootResult[BootData, Model] =
    addFonts(newFonts.toSet)
  def withFonts(newFonts: Set[FontInfo]): BootResult[BootData, Model] =
    this.copy(fonts = newFonts)
  def withFonts(newFonts: FontInfo*): BootResult[BootData, Model] =
    withFonts(newFonts.toSet)

  def addSubSystems(newSubSystems: Set[SubSystem[Model]]): BootResult[BootData, Model] =
    this.copy(subSystems = subSystems ++ newSubSystems)
  def addSubSystems(newSubSystems: SubSystem[Model]*): BootResult[BootData, Model] =
    addSubSystems(newSubSystems.toSet)
  def withSubSystems(newSubSystems: Set[SubSystem[Model]]): BootResult[BootData, Model] =
    this.copy(subSystems = newSubSystems)
  def withSubSystems(newSubSystems: SubSystem[Model]*): BootResult[BootData, Model] =
    withSubSystems(newSubSystems.toSet)

  def addShaders(newShaders: Set[ShaderProgram]): BootResult[BootData, Model] =
    this.copy(shaders = shaders ++ newShaders)
  def addShaders(newShaders: ShaderProgram*): BootResult[BootData, Model] =
    addShaders(newShaders.toSet)
  def withShaders(newShaders: Set[ShaderProgram]): BootResult[BootData, Model] =
    this.copy(shaders = newShaders)
  def withShaders(newShaders: ShaderProgram*): BootResult[BootData, Model] =
    withShaders(newShaders.toSet)

}
object BootResult {
  def apply[BootData, Model](gameConfig: GameConfig, bootData: BootData): BootResult[BootData, Model] =
    new BootResult[BootData, Model](gameConfig, bootData, Set(), Set(), Set(), Set(), Set())

  def noData[Model](gameConfig: GameConfig): BootResult[Unit, Model] =
    apply(gameConfig, ())
  def configOnly[Model](gameConfig: GameConfig): BootResult[Unit, Model] =
    noData(gameConfig)

  def default[Model]: BootResult[Unit, Model] =
    noData(GameConfig.default)
}
