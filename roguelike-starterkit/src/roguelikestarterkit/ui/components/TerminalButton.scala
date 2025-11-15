package roguelikestarterkit.ui.components

import indigo.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.TerminalEmulator
import roguelikestarterkit.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.tiles.Tile
import roguelikestarterkit.ui.*

object TerminalButton:

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentButton[ReferenceData](
      label: UIContext[ReferenceData] => Batch[Tile],
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet,
      hasBorder: Boolean,
      borderTiles: TerminalBorderTiles
  ): (UIContext[ReferenceData], Button[ReferenceData]) => Outcome[Layer] =
    if hasBorder then presentButtonWithBorder(label, fgColor, bgColor, charSheet, borderTiles)
    else presentButtonNoBorder(label, fgColor, bgColor, charSheet)

  private def presentButtonNoBorder[ReferenceData](
      label: UIContext[ReferenceData] => Batch[Tile],
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet
  ): (UIContext[ReferenceData], Button[ReferenceData]) => Outcome[Layer] =
    (context, button) =>
      val size = button.bounds.dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .fill(Tile.` `, fgColor, bgColor)
          .putTileLine(Point.zero, label(context), fgColor, bgColor)
          .toCloneTiles(
            CloneId(s"button_${charSheet.assetName.toString}"),
            button.bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(context.parent.coords.toScreenSpace(charSheet.size)),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      Outcome(Layer.Content(terminal))

  private def presentButtonWithBorder[ReferenceData](
      label: UIContext[ReferenceData] => Batch[Tile],
      fgColor: RGBA,
      bgColor: RGBA,
      charSheet: CharSheet,
      borderTiles: TerminalBorderTiles
  ): (UIContext[ReferenceData], Button[ReferenceData]) => Outcome[Layer] =
    (context, button) =>
      val bounds = button.bounds(context)
      val txt    = label(context).take(bounds.width - 2)
      val hBar   = Batch.fill(bounds.width - 2)(borderTiles.horizontal)
      val size   = bounds.dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .fill(borderTiles.fill, fgColor, bgColor)
          .put(Point(0, 0), borderTiles.topLeft, fgColor, bgColor)
          .put(Point(size.width - 1, 0), borderTiles.topRight, fgColor, bgColor)
          .put(Point(0, size.height - 1), borderTiles.bottomLeft, fgColor, bgColor)
          .put(Point(size.width - 1, size.height - 1), borderTiles.bottomRight, fgColor, bgColor)
          .put(Point(0, 1), borderTiles.vertical, fgColor, bgColor)
          .put(Point(size.width - 1, 1), borderTiles.vertical, fgColor, bgColor)
          .putTileLine(Point(1, 0), hBar, fgColor, bgColor)
          .putTileLine(Point(1, 1), txt, fgColor, bgColor)
          .putTileLine(Point(1, 2), hBar, fgColor, bgColor)

      val terminalClones =
        terminal
          .toCloneTiles(
            CloneId(s"button_${charSheet.assetName.toString}"),
            bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(context.parent.coords.toScreenSpace(charSheet.size)),
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
      label: UIContext[ReferenceData] => String,
      theme: Theme,
      calculateBounds: UIContext[ReferenceData] => Bounds
  ): Button[ReferenceData] =
    val tileLabel = (ctx: UIContext[ReferenceData]) =>
      TerminalEmulator.stringToTileBatch(label(ctx))
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
      label: UIContext[ReferenceData] => String,
      theme: Theme
  ): Button[ReferenceData] =
    TerminalButton(
      label,
      theme,
      (ctx: UIContext[ReferenceData]) => findBounds(label(ctx).length, theme.hasBorder)
    )

  /** Creates a button rendered using the RogueTerminalEmulator based on a `Button.Theme`, with
    * dynamically calculated bounds, where the label is a row of tiles.
    */
  def fromTiles[ReferenceData](
      tileLabel: UIContext[ReferenceData] => Batch[Tile],
      theme: Theme,
      calculateBounds: UIContext[ReferenceData] => Bounds
  ): Button[ReferenceData] =
    Button(calculateBounds) {
      presentButton(
        tileLabel,
        theme.up.foreground,
        theme.up.background,
        theme.charSheet,
        theme.hasBorder,
        theme.borderTiles
      )
    }
      .presentOver(
        presentButton(
          tileLabel,
          theme.over.foreground,
          theme.over.background,
          theme.charSheet,
          theme.hasBorder,
          theme.borderTiles
        )
      )
      .presentDown(
        presentButton(
          tileLabel,
          theme.down.foreground,
          theme.down.background,
          theme.charSheet,
          theme.hasBorder,
          theme.borderTiles
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
      label: UIContext[ReferenceData] => Batch[Tile],
      theme: Theme
  ): Button[ReferenceData] =
    TerminalButton.fromTiles(
      label,
      theme,
      (ctx: UIContext[ReferenceData]) => findBounds(label(ctx).length, theme.hasBorder)
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
      label: UIContext[ReferenceData] => Tile,
      theme: Theme
  ): Button[ReferenceData] =
    TerminalButton.fromTiles(
      (ctx: UIContext[ReferenceData]) => Batch(label(ctx)),
      theme,
      _ => findBounds(1, theme.hasBorder)
    )

  final case class Theme(
      charSheet: CharSheet,
      up: TerminalTileColors,
      over: TerminalTileColors,
      down: TerminalTileColors,
      hasBorder: Boolean,
      borderTiles: TerminalBorderTiles
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

    def withBorderTiles(value: TerminalBorderTiles): Theme =
      this.copy(borderTiles = value)
    def modifyBorderTiles(f: TerminalBorderTiles => TerminalBorderTiles): Theme =
      this.copy(borderTiles = f(borderTiles))

  object Theme:

    def apply(charSheet: CharSheet, foreground: RGBA, background: RGBA, hasBorder: Boolean): Theme =
      Theme(
        charSheet,
        TerminalTileColors(foreground, background),
        TerminalTileColors(foreground, background),
        TerminalTileColors(foreground, background),
        hasBorder,
        TerminalBorderTiles.default
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
        hasBorder,
        TerminalBorderTiles.default
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
        hasBorder,
        TerminalBorderTiles.default
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
