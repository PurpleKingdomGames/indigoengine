package demo

import indigo.*
import indigo.scenes.*
import roguelikestarterkit.*

object UIScene extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("UI scene")

  val modelLens: Lens[Model, Model] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, ViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[Model]] =
    Set(
      WindowManager[Model, Int](
        SubSystemId("window manager 2"),
        RogueLikeGame.magnification,
        Model.defaultCharSheet,
        _.mouseOverWindows.length
      )
        .register(
          ComponentsWindow.window(
            Model.defaultCharSheet
          )
        )
        .register(
          ComponentsWindow2.window(
            Model.defaultCharSheet
          )
        )
        .register(
          MenuWindow.window(
            Model.defaultCharSheet
          )
        )
        .open(
          MenuWindow.windowId,
          ComponentsWindow.windowId,
          ComponentsWindow2.windowId
        )
    )

  def updateModel(
      context: SceneContext[Size],
      model: Model
  ): GlobalEvent => Outcome[Model] =
    case WindowEvent.MouseOver(id) =>
      println("Mouse over window: " + id)
      val ids = id :: model.mouseOverWindows.filterNot(_ == id)

      Outcome(model.copy(mouseOverWindows = ids))

    case WindowEvent.MouseOut(id) =>
      println("Mouse out window: " + id)
      val ids = model.mouseOverWindows.filterNot(_ == id)

      Outcome(model.copy(mouseOverWindows = ids))

    case WindowEvent.Closed(id) =>
      println("Closed window: " + id)
      val ids = model.mouseOverWindows.filterNot(_ == id)

      Outcome(model.copy(mouseOverWindows = ids))

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
      SceneUpdateFragment.empty
    )
