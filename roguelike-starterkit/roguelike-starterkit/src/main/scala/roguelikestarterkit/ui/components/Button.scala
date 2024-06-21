package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.tiles.Tile
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.component.StatelessComponent
import roguelikestarterkit.ui.components.common.TerminalTileColors
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
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

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentButton[ReferenceData](
      label: ReferenceData => String,
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet,
      hasBorder: Boolean
  ): (Coords, Bounds, ReferenceData) => Outcome[ComponentFragment] =
    if hasBorder then presentButtonWithBorder(label, fgColor, bgColor, charSheet)
    else presentButtonNoBorder(label, fgColor, bgColor, charSheet)

  private def presentButtonNoBorder[ReferenceData](
      label: ReferenceData => String,
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet
  ): (Coords, Bounds, ReferenceData) => Outcome[ComponentFragment] =
    (offset, bounds, ref) =>
      val size = bounds.dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .putLine(Point.zero, label(ref), fgColor, bgColor)
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

  private def presentButtonWithBorder[ReferenceData](
      label: ReferenceData => String,
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet
  ): (Coords, Bounds, ReferenceData) => Outcome[ComponentFragment] =
    (offset, bounds, ref) =>
      val txt  = label(ref)
      val hBar = Batch.fill(txt.length)("─").mkString
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
          .putLine(Point(1, 1), txt, fgColor, bgColor)
          .putLine(Point(1, 2), hBar, fgColor, bgColor)

      val terminalClones =
        terminal
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
          terminalClones.clones
        ).addCloneBlanks(terminalClones.blanks)
      )

  private def findBounds(label: String, hasBorder: Boolean): Bounds =
    if hasBorder then Bounds(0, 0, label.length + 2, 3) else Bounds(0, 0, label.length, 1)

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

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme`, with
    * dynamically calculated bounds
    */
  def apply[ReferenceData](
      label: ReferenceData => String,
      theme: Theme,
      calculateBounds: ReferenceData => Bounds
  ): Button[ReferenceData] =
    Button(
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
      _ => Batch.empty,
      calculateBounds
    )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme`, with
    * custom bounds
    */
  def apply[ReferenceData](
      label: String,
      theme: Theme,
      bounds: Bounds
  ): Button[ReferenceData] =
    Button(_ => label, theme, _ => bounds)

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme` where the
    * bounds are based on the label size, which is assumed to be a single line of simple text.
    */
  def apply[ReferenceData](
      label: String,
      theme: Theme
  ): Button[ReferenceData] =
    Button(
      label,
      theme,
      findBounds(label, theme.hasBorder)
    )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme` where the
    * bounds are based on the label size, which is assumed to be a single line of simple text.
    */
  def apply[ReferenceData](
      label: ReferenceData => String,
      theme: Theme
  ): Button[ReferenceData] =
    Button(
      label,
      theme,
      (ref: ReferenceData) => findBounds(label(ref), theme.hasBorder)
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
      Theme(
        charSheet,
        RGBA.Silver -> RGBA.Black,
        RGBA.White  -> RGBA.Black,
        RGBA.Black  -> RGBA.White
      )

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
