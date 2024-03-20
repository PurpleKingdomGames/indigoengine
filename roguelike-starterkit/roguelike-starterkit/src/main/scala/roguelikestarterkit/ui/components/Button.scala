package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.*
import roguelikestarterkit.tiles.RoguelikeTiles10x10
import roguelikestarterkit.tiles.RoguelikeTiles5x6
import roguelikestarterkit.ui.component.Component
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.UiContext

final case class Button(
    bounds: Bounds,
    state: ButtonState,
    up: (Coords, Bounds) => Outcome[ComponentFragment],
    over: Option[(Coords, Bounds) => Outcome[ComponentFragment]],
    down: Option[(Coords, Bounds) => Outcome[ComponentFragment]],
    click: Batch[GlobalEvent]
):
  export bounds.*

  def presentUp(up: (Coords, Bounds) => Outcome[ComponentFragment]): Button =
    this.copy(up = up)

  def presentOver(over: (Coords, Bounds) => Outcome[ComponentFragment]): Button =
    this.copy(over = Option(over))

  def presentDown(down: (Coords, Bounds) => Outcome[ComponentFragment]): Button =
    this.copy(down = Option(down))

  def onClick(events: Batch[GlobalEvent]): Button =
    this.copy(click = events)
  def onClick(events: GlobalEvent*): Button =
    onClick(Batch.fromSeq(events))

object Button:

  def apply(bounds: Bounds)(present: (Coords, Bounds) => Outcome[ComponentFragment]): Button =
    val p = (_: Coords, _: Bounds) => Outcome(ComponentFragment.empty)
    Button(bounds, ButtonState.Up, present, None, None, Batch.empty)

  given Component[Button] with
    def bounds(model: Button): Bounds =
      model.bounds

    def updateModel(
        context: UiContext,
        model: Button
    ): GlobalEvent => Outcome[Button] =
      case FrameTick =>
        Outcome(
          model.copy(state =
            if model.bounds.moveBy(context.bounds.coords).contains(context.mouseCoords) then
              if context.mouse.isLeftDown then ButtonState.Down
              else ButtonState.Over
            else ButtonState.Up
          )
        )

      case _: MouseEvent.Click
          if model.bounds.moveBy(context.bounds.coords).contains(context.mouseCoords) =>
        Outcome(model).addGlobalEvents(model.click)

      case _ =>
        Outcome(model)

    def present(
        context: UiContext,
        model: Button
    ): Outcome[ComponentFragment] =
      model.state match
        case ButtonState.Up   => model.up(context.bounds.coords, model.bounds)
        case ButtonState.Over => model.over.getOrElse(model.up)(context.bounds.coords, model.bounds)
        case ButtonState.Down => model.down.getOrElse(model.up)(context.bounds.coords, model.bounds)

    def reflow(model: Button): Button =
      model

enum ButtonState:
  case Up, Over, Down

  def isUp: Boolean =
    this match
      case Up => true
      case _  => false

  def is: Boolean =
    this match
      case Over => true
      case _    => false

  def isDown: Boolean =
    this match
      case Down => true
      case _    => false
