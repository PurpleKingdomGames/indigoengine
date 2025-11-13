package roguelikestarterkit.ui.components

import indigo.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.ui.*

/** TerminalLabel is a simple `Component` s that renders text using a Terminal.
  */
object TerminalLabel:

  private def findBounds(text: String): Bounds =
    Bounds(0, 0, text.length, 1)

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentLabel[ReferenceData](
      charSheet: CharSheet,
      fgColor: RGBA,
      bgColor: RGBA
  ): (UIContext[ReferenceData], Label[ReferenceData]) => Outcome[Layer] = { case (context, label) =>
    val size = label.bounds(context).dimensions.unsafeToSize

    val terminal =
      RogueTerminalEmulator(size)
        .putLine(Point.zero, label.text(context), fgColor, bgColor)
        .toCloneTiles(
          CloneId(s"label_${charSheet.assetName.toString}"),
          context.parent.coords.toScreenSpace(charSheet.size),
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
  def apply[ReferenceData](
      text: UIContext[ReferenceData] => String,
      theme: Theme
  ): Label[ReferenceData] =
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
