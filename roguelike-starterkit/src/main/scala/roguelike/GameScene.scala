package roguelike

import indigo._
import indigo.scenes._

import roguelike.utils.{MapRenderer, TerminalText}

object GameScene extends Scene[Unit, Unit, Unit]:

  type SceneModel     = Unit
  type SceneViewModel = Unit

  val name: SceneName =
    SceneName("game scene")

  val modelLens: Lens[Unit, Unit] =
    Lens.keepLatest

  val viewModelLens: Lens[Unit, Unit] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(StartScene.name))

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: FrameContext[Unit],
      model: Unit,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  val size = Size(30)

  val mapRenderer: MapRenderer =
    MapRenderer(Assets.tileMap, Size(3, 3), Size(10, 10))

  def present(context: FrameContext[Unit], model: Unit, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        mapRenderer.withMap(
          List(176, 176, 176, 176, 64, 176, 176, 176, 176)
        )
      )
    )
