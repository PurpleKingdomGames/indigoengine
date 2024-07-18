package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.TerminalEmulator
import roguelikestarterkit.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.tiles.Tile
import roguelikestarterkit.ui.components.Button
import roguelikestarterkit.ui.components.TerminalTileColors
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords

object TerminalButton:

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentButton[ReferenceData](
      label: ReferenceData => Batch[Tile],
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet,
      hasBorder: Boolean
  ): (Coords, Bounds, ReferenceData) => Outcome[Layer] =
    if hasBorder then presentButtonWithBorder(label, fgColor, bgColor, charSheet)
    else presentButtonNoBorder(label, fgColor, bgColor, charSheet)

  private def presentButtonNoBorder[ReferenceData](
      label: ReferenceData => Batch[Tile],
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet
  ): (Coords, Bounds, ReferenceData) => Outcome[Layer] =
    (offset, bounds, ref) =>
      val size = bounds.dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .fill(Tile.` `, fgColor, bgColor)
          .putTileLine(Point.zero, label(ref), fgColor, bgColor)
          .toCloneTiles(
            CloneId(s"button_${charSheet.assetName.toString}"),
            bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(offset.toScreenSpace(charSheet.size)),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      Outcome(Layer.Content(terminal))

  private def presentButtonWithBorder[ReferenceData](
      label: ReferenceData => Batch[Tile],
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet
  ): (Coords, Bounds, ReferenceData) => Outcome[Layer] =
    (offset, bounds, ref) =>
      val txt  = label(ref).take(bounds.width - 2)
      val hBar = Batch.fill(bounds.width - 2)("─").mkString
      val size = bounds.dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .fill(Tile.` `, fgColor, bgColor)
          .put(Point(0, 0), Tile.`┌`, fgColor, bgColor)
          .put(Point(size.width - 1, 0), Tile.`┐`, fgColor, bgColor)
          .put(Point(0, size.height - 1), Tile.`└`, fgColor, bgColor)
          .put(Point(size.width - 1, size.height - 1), Tile.`┘`, fgColor, bgColor)
          .put(Point(0, 1), Tile.`│`, fgColor, bgColor)
          .put(Point(size.width - 1, 1), Tile.`│`, fgColor, bgColor)
          .putLine(Point(1, 0), hBar, fgColor, bgColor)
          .putTileLine(Point(1, 1), txt, fgColor, bgColor)
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

      Outcome(Layer.Content(terminalClones))

  private def findBounds(labelLength: Int, hasBorder: Boolean): Bounds =
    if hasBorder then Bounds(0, 0, labelLength + 2, 3) else Bounds(0, 0, labelLength, 1)

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme`, with
    * dynamically calculated bounds
    */
  def apply[ReferenceData](
      label: ReferenceData => String,
      theme: Theme,
      calculateBounds: ReferenceData => Bounds
  ): Button[ReferenceData] =
    val tileLabel = (ref: ReferenceData) => TerminalEmulator.stringToTileBatch(label(ref))
    fromTiles(tileLabel, theme, calculateBounds)

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme`, with
    * custom bounds
    */
  def apply[ReferenceData](
      label: String,
      theme: Theme,
      bounds: Bounds
  ): Button[ReferenceData] =
    TerminalButton(_ => label, theme, _ => bounds)

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme` where the
    * bounds are based on the label size, which is assumed to be a single line of simple text.
    */
  def apply[ReferenceData](
      label: String,
      theme: Theme
  ): Button[ReferenceData] =
    TerminalButton(
      label,
      theme,
      findBounds(label.length, theme.hasBorder)
    )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme` where the
    * bounds are based on the label size, which is assumed to be a single line of simple text.
    */
  def apply[ReferenceData](
      label: ReferenceData => String,
      theme: Theme
  ): Button[ReferenceData] =
    TerminalButton(
      label,
      theme,
      (ref: ReferenceData) => findBounds(label(ref).length, theme.hasBorder)
    )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme`, with
    * dynamically calculated bounds, where the label is a row of tiles.
    */
  def fromTiles[ReferenceData](
      tileLabel: ReferenceData => Batch[Tile],
      theme: Theme,
      calculateBounds: ReferenceData => Bounds
  ): Button[ReferenceData] =
    Button(calculateBounds) {
      presentButton(
        tileLabel,
        theme.up.foreground,
        theme.up.background,
        theme.charSheet,
        theme.hasBorder
      )
    }
      .presentOver(
        presentButton(
          tileLabel,
          theme.over.foreground,
          theme.over.background,
          theme.charSheet,
          theme.hasBorder
        )
      )
      .presentDown(
        presentButton(
          tileLabel,
          theme.down.foreground,
          theme.down.background,
          theme.charSheet,
          theme.hasBorder
        )
      )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme`, with
    * custom bounds, where the label is a row of tiles.
    */
  def fromTiles[ReferenceData](
      label: Batch[Tile],
      theme: Theme,
      bounds: Bounds
  ): Button[ReferenceData] =
    TerminalButton.fromTiles(_ => label, theme, _ => bounds)

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme` where the
    * bounds are based on the label size, which is assumed to be a single row of tiles.
    */
  def fromTiles[ReferenceData](
      label: Batch[Tile],
      theme: Theme
  ): Button[ReferenceData] =
    TerminalButton.fromTiles(
      label,
      theme,
      findBounds(label.length, theme.hasBorder)
    )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme` where the
    * bounds are based on the label size, which is assumed to be a single row of tiles.
    */
  def fromTiles[ReferenceData](
      label: ReferenceData => Batch[Tile],
      theme: Theme
  ): Button[ReferenceData] =
    TerminalButton.fromTiles(
      label,
      theme,
      (ref: ReferenceData) => findBounds(label(ref).length, theme.hasBorder)
    )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme` where the
    * bounds are based on the label size, which is assumed to be a single Tile.
    */
  def fromTile[ReferenceData](
      label: Tile,
      theme: Theme
  ): Button[ReferenceData] =
    TerminalButton.fromTiles(
      Batch(label),
      theme,
      findBounds(1, theme.hasBorder)
    )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme` where the
    * bounds are based on the label size, which is assumed to be a Tile.
    */
  def fromTile[ReferenceData](
      label: ReferenceData => Tile,
      theme: Theme
  ): Button[ReferenceData] =
    TerminalButton.fromTiles(
      (ref: ReferenceData) => Batch(label(ref)),
      theme,
      (ref: ReferenceData) => findBounds(1, theme.hasBorder)
    )

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
