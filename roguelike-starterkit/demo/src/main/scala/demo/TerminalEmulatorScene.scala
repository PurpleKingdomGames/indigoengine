package demo

import indigo.*
import indigo.scenes.*
import roguelikestarterkit.*

object TerminalEmulatorScene extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("TerminalEmulatorScene")

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
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(UIScene.name))

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  // This shouldn't live here really, just keeping it simple for demo purposes.
  val terminal: TerminalEmulator =
    TerminalEmulator(Size(3, 3))
      .put(
        Point(0, 0) -> MapTile(Tile.`░`, RGBA.Cyan, RGBA.Blue),
        Point(1, 0) -> MapTile(Tile.`░`, RGBA.Cyan, RGBA.Blue),
        Point(2, 0) -> MapTile(Tile.`░`, RGBA.Cyan, RGBA.Blue),
        Point(0, 1) -> MapTile(Tile.`░`, RGBA.Cyan, RGBA.Blue),
        Point(1, 1) -> MapTile(Tile.`@`, RGBA.Magenta),
        Point(2, 1) -> MapTile(Tile.`░`, RGBA.Cyan, RGBA.Blue),
        Point(0, 2) -> MapTile(Tile.`░`, RGBA.Cyan, RGBA.Blue),
        Point(1, 2) -> MapTile(Tile.`░`, RGBA.Cyan, RGBA.Blue),
        Point(2, 2) -> MapTile(Tile.`░`, RGBA.Cyan, RGBA.Blue)
      )

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

    Outcome(
      SceneUpdateFragment(tiles.clones)
        .addCloneBlanks(tiles.blanks)
    )
