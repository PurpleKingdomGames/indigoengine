package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.tiles.RoguelikeTiles10x10
import roguelikestarterkit.tiles.RoguelikeTiles5x6
import roguelikestarterkit.ui.component.Component
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.CharSheet
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

import scala.annotation.targetName

/** Labels are a simple `Component` that render text.
  */
final case class Label(text: String, render: (Coords, String) => Outcome[ComponentFragment]):
  def withText(value: String): Label =
    this.copy(text = value)

object Label:

  /** Minimal label constructor with custom rendering function
    */
  @targetName("Label_apply_curried")
  def apply(text: String)(present: (Coords, String) => Outcome[ComponentFragment]): Label =
    Label(text, present)

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentLabel(
      charSheet: CharSheet,
      fgColor: RGBA,
      bgColor: RGBA
  ): (Coords, String) => Outcome[ComponentFragment] = { case (offset, label) =>
    val dimensions = Dimensions(label.length, 1)
    val size       = dimensions.unsafeToSize

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

  /** Creates a Label rendered using the RogueTerminalEmulator based on a `Label.Theme`, with custom
    * bounds
    */
  def apply(text: String, theme: Theme): Label =
    Label(text, presentLabel(theme.charSheet, theme.colors.foreground, theme.colors.background))

  given Component[Label] with
    def bounds(model: Label): Bounds =
      Bounds(0, 0, model.text.length, 1)

    def updateModel[ReferenceData](
        context: UiContext[ReferenceData],
        model: Label
    ): GlobalEvent => Outcome[Label] =
      case _ =>
        Outcome(model)

    def present[ReferenceData](
        context: UiContext[ReferenceData],
        model: Label
    ): Outcome[ComponentFragment] =
      model.render(context.bounds.coords, model.text)

    def reflow(model: Label): Label =
      model

    def cascade(model: Label, parentBounds: Bounds): Label =
      model

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
