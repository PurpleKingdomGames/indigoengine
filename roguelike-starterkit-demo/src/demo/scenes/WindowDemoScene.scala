package demo.scenes

import demo.RogueLikeGame
import demo.models.Model
import demo.models.ViewModel
import demo.windows.DemoWindow
import indigo.*
import indigo.scenes.*
import indigoextras.ui.*

object WindowDemoScene extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("window demo scene")

  val modelLens: Lens[Model, Model] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, ViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[Model]] =
    Set(
      WindowManager[Model](
        SubSystemId("demo window manager"),
        RogueLikeGame.magnification
      )
        .withLayerKey(LayerKey("UI Layer"))
        .register(
          DemoWindow.window
        )
        .open(DemoWindow.windowId)
        .focus(DemoWindow.windowId)
    )

  def updateModel(
      context: SceneContext[Size],
      model: Model
  ): GlobalEvent => Outcome[Model] =
    case WindowEvent.Closed(id) =>
      val ids = model.pointerOverWindows.filterNot(_ == id)

      Outcome(model.copy(pointerOverWindows = ids))

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
    Outcome(
      SceneUpdateFragment(
        LayerKey("UI Layer") -> Layer.Stack.empty
      )
    )
