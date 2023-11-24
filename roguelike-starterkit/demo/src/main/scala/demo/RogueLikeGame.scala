package demo

import indigo.*
import indigo.scenes.*
import indigoextras.subsystems.FPSCounter
import roguelikestarterkit.*

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object RogueLikeGame extends IndigoGame[Size, Size, Model, ViewModel]:

  def initialScene(bootData: Size): Option[SceneName] =
    None

  def scenes(bootData: Size): NonEmptyList[Scene[Size, Model, ViewModel]] =
    NonEmptyList(UIScene, RogueTerminalEmulatorScene, TerminalTextScene, TerminalEmulatorScene)

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Size]] =
    Outcome(
      BootResult(
        Config.config,
        Config.config.viewport.size / 2
      )
        .withFonts(RoguelikeTiles.Size10x10.Fonts.fontInfo)
        .withAssets(Assets.assets.assetSet)
        .withShaders(
          uiShaders ++ Set(
            TerminalText.standardShader,
            TerminalMaterial.standardShader,
            TerminalTextScene.customShader(ShaderId("my shader"))
          )
        )
        .withSubSystems(FPSCounter(Point(10, 350)))
    )

  def initialModel(startupData: Size): Outcome[Model] =
    Outcome(Model.initial)

  def initialViewModel(startupData: Size, model: Model): Outcome[ViewModel] =
    Outcome(ViewModel.initial(startupData))

  def setup(bootData: Size, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Size]] =
    Outcome(Startup.Success(bootData))

  def updateModel(context: FrameContext[Size], model: Model): GlobalEvent => Outcome[Model] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[Size],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

final case class CustomContext() // Placeholder, not used.

final case class Model(windowManager: WindowManagerModel[Size, CustomContext])

object Model:

  val defaultCharSheet: CharSheet =
    CharSheet(
      Assets.assets.AnikkiSquare10x10,
      Size(10),
      RoguelikeTiles.Size10x10.charCrops
    )

  val initial: Model =
    Model(
      WindowManagerModel
        .initial[Size, CustomContext]
        .add(
          ColourWindow.window(
            defaultCharSheet
          )
        )
    )

final case class ViewModel(windowManager: WindowManagerViewModel[Size, CustomContext])
object ViewModel:
  def initial(viewportSize: Size): ViewModel =
    ViewModel(WindowManagerViewModel.initial)
