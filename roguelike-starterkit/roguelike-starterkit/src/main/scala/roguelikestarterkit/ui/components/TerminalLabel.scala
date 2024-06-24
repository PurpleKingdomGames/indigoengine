package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.component.StatelessComponent
import roguelikestarterkit.ui.components.TerminalTileColors
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UIContext

/** TerminalLabel are a simple `Component`s that render text using a Terminal.
  */
object TerminalLabel:

  private def findBounds(text: String): Bounds =
    Bounds(0, 0, text.length, 1)

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentLabel(
      charSheet: CharSheet,
      fgColor: RGBA,
      bgColor: RGBA
  ): (Coords, String, Dimensions) => Outcome[ComponentFragment] = {
    case (offset, label, dimensions) =>
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

      Outcome(ComponentFragment(terminal))
  }

  /** Creates a Label rendered using the RogueTerminalEmulator based on a `Label.Theme`, with bounds
    * based on the text length
    */
  def apply[ReferenceData](text: String, theme: Theme): Label[ReferenceData] =
    Label(
      _ => text,
      presentLabel(theme.charSheet, theme.colors.foreground, theme.colors.background),
      (_, t) => findBounds(t)
    )

  /** Creates a Label rendered using the RogueTerminalEmulator based on a `Label.Theme`, with custom
    * bounds
    */
  def apply[ReferenceData](text: ReferenceData => String, theme: Theme): Label[ReferenceData] =
    Label(
      text,
      presentLabel(theme.charSheet, theme.colors.foreground, theme.colors.background),
      (_, t) => findBounds(t)
    )

  given [ReferenceData]: StatelessComponent[Label[ReferenceData], ReferenceData] with
    def bounds(reference: ReferenceData, model: Label[ReferenceData]): Bounds =
      model.calculateBounds(reference, model.text(reference))

    def present(
        context: UIContext[ReferenceData],
        model: Label[ReferenceData]
    ): Outcome[ComponentFragment] =
      val t = model.text(context.reference)
      model.render(context.bounds.coords, t, model.calculateBounds(context.reference, t).dimensions)

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
