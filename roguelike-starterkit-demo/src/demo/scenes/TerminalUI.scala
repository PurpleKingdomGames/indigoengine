package demo.scenes

import demo.Assets
import demo.GameEvent
import demo.models.GameModel
import indigo.next.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.*
import roguelikestarterkit.ui.*

object TerminalUI extends Scene[Size, GameModel]:

  type SceneModel = GameModel

  val name: SceneName =
    SceneName("TerminalUI scene")

  val modelLens: Lens[GameModel, GameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[GameModel]] =
    Set()

  def updateModel(
      context: SceneContext[Size],
      model: GameModel
  ): GlobalEvent => Outcome[GameModel] =
    case GameEvent.Log(message) =>
      println(message)
      Outcome(model)

    case e =>
      val ctx = UIContext(context.toContext, context.frame.globalMagnification)
        .withSnapGrid(TerminalUIComponents.charSheet.size)
        .moveParentBy(Coords(5, 5))

      model.button.update(ctx)(e).map { b =>
        model.copy(button = b)
      }

  def present(
      context: SceneContext[Size],
      model: GameModel
  ): Outcome[SceneUpdateFragment] =
    val ctx = UIContext(context.toContext, context.frame.globalMagnification)
      .withSnapGrid(TerminalUIComponents.charSheet.size)
      .moveParentBy(Coords(5, 5))

    model.button
      .present(ctx)
      .map(l => SceneUpdateFragment(l))

object TerminalUIComponents:

  val charSheet: CharSheet =
    CharSheet(
      Assets.assets.AnikkiSquare10x10,
      Size(10),
      RoguelikeTiles.Size10x10.charCrops,
      RoguelikeTiles.Size10x10.Fonts.fontKey
    )

  val customButton: Button[Unit] =
    TerminalButton(
      "Click me!",
      TerminalButton.Theme(
        charSheet,
        RGBA.Silver -> RGBA.Black,
        RGBA.White  -> RGBA.Black,
        RGBA.Black  -> RGBA.White,
        hasBorder = true
      )
    )
      .onClick(GameEvent.Log("Button clicked"))
      .onPress(GameEvent.Log("Button pressed"))
      .onRelease(GameEvent.Log("Button released"))
