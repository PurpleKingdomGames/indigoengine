package roguelikestarterkit.ui.components

import indigo.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions

/** TerminalLabel are a simple `Component` s that render text using a Terminal.
  */
object TerminalLabel:

  private def findBounds(text: String): Bounds =
    Bounds(0, 0, text.length, 1)

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentLabel(
      charSheet: CharSheet,
      fgColor: RGBA,
      bgColor: RGBA
  ): (Coords, String, Dimensions) => Outcome[Layer] = { case (offset, label, dimensions) =>
    val size = dimensions.unsafeToSize

    val terminal =
      RogueTerminalEmulator(size)
        .putLine(Point.zero, label, fgColor, bgColor)
        .toCloneTiles(
          CloneId(s"label_${charSheet.assetName.toString}"),
          offset.toScreenSpace(charSheet.size),
          charSheet.charCrops
        ) { case (fg, bg) =>
          graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
        }

    Outcome(Layer.Content(terminal))
  }

  /** Creates a Label rendered using the RogueTerminalEmulator based on a `Label.Theme`, with bounds
    * based on the text length.
    */
  def apply[ReferenceData](text: String, theme: Theme): Label[ReferenceData] =
    Label(
      _ => text,
      presentLabel(theme.charSheet, theme.colors.foreground, theme.colors.background),
      (_, t) => findBounds(t)
    )

  /** Creates a Label with dynamic text, rendered using the RogueTerminalEmulator based on a
    * `Label.Theme`, with bounds based on the text length.
    */
  def apply[ReferenceData](text: ReferenceData => String, theme: Theme): Label[ReferenceData] =
    Label(
      text,
      presentLabel(theme.charSheet, theme.colors.foreground, theme.colors.background),
      (_, t) => findBounds(t)
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
