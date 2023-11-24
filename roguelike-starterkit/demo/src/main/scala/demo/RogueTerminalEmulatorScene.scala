package demo

import indigo.*
import indigo.scenes.*
import roguelikestarterkit.*

object RogueTerminalEmulatorScene extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("RogueTerminalEmulatorScene")

  val modelLens: Lens[Model, Model] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, ViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: SceneContext[Size], model: Model): GlobalEvent => Outcome[Model] =
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(TerminalTextScene.name))

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  // This shouldn't live here really, just keeping it simple for demo purposes.
  val terminal: RogueTerminalEmulator =
    RogueTerminalEmulator(Size(11, 11))
      .fill(MapTile(Tile.DARK_SHADE, RGBA.Yellow, RGBA.Black))
      .fillRectangle(Rectangle(1, 1, 9, 9), MapTile(Tile.MEDIUM_SHADE, RGBA.Yellow, RGBA.Black))
      .fillCircle(Circle(5, 5, 4), MapTile(Tile.LIGHT_SHADE, RGBA.Yellow, RGBA.Black))
      .mapLine(Point(0, 10), Point(10, 0)) { case (pt, tile) =>
        tile.withForegroundColor(RGBA.Red)
      }
      .put(Point(5, 5), MapTile(Tile.`@`, RGBA.Cyan))

  def present(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    val tiles =
      terminal.toCloneTiles(
        CloneId("demo"),
        Point.zero,
        RoguelikeTiles.Size10x10.charCrops
      ) { (fg, bg) =>
        Graphic(10, 10, TerminalMaterial(Assets.assets.AnikkiSquare10x10, fg, bg))
      }

    Outcome(tiles.toSceneUpdateFragment)
