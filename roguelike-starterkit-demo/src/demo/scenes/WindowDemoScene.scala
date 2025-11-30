package demo.scenes

import demo.Constants
import demo.models.GameModel
import demo.windows.DemoWindow
import indigo.next.*
import indigoextras.ui.*

object WindowDemoScene extends Scene[Size, GameModel]:

  type SceneModel = GameModel

  val name: SceneName =
    SceneName("window demo scene")

  val modelLens: Lens[GameModel, GameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[GameModel]] =
    Set(
      WindowManager[GameModel](
        SubSystemId("demo window manager"),
        Constants.magnification
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
      model: GameModel
  ): GlobalEvent => Outcome[GameModel] =
    case WindowEvent.Closed(id) =>
      val ids = model.pointerOverWindows.filterNot(_ == id)

      Outcome(model.copy(pointerOverWindows = ids))

    case _ =>
      Outcome(model)

  def present(
      context: SceneContext[Size],
      model: GameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        LayerKey("UI Layer") -> Layer.Stack.empty
      )
    )
