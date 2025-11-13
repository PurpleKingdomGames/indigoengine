package demo.scenes

import demo.Assets
import demo.models.Model
import demo.models.ViewModel
import indigo.*
import indigo.scenes.*
import roguelikestarterkit.*

object LightingScene extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("LightingScene")

  val modelLens: Lens[Model, Model] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, ViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[Model]] =
    Set()

  def updateModel(context: SceneContext[Size], model: Model): GlobalEvent => Outcome[Model] =
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
      .fill(MapTile(Tile.DARK_SHADE, RGBA.Yellow.mix(RGBA.Black, 0.5), RGBA.Black))
      .fillRectangle(Rectangle(1, 1, 9, 9), MapTile(Tile.MEDIUM_SHADE, RGBA.Yellow, RGBA.Black))
      .fillCircle(Circle(5, 5, 4), MapTile(Tile.LIGHT_SHADE, RGBA.Yellow, RGBA.Black))
      .mapLine(Point(0, 10), Point(10, 0)) { case (pt, tile) =>
        tile.withForegroundColor(RGBA.Red)
      }
      .put(Point(5, 5), MapTile(Tile.`@`, RGBA.Cyan))

  val lightingModel =
    LightingModel
      .Lit()
      .withNormal(Assets.assets.AnikkiSquare10x10Normal, 1.0)
      .withRoughness(Assets.assets.AnikkiSquare10x10Specular, 1.0)

  val material =
    TerminalMaterial(Assets.assets.AnikkiSquare10x10, RGBA.White, RGBA.Black)
      .withLighting(lightingModel)

  val graphic =
    Graphic(10, 10, material)

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
        graphic.withMaterial(material.withColors(fg, bg))
      }

    val pt = Signal.Orbit(Point(50), 20).affectTime(0.5).at(context.sceneRunning).toPoint

    Outcome(
      tiles.toSceneUpdateFragment
        .withLights(
          AmbientLight(RGBA.Red),
          PointLight(pt, RGBA.White)
        )
    )
