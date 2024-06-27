package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.component.StatelessComponent
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.UIContext

/** Buttons `Component`s allow you to create buttons for your UI.
  */
final case class Button[ReferenceData](
    up: (Coords, Bounds, ReferenceData) => Outcome[ComponentFragment],
    over: Option[(Coords, Bounds, ReferenceData) => Outcome[ComponentFragment]],
    down: Option[(Coords, Bounds, ReferenceData) => Outcome[ComponentFragment]],
    click: ReferenceData => Batch[GlobalEvent],
    calculateBounds: ReferenceData => Bounds
):
  def presentUp(
      up: (Coords, Bounds, ReferenceData) => Outcome[ComponentFragment]
  ): Button[ReferenceData] =
    this.copy(up = up)

  def presentOver(
      over: (Coords, Bounds, ReferenceData) => Outcome[ComponentFragment]
  ): Button[ReferenceData] =
    this.copy(over = Option(over))

  def presentDown(
      down: (Coords, Bounds, ReferenceData) => Outcome[ComponentFragment]
  ): Button[ReferenceData] =
    this.copy(down = Option(down))

  def onClick(events: ReferenceData => Batch[GlobalEvent]): Button[ReferenceData] =
    this.copy(click = events)
  def onClick(events: Batch[GlobalEvent]): Button[ReferenceData] =
    onClick(_ => events)
  def onClick(events: GlobalEvent*): Button[ReferenceData] =
    onClick(Batch.fromSeq(events))

object Button:

  /** Minimal button constructor with custom rendering function
    */
  def apply[ReferenceData](bounds: Bounds)(
      present: (Coords, Bounds, ReferenceData) => Outcome[ComponentFragment]
  ): Button[ReferenceData] =
    Button(
      present,
      None,
      None,
      _ => Batch.empty,
      _ => bounds
    )

  /** Minimal button constructor with custom rendering function and dynamic sizing
    */
  def apply[ReferenceData](calculateBounds: ReferenceData => Bounds)(
      present: (Coords, Bounds, ReferenceData) => Outcome[ComponentFragment]
  ): Button[ReferenceData] =
    Button(
      present,
      None,
      None,
      _ => Batch.empty,
      calculateBounds
    )

  given [ReferenceData]: StatelessComponent[Button[ReferenceData], ReferenceData] with
    def bounds(reference: ReferenceData, model: Button[ReferenceData]): Bounds =
      model.calculateBounds(reference)

    def present(
        context: UIContext[ReferenceData],
        model: Button[ReferenceData]
    ): Outcome[ComponentFragment] =
      val b           = model.calculateBounds(context.reference)
      val mouseWithin = b.moveBy(context.bounds.coords).contains(context.mouseCoords)

      val state =
        if context.isActive && mouseWithin then
          if context.mouse.isLeftDown then ButtonState.Down
          else ButtonState.Over
        else ButtonState.Up

      val events =
        if context.isActive && mouseWithin && context.mouse.mouseClicked then
          model.click(context.reference)
        else Batch.empty

      state match
        case ButtonState.Up =>
          model
            .up(context.bounds.coords, b, context.reference)
            .addGlobalEvents(events)

        case ButtonState.Over =>
          model.over
            .getOrElse(model.up)(context.bounds.coords, b, context.reference)
            .addGlobalEvents(events)

        case ButtonState.Down =>
          model.down
            .getOrElse(model.up)(context.bounds.coords, b, context.reference)
            .addGlobalEvents(events)

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
