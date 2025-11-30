package demo.scenes

import demo.Constants
import demo.models.GameModel
import demo.windows.ComponentsWindow
import demo.windows.ComponentsWindow2
import demo.windows.MenuWindow
import indigo.next.*
import indigoextras.ui.*

object MultipleWindowsScene extends Scene[Size, GameModel]:

  type SceneModel = GameModel

  val name: SceneName =
    SceneName("MultipleWindowsScene")

  val modelLens: Lens[GameModel, GameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[GameModel]] =
    Set(
      WindowManager[GameModel, Int](
        SubSystemId("window manager 2"),
        Constants.magnification,
        Size(GameModel.defaultCharSheet.charSize),
        _.pointerOverWindows.length
      )
        .register(
          ComponentsWindow.window(
            GameModel.defaultCharSheet
          )
        )
        .register(
          ComponentsWindow2.window(
            GameModel.defaultCharSheet
          )
        )
        .register(
          MenuWindow.window(
            GameModel.defaultCharSheet
          )
        )
        .open(
          MenuWindow.windowId,
          ComponentsWindow.windowId,
          ComponentsWindow2.windowId
        )
        .focus(ComponentsWindow2.windowId)
    )

  def updateModel(
      context: SceneContext[Size],
      model: GameModel
  ): GlobalEvent => Outcome[GameModel] =
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

  def present(
      context: SceneContext[Size],
      model: GameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
    )
