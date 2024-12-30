package demo.scenes

import demo.RogueLikeGame
import demo.models.Model
import demo.models.ViewModel
import demo.windows.ComponentsWindow
import demo.windows.ComponentsWindow2
import demo.windows.MenuWindow
import indigo.*
import indigo.scenes.*
import roguelikestarterkit.*

object MultipleWindowsScene extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("MultipleWindowsScene")

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
        Size(Model.defaultCharSheet.charSize),
        _.pointerOverWindows.length
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
        .focus(ComponentsWindow.windowId)
    )

  def updateModel(
      context: SceneContext[Size],
      model: Model
  ): GlobalEvent => Outcome[Model] =
    case WindowEvent.PointerOver(id) =>
      println("Pointer over window: " + id)
      val ids = id :: model.pointerOverWindows.filterNot(_ == id)

      Outcome(model.copy(pointerOverWindows = ids))

    case WindowEvent.PointerOut(id) =>
      println("Pointer out window: " + id)
      val ids = model.pointerOverWindows.filterNot(_ == id)

      Outcome(model.copy(pointerOverWindows = ids))

    case WindowEvent.Closed(id) =>
      println("Closed window: " + id)
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
      SceneUpdateFragment.empty
    )
