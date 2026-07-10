package demo

import demo.models.*
import demo.scenes.*
import indigo.*
import indigoextras.subsystems.FPSCounter
import roguelikestarterkit.*

final class RogueLikeGame() extends Game[Unit, Unit, GameModel]:

  val gameId: GameId = GameId("roguelike")

  def initialScene(bootData: Unit): Option[SceneName] =
    Option(ColourWindowScene.name)

  def scenes(bootData: Unit): NonEmptyBatch[Scene[GameModel]] =
    NonEmptyBatch(
      NoTerminalUI,
      ColourWindowScene,
      MultipleWindowsScene,
      LightingScene,
      RogueTerminalEmulatorScene,
      TerminalTextScene,
      TerminalEmulatorScene,
      WindowDemoScene,
      TerminalUI
    )

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit, GameModel]] =
    Outcome(
      BootResult
        .noData(
          Config.config
        )
        .withFonts(RoguelikeTiles.Size10x10.Fonts.fontInfo)
        .withAssets(Assets.assets.assetSetRelative)
        .withShaders(
          indigoextras.ui.shaders.all ++
            roguelikestarterkit.shaders.all ++ Set(
              TerminalTextScene.customShader(ShaderId("my shader"))
            )
        )
        .withSubSystems(
          FPSCounter(
            RoguelikeTiles.Size10x10.Fonts.fontKey,
            Assets.assets.AnikkiSquare10x10,
            Constants.LayerKeys.fps
          ) // .moveTo(Point(10, 10))
        )
    )

  def initialModel(startupData: Unit): Outcome[GameModel] =
    Outcome(GameModel.initial)

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(bootData))

  def updateModel(context: Context, model: GameModel): GlobalEvent => Outcome[GameModel] =
    case KeyboardEvent.KeyUp(Key.PAGE_UP) =>
      Outcome(model).addGlobalEvents(SceneEvent.LoopPrevious)

    case KeyboardEvent.KeyUp(Key.PAGE_DOWN) =>
      Outcome(model).addGlobalEvents(SceneEvent.LoopNext)

    case SceneEvent.SceneChange(_, _, _) =>
      Outcome(model.copy(pointerOverWindows = Batch.empty))

    case ViewportResize(size) =>
      Outcome(model.copy(viewportSize = size))

    case _ =>
      Outcome(model)

  def present(
      context: Context,
      model: GameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Constants.LayerKeys.background -> Layer.empty,
        Constants.LayerKeys.game       -> Layer.empty,
        Constants.LayerKeys.ui         -> Layer.empty
      )
        .addLayers(
          LayerEntry(Constants.LayerKeys.fps, Layer.empty, Magnification.x2)
        )
    )

enum GameEvent extends GlobalEvent:
  case NoOp
  case Log(msg: String)
