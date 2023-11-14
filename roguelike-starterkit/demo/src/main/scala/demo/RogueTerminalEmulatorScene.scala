package demo

import indigo.*
import indigo.scenes.*
import io.indigoengine.roguelike.starterkit.*

object RogueTerminalEmulatorScene extends Scene[Unit, Unit, Unit]:

  type SceneModel     = Unit
  type SceneViewModel = Unit

  val name: SceneName =
    SceneName("RogueTerminalEmulatorScene")

  val modelLens: Lens[Unit, Unit] =
    Lens.keepLatest

  val viewModelLens: Lens[Unit, Unit] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: SceneContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(TerminalTextScene.name))

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[Unit],
      model: Unit,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  // This shouldn't live here really, just keeping it simple for demo purposes.
  val terminal: RogueTerminalEmulator =
    RogueTerminalEmulator(Size(3, 3))
      .put(
        Point(0, 0) -> MapTile(Tile.`░`, RGBA.Yellow, RGBA.Orange),
        Point(1, 0) -> MapTile(Tile.`░`, RGBA.Yellow, RGBA.Orange),
        Point(2, 0) -> MapTile(Tile.`░`, RGBA.Yellow, RGBA.Orange),
        Point(0, 1) -> MapTile(Tile.`░`, RGBA.Yellow, RGBA.Orange),
        Point(1, 1) -> MapTile(Tile.`@`, RGBA.Cyan),
        Point(2, 1) -> MapTile(Tile.`░`, RGBA.Yellow, RGBA.Orange),
        Point(0, 2) -> MapTile(Tile.`░`, RGBA.Yellow, RGBA.Orange),
        Point(1, 2) -> MapTile(Tile.`░`, RGBA.Yellow, RGBA.Orange),
        Point(2, 2) -> MapTile(Tile.`░`, RGBA.Yellow, RGBA.Orange)
      )

  def present(
      context: SceneContext[Unit],
      model: Unit,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    val tiles =
      terminal.toCloneTiles(
        CloneId("demo"),
        Point.zero,
        RoguelikeTiles.Size10x10.charCrops
      ) { (fg, bg) =>
        Graphic(10, 10, TerminalMaterial(Assets.tileMap, fg, bg))
      }

    Outcome(
      SceneUpdateFragment(tiles.clones)
        .addCloneBlanks(tiles.blanks)
    )
