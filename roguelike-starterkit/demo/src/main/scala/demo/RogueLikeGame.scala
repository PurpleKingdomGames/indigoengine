package demo

import demo.models.*
import demo.scenes.*
import indigo.*
import indigo.scenes.*
import indigoextras.subsystems.FPSCounter
import roguelikestarterkit.*

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object RogueLikeGame extends IndigoGame[Size, Size, Model, ViewModel]:

  val magnification: Int = 2

  def initialScene(bootData: Size): Option[SceneName] =
    Option(MultipleWindowsScene.name)

  def scenes(bootData: Size): NonEmptyList[Scene[Size, Model, ViewModel]] =
    NonEmptyList(
      NoTerminalUI,
      ColourWindowScene,
      MultipleWindowsScene,
      LightingScene,
      RogueTerminalEmulatorScene,
      TerminalTextScene,
      TerminalEmulatorScene,
      WindowDemoScene
    )

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Size, Model]] =
    Outcome(
      BootResult(
        Config.config.withMagnification(magnification),
        Config.config.viewport.size / 2
      )
        .withFonts(RoguelikeTiles.Size10x10.Fonts.fontInfo)
        .withAssets(Assets.assets.assetSet)
        .withShaders(
          indigoextras.ui.shaders.all ++
            shaders.all ++ Set(
              TerminalTextScene.customShader(ShaderId("my shader"))
            )
        )
        .withSubSystems(
          FPSCounter(
            Point(10, 350),
            RoguelikeTiles.Size10x10.Fonts.fontKey,
            Assets.assets.AnikkiSquare10x10
          )
        )
    )

  def initialModel(startupData: Size): Outcome[Model] =
    Outcome(Model.initial)

  def initialViewModel(startupData: Size, model: Model): Outcome[ViewModel] =
    Outcome(ViewModel.initial)

  def setup(bootData: Size, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Size]] =
    Outcome(Startup.Success(bootData))

  def updateModel(context: Context[Size], model: Model): GlobalEvent => Outcome[Model] =
    case KeyboardEvent.KeyUp(Key.PAGE_UP) =>
      Outcome(model).addGlobalEvents(SceneEvent.LoopPrevious)

    case KeyboardEvent.KeyUp(Key.PAGE_DOWN) =>
      Outcome(model).addGlobalEvents(SceneEvent.LoopNext)

    case Log(msg) =>
      IndigoLogger.info(msg)
      Outcome(model)

    case SceneEvent.SceneChange(_, _, _) =>
      Outcome(model.copy(pointerOverWindows = Batch.empty))

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: Context[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: Context[Size],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
