package demo

import indigo._
import indigo.scenes._
import indigoextras.subsystems.FPSCounter
import io.indigoengine.roguelike.starterkit.*

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object RogueLikeGame extends IndigoGame[Unit, Unit, Unit, Unit]:

  val maxTileCount: Int = 4000

  def initialScene(bootData: Unit): Option[SceneName] =
    Option(StartScene.name)

  def scenes(bootData: Unit): NonEmptyList[Scene[Unit, Unit, Unit]] =
    NonEmptyList(StartScene, GameScene, CloneScene)

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val fps: Int = 60

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome(
      BootResult
        .noData(
          GameConfig.default
            .withMagnification(1)
            .withFrameRate(fps)
        )
        .withFonts(RoguelikeTiles.Size10x10.Fonts.fontInfo)
        .withAssets(Assets.assets)
        .withShaders(
          TerminalEntity.shader(maxTileCount),
          TerminalText.standardShader,
          TerminalText.customShader(ShaderId("my shader"), Assets.textFragShader)
        )
        .withSubSystems(FPSCounter(Point(10, 350), fps))
    )

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def initialViewModel(startupData: Unit, model: Unit): Outcome[Unit] =
    Outcome(())

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[Unit],
      model: Unit,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[Unit],
      model: Unit,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
