package demo

import indigo.*
import indigo.scenes.*
import roguelikestarterkit.*

object UISubSystemScene extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("UI subsystem scene")

  val modelLens: Lens[Model, Model] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, ViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set(
      WindowManager(
        SubSystemId("window manager"),
        RogueLikeGame.magnification,
        charSheet = Model.defaultCharSheet
      )
        .register(
          ColourWindow.window(
            Model.defaultCharSheet
          )
        )
    )

  def updateModel(
      context: SceneContext[Size],
      model: Model
  ): GlobalEvent => Outcome[Model] =
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model).addGlobalEvents(SceneEvent.Next)

    case KeyboardEvent.KeyUp(Key.KEY_O) =>
      Outcome(model).addGlobalEvents(WindowManagerEvent.OpenAt(ColourWindow.windowId, Coords(1, 1)))

    case WindowEvent.MouseOver(id) =>
      println("Mouse over window: " + id)
      Outcome(model)

    case WindowEvent.MouseOut(id) =>
      println("Mouse out window: " + id)
      Outcome(model)

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
