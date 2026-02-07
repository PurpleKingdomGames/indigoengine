package example

import indigo.*
import indigo.physics.*
import indigoextras.subsystems.FPSCounter

final class IndigoPhysics extends Game[Unit, Unit, Model]:

  def initialScene(bootData: Unit): Option[SceneName] =
    None

  def scenes(bootData: Unit): NonEmptyBatch[Scene[Unit, Model]] =
    NonEmptyBatch(LoadScene, VolumeScene, BoxesAndBallsScene, BoxesScene, BallsScene)

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit, Model]] =
    Outcome(
      BootResult
        .noData(Config.config.noResize)
        .withSubSystems(
          FPSCounter(PixelatedFont.fontKey, Assets.assets.generated.PixelatedFont)
            .moveTo(Point(10))
        )
        .withAssets(Assets.assets.generated.assetSet)
        .withFonts(PixelatedFont.fontInfo)
    )

  def initialModel(startupData: Unit): Outcome[Model] =
    Outcome(Model.initial(Dice.default))

  def setup(
      bootData: Unit,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def updateModel(
      context: Context[Unit],
      model: Model
  ): GlobalEvent => Outcome[Model] =
    case KeyboardEvent.KeyUp(Key.PAGE_UP) =>
      Outcome(model).addGlobalEvents(SceneEvent.Previous)

    case KeyboardEvent.KeyUp(Key.PAGE_DOWN) =>
      Outcome(model).addGlobalEvents(SceneEvent.Next)

    case _ =>
      Outcome(model)

  def present(
      context: Context[Unit],
      model: Model
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

final case class Model(volume: World[MyTag], balls: World[MyTag], boxes: World[MyTag], boxesAndBalls: World[MyTag])
object Model:
  def initial(dice: Dice): Model =
    Model(
      VolumeScene.world(dice),
      BallsScene.world(dice),
      BoxesScene.world(dice),
      BoxesAndBallsScene.world(dice)
    )

enum MyTag derives CanEqual:
  case Platform
  case StaticCircle
  case Ball
  case Box
