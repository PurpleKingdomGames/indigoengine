package demo.scenes

import demo.Assets
import demo.Constants
import demo.models.GameModel
import indigo.*
import roguelikestarterkit.*

object TerminalTextScene extends Scene[GameModel]:

  type SceneModel = GameModel

  val name: SceneName =
    SceneName("TerminalText scene")

  val modelLens: Lens[GameModel, GameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[GameModel]] =
    Set()

  def updateModel(context: SceneContext, model: GameModel): GlobalEvent => Outcome[GameModel] =
    case _ =>
      Outcome(model)

  val size = Size(30)

  def message: String =
    """
    |╔═════════════════════╗
    |║ Hit Space to Start! ║
    |╚═════════════════════╝
    |""".stripMargin

  def present(
      context: SceneContext,
      model: GameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Constants.LayerKeys.game -> Layer.Content(
          Text(
            message,
            RoguelikeTiles.Size10x10.Fonts.fontKey,
            TerminalText(Assets.assets.AnikkiSquare10x10, RGBA.Cyan, RGBA.Blue)
          ),
          Text(
            message,
            RoguelikeTiles.Size10x10.Fonts.fontKey,
            TerminalText(
              Assets.assets.AnikkiSquare10x10,
              RGBA.White,
              RGBA.Zero,
              RGBA.Magenta.withAlpha(0.75)
            )
          ).moveBy(0, 40)
        )
      ).withMagnification(Magnification.x2)
    )
