package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.ui.components.TerminalTileColors
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions

import scala.annotation.targetName

object TerminalTextArea:

  private def findBounds(text: List[String]): Bounds =
    val maxLength =
      text.foldLeft(0) { (acc, line) =>
        if line.length > acc then line.length else acc
      }
    Bounds(0, 0, maxLength, text.length)

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentTextArea(
      charSheet: CharSheet,
      fgColor: RGBA,
      bgColor: RGBA
  ): (Coords, List[String], Dimensions) => Outcome[Layer] = {
    case (offset, label, dimensions) =>
      val size = dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .putLines(Point.zero, Batch.fromList(label), fgColor, bgColor)
          .toCloneTiles(
            CloneId(s"label_${charSheet.assetName.toString}"),
            offset.toScreenSpace(charSheet.size),
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
    val t = text.split("\n").toList

    TextArea(
      _ => t,
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
  def apply[ReferenceData](text: ReferenceData => String, theme: Theme): TextArea[ReferenceData] =
    TextArea(
      (r: ReferenceData) => text(r).split("\n").toList,
      presentTextArea(
        theme.charSheet,
        theme.colors.foreground,
        theme.colors.background
      ),
      (r, t) => findBounds(text(r).split("\n").toList)
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
