package roguelikestarterkit.ui.components

import indigo.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.ui.*

object TerminalTextArea:

  private def findBounds(text: String): Bounds =
    val lines = text.split("\n").toList
    val maxLength =
      lines.foldLeft(0) { (acc, line) =>
        if line.length > acc then line.length else acc
      }
    Bounds(0, 0, maxLength, text.length)

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentTextArea[ReferenceData](
      charSheet: CharSheet,
      fgColor: RGBA,
      bgColor: RGBA
  ): (UIContext[ReferenceData], TextArea[ReferenceData]) => Outcome[Layer] = {
    case (context, textArea) =>
      val size = textArea.bounds(context).dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .putLines(
            Point.zero,
            Batch.fromList(textArea.text(context).split("\n").toList),
            fgColor,
            bgColor
          )
          .toCloneTiles(
            CloneId(s"label_${charSheet.assetName.toString}"),
            context.parent.coords.toScreenSpace(charSheet.size),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      Outcome(Layer.Content(terminal))
  }

  /** Creates a TerminalTextArea rendered using the RogueTerminalEmulator based on a
    * `TerminalTextArea.Theme`, with bounds based on the text length
    */
  def apply[ReferenceData](text: String, theme: Theme): TextArea[ReferenceData] =
    TextArea(
      _ => text,
      presentTextArea(
        theme.charSheet,
        theme.colors.foreground,
        theme.colors.background
      ),
      (_, t) => findBounds(t)
    )

  /** Creates a TerminalTextArea rendered using the RogueTerminalEmulator based on a
    * `TerminalTextArea.Theme`, with custom bounds
    */
  def apply[ReferenceData](
      text: UIContext[ReferenceData] => String,
      theme: Theme
  ): TextArea[ReferenceData] =
    TextArea(
      (ctx: UIContext[ReferenceData]) => text(ctx),
      presentTextArea(
        theme.charSheet,
        theme.colors.foreground,
        theme.colors.background
      ),
      (ctx, _) => findBounds(text(ctx))
    )

  final case class Theme(
      charSheet: CharSheet,
      colors: TerminalTileColors
  ):
    def withCharSheet(value: CharSheet): Theme =
      this.copy(charSheet = value)

    def withColors(foreground: RGBA, background: RGBA): Theme =
      this.copy(colors = TerminalTileColors(foreground, background))

  object Theme:
    def apply(charSheet: CharSheet, foreground: RGBA, background: RGBA): Theme =
      Theme(
        charSheet,
        TerminalTileColors(foreground, background)
      )

    def apply(charSheet: CharSheet): Theme =
      Theme(charSheet, RGBA.White, RGBA.Black)
