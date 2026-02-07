package demo.scenes

import demo.Assets
import demo.Constants
import demo.models.GameModel
import demo.windows.ColourWindow
import indigo.*
import indigoextras.ui.*
import roguelikestarterkit.*

object ColourWindowScene extends Scene[Size, GameModel]:

  type SceneModel = GameModel

  val name: SceneName =
    SceneName("colour window scene")

  val modelLens: Lens[GameModel, GameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[GameModel]] =
    Set(
      WindowManager[GameModel, Unit](
        SubSystemId("window manager"),
        Constants.magnification,
        Size(GameModel.defaultCharSheet.charSize),
        _ => ()
      )
        .withLayerKey(LayerKey("UI Layer"))
        .register(
          ColourWindow.window(
            GameModel.defaultCharSheet
          )
        )
        .open(ColourWindow.windowId)
        .focus(ColourWindow.windowId)
    )

  def updateModel(
      context: SceneContext[Size],
      model: GameModel
  ): GlobalEvent => Outcome[GameModel] =
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

  private val text: Text[TerminalMaterial] =
    Text(
      "",
      RoguelikeTiles.Size10x10.Fonts.fontKey,
      TerminalMaterial(Assets.assets.AnikkiSquare10x10)
    )

  def present(
      context: SceneContext[Size],
      model: GameModel
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
