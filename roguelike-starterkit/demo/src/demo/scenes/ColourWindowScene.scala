package demo.scenes

import demo.Assets
import demo.RogueLikeGame
import demo.models.Model
import demo.models.ViewModel
import demo.windows.ColourWindow
import indigo.*
import indigo.scenes.*
import indigoextras.ui.*
import roguelikestarterkit.*

object ColourWindowScene extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("colour window scene")

  val modelLens: Lens[Model, Model] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, ViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[Model]] =
    Set(
      WindowManager[Model, Unit](
        SubSystemId("window manager"),
        RogueLikeGame.magnification,
        Size(Model.defaultCharSheet.charSize),
        _ => ()
      )
        .withLayerKey(LayerKey("UI Layer"))
        .register(
          ColourWindow.window(
            Model.defaultCharSheet
          )
        )
        .open(ColourWindow.windowId)
        .focus(ColourWindow.windowId)
    )

  def updateModel(
      context: SceneContext[Size],
      model: Model
  ): GlobalEvent => Outcome[Model] =
    case KeyboardEvent.KeyUp(Key.KEY_O) =>
      Outcome(model).addGlobalEvents(WindowEvent.OpenAt(ColourWindow.windowId, Coords(1, 1)))

    case KeyboardEvent.KeyUp(Key.KEY_T) =>
      Outcome(model).addGlobalEvents(WindowEvent.Toggle(ColourWindow.windowId))

    case WindowEvent.PointerOver(id) =>
      println("Pointer over window: " + id)
      val ids = id :: model.pointerOverWindows.filterNot(_ == id)

      Outcome(model.copy(pointerOverWindows = ids))

    case WindowEvent.PointerOut(id) =>
      println("Pointer out window: " + id)
      val ids = model.pointerOverWindows.filterNot(_ == id)

      Outcome(model.copy(pointerOverWindows = ids))

    case WindowEvent.Closed(id) =>
      println("Window closed: " + id)
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

  private val text: Text[TerminalMaterial] =
    Text(
      "",
      RoguelikeTiles.Size10x10.Fonts.fontKey,
      TerminalMaterial(Assets.assets.AnikkiSquare10x10)
    )

  def present(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        LayerKey("info") ->
          Layer(
            text
              .withText(
                "Pointer over: " +
                  model.pointerOverWindows.mkString("[", ",", "]")
              )
              .moveTo(0, 260)
          ),
        LayerKey("UI Layer") -> Layer.Stack.empty
      )
    )
