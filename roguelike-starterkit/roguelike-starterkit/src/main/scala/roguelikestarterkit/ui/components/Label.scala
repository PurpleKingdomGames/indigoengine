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
final case class Label[ReferenceData](
    text: ReferenceData => String,
    bounds: Bounds,
    render: (Coords, String) => Outcome[ComponentFragment],
    resize: Batch[GlobalEvent]
):
  def withText(value: String): Label[ReferenceData] =
    this.copy(text = _ => value)
  def withText(f: ReferenceData => String): Label[ReferenceData] =
    this.copy(text = f)

  def withBounds(value: Bounds): Label[ReferenceData] =
    this.copy(bounds = value)

  def onResize(events: Batch[GlobalEvent]): Label[ReferenceData] =
    this.copy(resize = events)
  def onResize(events: GlobalEvent*): Label[ReferenceData] =
    onResize(Batch.fromSeq(events))

object Label:

  private def findBounds(text: String): Bounds =
    Bounds(0, 0, text.length, 1)

  /** Minimal label constructor with custom rendering function
    */
  def apply[ReferenceData](text: String)(
      present: (Coords, String) => Outcome[ComponentFragment]
  ): Label[ReferenceData] =
    Label(_ => text, findBounds(text), present, Batch.empty)

  @targetName("Label_apply_curried")
  def apply[ReferenceData](text: ReferenceData => String)(
      present: (Coords, String) => Outcome[ComponentFragment]
  ): Label[ReferenceData] =
    Label(text, Bounds(0, 0, 1, 1), present, Batch.empty)

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

  /** Creates a Label rendered using the RogueTerminalEmulator based on a `Label.Theme`, with bounds
    * based on the text length
    */
  def apply[ReferenceData](text: String, theme: Theme): Label[ReferenceData] =
    Label(
      _ => text,
      findBounds(text),
      presentLabel(theme.charSheet, theme.colors.foreground, theme.colors.background),
      Batch.empty
    )

  /** Creates a Label rendered using the RogueTerminalEmulator based on a `Label.Theme`, with custom
    * bounds
    */
  def apply[ReferenceData](text: ReferenceData => String, theme: Theme): Label[ReferenceData] =
    Label(
      text,
      Bounds(0, 0, 1, 1),
      presentLabel(theme.charSheet, theme.colors.foreground, theme.colors.background),
      Batch.empty
    )

  /** Creates a Label rendered using the RogueTerminalEmulator based on a `Label.Theme`, with custom
    * bounds
    */
  def apply[ReferenceData](
      text: ReferenceData => String,
      theme: Theme,
      onResize: GlobalEvent
  ): Label[ReferenceData] =
    Label(
      text,
      Bounds(0, 0, 1, 1),
      presentLabel(theme.charSheet, theme.colors.foreground, theme.colors.background),
      Batch(onResize)
    )

  /** Creates a Label rendered using the RogueTerminalEmulator based on a `Label.Theme`, with custom
    * bounds
    */
  def apply[ReferenceData](
      text: ReferenceData => String,
      theme: Theme,
      onResize: Batch[GlobalEvent]
  ): Label[ReferenceData] =
    Label(
      text,
      Bounds(0, 0, 1, 1),
      presentLabel(theme.charSheet, theme.colors.foreground, theme.colors.background),
      onResize
    )

  given [ReferenceData]: Component[Label[ReferenceData], ReferenceData] with
    def bounds(model: Label[ReferenceData]): Bounds =
      model.bounds

    def updateModel(
        context: UiContext[ReferenceData],
        model: Label[ReferenceData]
    ): GlobalEvent => Outcome[Label[ReferenceData]] =
      case FrameTick =>
        val newBounds = findBounds(model.text(context.reference))

        Outcome(
          model.withBounds(newBounds),
          if model.bounds != newBounds then model.resize else Batch.empty
        )

      case _ =>
        Outcome(model)

    def present(
        context: UiContext[ReferenceData],
        model: Label[ReferenceData]
    ): Outcome[ComponentFragment] =
      model.render(context.bounds.coords, model.text(context.reference))

    def reflow(model: Label[ReferenceData]): Label[ReferenceData] =
      model

    def cascade(model: Label[ReferenceData], parentBounds: Bounds): Label[ReferenceData] =
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
