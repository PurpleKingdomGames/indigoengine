package roguelikestarterkit.ui.components

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.Tile
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

/** Input components allow the user to input text information.
  */
final case class Input(
    placeholder: String,
    text: Option[String],
    bounds: Bounds,
    render: (Coords, Bounds, String, Boolean) => Outcome[ComponentFragment]
):
  def withText(value: String): Input =
    this.copy(text = Option(value))

  def withBounds(value: Bounds): Input =
    this.copy(bounds = value)

  def withWidth(value: Int): Input =
    this.copy(bounds = bounds.withDimensions(value, bounds.height))

  def withPlaceholder(value: String): Input =
    this.copy(placeholder = value)

  // Delegates, for convenience.

  def update[StartupData, ContextData](
      context: UiContext[?]
  ): GlobalEvent => Outcome[Input] =
    summon[Component[Input, ?]].updateModel(context, this)

  def present[StartupData, ContextData](
      context: UiContext[?]
  ): Outcome[ComponentFragment] =
    summon[Component[Input, ?]].present(context, this)

  def reflow: Input =
    summon[Component[Input, ?]].reflow(this)

  def cascade(parentBounds: Bounds): Input =
    summon[Component[Input, ?]].cascade(this, parentBounds)

object Input:

  /** Minimal input constructor with custom rendering function
    */
  def apply(bounds: Bounds)(
      present: (Coords, Bounds, String, Boolean) => Outcome[ComponentFragment]
  ): Input =
    Input("", None, bounds, present)

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def presentInput[ReferenceData](
      charSheet: CharSheet,
      fgColor: RGBA,
      bgColor: RGBA
  ): (Coords, Bounds, String, Boolean) => Outcome[ComponentFragment] =
    (offset, bounds, label, isPlaceholder) =>
      val correctedLabel =
        if label.length == bounds.width then label
        else if label.length > bounds.width then label.take(bounds.width)
        else label + (List.fill(bounds.width - label.length)(" ").mkString)

      val hBar = Batch.fill(correctedLabel.length)("─").mkString
      val size = (bounds.dimensions + 2).unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .put(Point(0, 0), Tile.`┌`, fgColor, bgColor)
          .put(Point(size.width - 1, 0), Tile.`┐`, fgColor, bgColor)
          .put(Point(0, size.height - 1), Tile.`└`, fgColor, bgColor)
          .put(Point(size.width - 1, size.height - 1), Tile.`┘`, fgColor, bgColor)
          .put(Point(0, 1), Tile.`│`, fgColor, bgColor)
          .put(Point(size.width - 1, 1), Tile.`│`, fgColor, bgColor)
          .putLine(Point(1, 0), hBar, fgColor, bgColor)
          .putLine(
            Point(1, 1),
            correctedLabel,
            if isPlaceholder then fgColor.mix(bgColor, 0.66) else fgColor,
            bgColor
          )
          .putLine(Point(1, 2), hBar, fgColor, bgColor)

      val terminalClones =
        terminal
          .toCloneTiles(
            CloneId(s"input_${charSheet.assetName.toString}"),
            bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(offset.toScreenSpace(charSheet.size)),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      Outcome(ComponentFragment(terminalClones))

  /** Creates a Input rendered using the RogueTerminalEmulator based on a `Input.Theme`, where the
    * bounds are the supplied width, height 1, plus border.
    */
  def apply(width: Int, theme: Theme): Input =
    Input(
      "",
      None,
      Bounds(0, 0, width, 1),
      presentInput(theme.charSheet, theme.colors.foreground, theme.colors.background)
    )

  /** Creates a Input rendered using the RogueTerminalEmulator based on a `Input.Theme`, with
    * placeholder text, where the bounds are the supplied width, height 1, plus border.
    */
  def apply(placeholder: String, width: Int, theme: Theme): Input =
    Input(
      placeholder,
      None,
      Bounds(0, 0, width, 1),
      presentInput(theme.charSheet, theme.colors.foreground, theme.colors.background)
    )

  given [ReferenceData]: Component[Input, ReferenceData] with
    def bounds(model: Input): Bounds =
      model.bounds

    def updateModel(
        context: UiContext[ReferenceData],
        model: Input
    ): GlobalEvent => Outcome[Input] =
      case _ =>
        Outcome(model)

    def present(
        context: UiContext[ReferenceData],
        model: Input
    ): Outcome[ComponentFragment] =
      model.render(
        context.bounds.coords,
        model.bounds,
        model.text.getOrElse(model.placeholder),
        model.text.isEmpty
      )

    def reflow(model: Input): Input =
      model

    def cascade(model: Input, parentBounds: Bounds): Input =
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
