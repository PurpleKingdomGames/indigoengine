package roguelikestarterkit.ui.components

import indigo.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.ui.*

object TerminalInput:

  /** Creates a TerminalInput rendered using the RogueTerminalEmulator based on a
    * `TerminalInput.Theme`, where the bounds are the supplied width, height 1, plus border.
    */
  def apply[ReferenceData](placeholder: String, width: Int, theme: Theme): Input[ReferenceData] =
    Input[ReferenceData](
      text = placeholder,
      dimensions = Dimensions(width, 1) + 2, // Account for border
      render = presentInput(
        theme.charSheet,
        theme.colors.foreground,
        theme.colors.background,
        theme.borderTiles
      ),
      change = _ => Batch.empty,
      calculateLineLength = (_, label) => label.length,
      characterLimit = width,
      cursor = Cursor.default,
      hasFocus = false,
      onFocus = () => Batch.empty,
      onLoseFocus = () => Batch.empty
    )

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def drawCursor(
      offset: Coords,
      cursorPosition: Int,
      charSheet: CharSheet,
      color: RGBA
  ): Batch[SceneNode] =
    Batch(
      Shape.Box(
        Rectangle(
          (offset + Coords(cursorPosition, 0) + 1).toScreenSpace(charSheet.size),
          Size(
            Math.max(1, charSheet.size.width / 5).toInt,
            charSheet.size.height
          )
        ),
        Fill.Color(color)
      )
    )

  private def presentInput[ReferenceData](
      charSheet: CharSheet,
      fgColor: RGBA,
      bgColor: RGBA,
      borderTiles: TerminalBorderTiles
  ): (UIContext[ReferenceData], Input[ReferenceData]) => Outcome[Layer] =
    (context, input) =>
      val bounds = input.bounds(context)
      val correctedLabel =
        if input.text.length == bounds.width then input.text
        else if input.text.length > bounds.width then input.text.take(bounds.width)
        else input.text + (List.fill(bounds.width - input.text.length)(" ").mkString)

      val hBar = Batch.fill(correctedLabel.length)(borderTiles.horizontal)
      val size = (bounds.dimensions + 2).unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .put(Point(0, 0), borderTiles.topLeft, fgColor, bgColor)
          .put(Point(size.width - 1, 0), borderTiles.topRight, fgColor, bgColor)
          .put(Point(0, 2), borderTiles.bottomLeft, fgColor, bgColor)
          .put(Point(size.width - 1, 2), borderTiles.bottomRight, fgColor, bgColor)
          .put(Point(0, 1), borderTiles.vertical, fgColor, bgColor)
          .put(Point(size.width - 1, 1), borderTiles.vertical, fgColor, bgColor)
          .putTileLine(Point(1, 0), hBar, fgColor, bgColor)
          .putLine(
            Point(1, 1),
            correctedLabel,
            fgColor,
            bgColor
          )
          .putTileLine(Point(1, 2), hBar, fgColor, bgColor)

      val terminalClones =
        terminal
          .toCloneTiles(
            CloneId(s"input_${charSheet.assetName.toString}"),
            bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(context.parent.coords.toScreenSpace(charSheet.size)),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      val cursor: Batch[SceneNode] =
        if input.hasFocus then
          input.cursor.blinkRate match
            case None =>
              drawCursor(context.parent.coords, input.cursor.position, charSheet, fgColor)

            case Some(blinkRate) =>
              Signal
                .Pulse(blinkRate)
                .map(p =>
                  if (context.frame.time.running - input.cursor.lastModified < Seconds(0.5)) true
                  else p
                )
                .map {
                  case false =>
                    Batch.empty

                  case true =>
                    drawCursor(context.parent.coords, input.cursor.position, charSheet, fgColor)
                }
                .at(context.frame.time.running)
        else Batch.empty

      Outcome(Layer.Content(terminalClones).addNodes(cursor))

  final case class Theme(
      charSheet: CharSheet,
      colors: TerminalTileColors,
      borderTiles: TerminalBorderTiles
  ):
    def withCharSheet(value: CharSheet): Theme =
      this.copy(charSheet = value)

    def withColors(foreground: RGBA, background: RGBA): Theme =
      this.copy(colors = TerminalTileColors(foreground, background))

    def withBorderTiles(value: TerminalBorderTiles): Theme =
      this.copy(borderTiles = value)
    def modifyBorderTiles(f: TerminalBorderTiles => TerminalBorderTiles): Theme =
      this.copy(borderTiles = f(borderTiles))

  object Theme:
    def apply(charSheet: CharSheet, foreground: RGBA, background: RGBA): Theme =
      Theme(
        charSheet,
        TerminalTileColors(foreground, background),
        TerminalBorderTiles.default
      )

    def apply(charSheet: CharSheet): Theme =
      Theme(charSheet, RGBA.White, RGBA.Black)
