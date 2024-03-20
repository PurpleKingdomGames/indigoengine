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

  val subSystems: Set[SubSystem] =
    Set()

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

    case e =>
      val updated =
        model.windowManager.update(
          UiContext(
            unitFrameContext(context.frameContext),
            Model.defaultCharSheet
          ),
          e
        )

      updated.map(w => model.copy(windowManager = w))

  def updateViewModel(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    case e =>
      val updated = viewModel.windowManager.update(
        UiContext(
          unitFrameContext(context.frameContext),
          Model.defaultCharSheet
        ),
        model.windowManager,
        e
      )

      updated.map(w => viewModel.copy(windowManager = w))

  def present(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    WindowManager
      .present(
        UiContext(
          unitFrameContext(context.frameContext),
          Model.defaultCharSheet
        ),
        RogueLikeGame.magnification,
        model.windowManager,
        viewModel.windowManager
      )
      .map { windowsSUF =>
        SceneUpdateFragment(
          TextBox(
            "Mouse over: " +
              viewModel.windowManager.mouseIsOverAnyWindow + ", " +
              viewModel.windowManager.mouseIsOver.mkString("[", ",", "]")
          )
            .withTextStyle(TextStyle.default.withColor(RGBA.White).withSize(Pixels(12)))
            .moveTo(0, 260)
        ) |+| windowsSUF
      }

  private def unitFrameContext(context: FrameContext[Size]): FrameContext[Unit] =
    new FrameContext[Unit](context.gameTime, context.dice, context.inputState, context.boundaryLocator, ())
