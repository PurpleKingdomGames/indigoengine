package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.tiles.RoguelikeTiles10x10
import roguelikestarterkit.tiles.RoguelikeTiles5x6
import roguelikestarterkit.tiles.Tile
import roguelikestarterkit.ui.component.Component
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.UiContext

/** Buttons `Component`s allow you to create buttons for your UI.
  */
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
      charSheet: CharSheet,
      hasBorder: Boolean
  ): (Coords, Bounds) => Outcome[ComponentFragment] =
    if hasBorder then presentButtonWithBorder(label, fgColor, bgColor, charSheet)
    else presentButtonNoBorder(label, fgColor, bgColor, charSheet)

  private def presentButtonNoBorder(
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
            CloneId(s"button_${charSheet.assetName.toString}"),
            bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(offset.toScreenSpace(charSheet.size)),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      Outcome(ComponentFragment(terminal))

  def presentButtonWithBorder(
      label: String,
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet
  ): (Coords, Bounds) => Outcome[ComponentFragment] =
    (offset, bounds) =>
      val hBar = Batch.fill(label.length)("─").mkString
      val size = bounds.dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .put(Point(0, 0), Tile.`┌`, fgColor, bgColor)
          .put(Point(size.width - 1, 0), Tile.`┐`, fgColor, bgColor)
          .put(Point(0, size.height - 1), Tile.`└`, fgColor, bgColor)
          .put(Point(size.width - 1, size.height - 1), Tile.`┘`, fgColor, bgColor)
          .put(Point(0, 1), Tile.`│`, fgColor, bgColor)
          .put(Point(size.width - 1, 1), Tile.`│`, fgColor, bgColor)
          .putLine(Point(1, 0), hBar, fgColor, bgColor)
          .putLine(Point(1, 1), label, fgColor, bgColor)
          .putLine(Point(1, 2), hBar, fgColor, bgColor)
          .toCloneTiles(
            CloneId(s"button_${charSheet.assetName.toString}"),
            bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(offset.toScreenSpace(charSheet.size)),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      Outcome(
        ComponentFragment(
          terminal.clones
        ).addCloneBlanks(terminal.blanks)
      )

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
      presentButton(
        label,
        theme.up.foreground,
        theme.up.background,
        theme.charSheet,
        theme.hasBorder
      ),
      Option(
        presentButton(
          label,
          theme.over.foreground,
          theme.over.background,
          theme.charSheet,
          theme.hasBorder
        )
      ),
      Option(
        presentButton(
          label,
          theme.down.foreground,
          theme.down.background,
          theme.charSheet,
          theme.hasBorder
        )
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
      if theme.hasBorder then Bounds(0, 0, label.length + 2, 3) else Bounds(0, 0, label.length, 1)
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
      up: TerminalTileColors,
      over: TerminalTileColors,
      down: TerminalTileColors,
      hasBorder: Boolean
  ):
    def withCharSheet(value: CharSheet): Theme =
      this.copy(charSheet = value)

    def withUp(foreground: RGBA, background: RGBA): Theme =
      this.copy(up = TerminalTileColors(foreground, background))

    def withOver(foreground: RGBA, background: RGBA): Theme =
      this.copy(over = TerminalTileColors(foreground, background))

    def withDown(foreground: RGBA, background: RGBA): Theme =
      this.copy(down = TerminalTileColors(foreground, background))

    def withBorder(value: Boolean): Theme =
      this.copy(hasBorder = value)
    def addBorder: Theme =
      this.copy(hasBorder = true)
    def noBorder: Theme =
      this.copy(hasBorder = false)

  object Theme:
    def apply(charSheet: CharSheet, foreground: RGBA, background: RGBA, hasBorder: Boolean): Theme =
      Theme(
        charSheet,
        TerminalTileColors(foreground, background),
        TerminalTileColors(foreground, background),
        TerminalTileColors(foreground, background),
        hasBorder
      )
    def apply(charSheet: CharSheet, foreground: RGBA, background: RGBA): Theme =
      Theme(
        charSheet,
        foreground,
        background,
        false
      )

    def apply(
        charSheet: CharSheet,
        foregroundUp: RGBA,
        backgroundUp: RGBA,
        foregroundOver: RGBA,
        backgroundOver: RGBA,
        foregroundDown: RGBA,
        backgroundDown: RGBA,
        hasBorder: Boolean
    ): Theme =
      Theme(
        charSheet,
        TerminalTileColors(foregroundUp, backgroundUp),
        TerminalTileColors(foregroundOver, backgroundOver),
        TerminalTileColors(foregroundDown, backgroundDown),
        hasBorder
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
        foregroundUp,
        backgroundUp,
        foregroundOver,
        backgroundOver,
        foregroundDown,
        backgroundDown,
        false
      )

    def apply(
        charSheet: CharSheet,
        up: (RGBA, RGBA),
        over: (RGBA, RGBA),
        down: (RGBA, RGBA),
        hasBorder: Boolean
    ): Theme =
      Theme(
        charSheet,
        TerminalTileColors(up._1, up._2),
        TerminalTileColors(over._1, over._2),
        TerminalTileColors(down._1, down._2),
        hasBorder
      )
    def apply(
        charSheet: CharSheet,
        up: (RGBA, RGBA),
        over: (RGBA, RGBA),
        down: (RGBA, RGBA)
    ): Theme =
      Theme(
        charSheet,
        up,
        over,
        down,
        false
      )

    def apply(charSheet: CharSheet): Theme =
      Theme(charSheet, RGBA.White, RGBA.Black)

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
