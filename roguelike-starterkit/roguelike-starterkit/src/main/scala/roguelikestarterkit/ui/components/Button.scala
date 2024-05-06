package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
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

  /** Minimal button constructor with custom rendering function
    */
  def apply(bounds: Bounds)(present: (Coords, Bounds) => Outcome[ComponentFragment]): Button =
    Button(bounds, ButtonState.Up, present, None, None, Batch.empty)

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentButton(
      label: String,
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet
  ): (Coords, Bounds) => Outcome[ComponentFragment] =
    (offset, bounds) =>
      val size = bounds.dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .putLine(Point.zero, label, fgColor, bgColor)
          .toCloneTiles(
            CloneId("button"),
            bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(offset.toScreenSpace(charSheet.size)),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      Outcome(ComponentFragment(terminal))

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme`, with
    * custom bounds
    */
  def apply(
      label: String,
      theme: Theme,
      bounds: Bounds
  ): Button =
    Button(
      bounds,
      ButtonState.Up,
      presentButton(label, theme.up.foreground, theme.up.background, theme.charSheet),
      Option(
        presentButton(label, theme.over.foreground, theme.over.background, theme.charSheet)
      ),
      Option(
        presentButton(label, theme.down.foreground, theme.down.background, theme.charSheet)
      ),
      Batch.empty
    )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme` where the
    * bounds are based on the label size, which is assumed to be a single line of simple text.
    */
  def apply(
      label: String,
      theme: Theme
  ): Button =
    Button(
      label,
      theme,
      Bounds(0, 0, label.length, 1)
    )

  given Component[Button] with
    def bounds(model: Button): Bounds =
      model.bounds

    def updateModel[ReferenceData](
        context: UiContext[ReferenceData],
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

    def present[ReferenceData](
        context: UiContext[ReferenceData],
        model: Button
    ): Outcome[ComponentFragment] =
      model.state match
        case ButtonState.Up   => model.up(context.bounds.coords, model.bounds)
        case ButtonState.Over => model.over.getOrElse(model.up)(context.bounds.coords, model.bounds)
        case ButtonState.Down => model.down.getOrElse(model.up)(context.bounds.coords, model.bounds)

    def reflow(model: Button): Button =
      model

    def cascade(model: Button, parentBounds: Bounds): Button =
      model

  final case class Theme(
      charSheet: CharSheet,
      up: Color,
      over: Color,
      down: Color
  ):
    def withUp(foreground: RGBA, background: RGBA): Theme =
      this.copy(up = Color(foreground, background))

    def withOver(foreground: RGBA, background: RGBA): Theme =
      this.copy(over = Color(foreground, background))

    def withDown(foreground: RGBA, background: RGBA): Theme =
      this.copy(down = Color(foreground, background))

  object Theme:
    def apply(charSheet: CharSheet, foreground: RGBA, background: RGBA): Theme =
      Theme(
        charSheet,
        Color(foreground, background),
        Color(foreground, background),
        Color(foreground, background)
      )

    def apply(
        charSheet: CharSheet,
        foregroundUp: RGBA,
        backgroundUp: RGBA,
        foregroundOver: RGBA,
        backgroundOver: RGBA,
        foregroundDown: RGBA,
        backgroundDown: RGBA
    ): Theme =
      Theme(
        charSheet,
        Color(foregroundUp, backgroundUp),
        Color(foregroundOver, backgroundOver),
        Color(foregroundDown, backgroundDown)
      )

    def apply(
        charSheet: CharSheet,
        up: (RGBA, RGBA),
        over: (RGBA, RGBA),
        down: (RGBA, RGBA)
    ): Theme =
      Theme(
        charSheet,
        Color(up._1, up._2),
        Color(over._1, over._2),
        Color(down._1, up._2)
      )

  final case class Color(foreground: RGBA, background: RGBA):
    def withForeground(value: RGBA): Color =
      this.copy(foreground = value)
    def withBackground(value: RGBA): Color =
      this.copy(background = value)

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
