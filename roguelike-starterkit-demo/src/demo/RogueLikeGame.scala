package demo

import demo.models.*
import demo.scenes.*
import indigo.next.*
import indigoextras.subsystems.FPSCounter
import roguelikestarterkit.*

final class RogueLikeGame() extends IndigoNext[Size, Size, GameModel]:

  def initialScene(bootData: Size): Option[SceneName] =
    Option(TerminalUI.name)

  def scenes(bootData: Size): NonEmptyBatch[Scene[Size, GameModel]] =
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

  def boot(flags: Map[String, String]): Outcome[BootResult[Size, GameModel]] =
    Outcome(
      BootResult(
        Config.config.withMagnification(Constants.magnification).noResize,
        Config.config.viewport.size / 2
      )
        .withFonts(RoguelikeTiles.Size10x10.Fonts.fontInfo)
        .withAssets(Assets.assets.assetSet)
        .withShaders(
          indigoextras.ui.shaders.all ++
            roguelikestarterkit.shaders.all ++ Set(
              TerminalTextScene.customShader(ShaderId("my shader"))
            )
        )
        .withSubSystems(
          FPSCounter(
            RoguelikeTiles.Size10x10.Fonts.fontKey,
            Assets.assets.AnikkiSquare10x10
          ).moveTo(Point(10, 350))
        )
    )

  def initialModel(startupData: Size): Outcome[GameModel] =
    Outcome(GameModel.initial)

  def setup(bootData: Size, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Size]] =
    Outcome(Startup.Success(bootData))

  def updateModel(context: Context[Size], model: GameModel): GlobalEvent => Outcome[GameModel] =
    case KeyboardEvent.KeyUp(Key.PAGE_UP) =>
      Outcome(model).addGlobalEvents(SceneEvent.LoopPrevious)

    case KeyboardEvent.KeyUp(Key.PAGE_DOWN) =>
      Outcome(model).addGlobalEvents(SceneEvent.LoopNext)

    case BridgeEvent.Receive(MsgData.Log(msg)) =>
      IndigoLogger.info(msg)
      Outcome(model)
        .addGlobalEvents(BridgeEvent.Send(MsgData.Log(s"Indigo says: ${msg.reverse}")))

    case SceneEvent.SceneChange(_, _, _) =>
      Outcome(model.copy(pointerOverWindows = Batch.empty))

    case _ =>
      Outcome(model)

  def present(
      context: Context[Size],
      model: GameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

enum MsgData extends BridgeData:
  case Log(msg: String)

enum GameEvent extends GlobalEvent:
  case NoOp
  case Log(msg: String)
