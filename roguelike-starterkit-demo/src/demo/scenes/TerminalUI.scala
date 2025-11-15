package demo.scenes

import demo.Assets
import demo.models.Log
import demo.models.Model
import demo.models.ViewModel
import indigo.*
import indigo.scenes.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.*
import roguelikestarterkit.ui.*

object TerminalUI extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("TerminalUI scene")

  val modelLens: Lens[Model, Model] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, ViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[Model]] =
    Set()

  def updateModel(
      context: SceneContext[Size],
      model: Model
  ): GlobalEvent => Outcome[Model] =
    case Log(message) =>
      println(message)
      Outcome(model)

    case e =>
      val ctx = UIContext(context.toContext, context.frame.globalMagnification)
        .withSnapGrid(TerminalUIComponents.charSheet.size)
        .moveParentBy(Coords(5, 5))

      model.button.update(ctx)(e).map { b =>
        model.copy(button = b)
      }

  def updateViewModel(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
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
      .onClick(Log("Button clicked"))
      .onPress(Log("Button pressed"))
      .onRelease(Log("Button released"))
