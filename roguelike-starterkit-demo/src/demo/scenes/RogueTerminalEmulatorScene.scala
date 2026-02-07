package demo.scenes

import demo.Assets
import demo.models.GameModel
import indigo.*
import roguelikestarterkit.*

object RogueTerminalEmulatorScene extends Scene[Size, GameModel]:

  type SceneModel = GameModel

  val name: SceneName =
    SceneName("RogueTerminalEmulatorScene")

  val modelLens: Lens[GameModel, GameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[GameModel]] =
    Set()

  def updateModel(context: SceneContext[Size], model: GameModel): GlobalEvent => Outcome[GameModel] =
    case _ =>
      Outcome(model)

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
      model: GameModel
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
