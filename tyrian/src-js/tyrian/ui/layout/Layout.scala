package tyrian.ui.layout

import indigoengine.shared.collections.Batch
import indigoengine.shared.optics.Lens
import tyrian.EmptyAttribute
import tyrian.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.LayoutDirection
import tyrian.ui.datatypes.Ratio
import tyrian.ui.datatypes.SpaceAlignment
import tyrian.ui.datatypes.Spacing
import tyrian.ui.datatypes.Wrapping
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride

final case class Layout(
    direction: LayoutDirection,
    children: Batch[UIElement[?, ?]],
    spacing: Spacing,
    spaceAlignment: SpaceAlignment,
    ratio: Ratio,
    wrapping: Wrapping,
    classNames: Set[String],
    id: Option[String],
    themeOverride: ThemeOverride[Unit]
) extends UIElement[Layout, Unit]:

  def withDirection(value: LayoutDirection): Layout =
    this.copy(direction = value)
  def toRow: Layout =
    withDirection(LayoutDirection.Row)
  def toColumn: Layout =
    withDirection(LayoutDirection.Column)

  def withSpacing(value: Spacing): Layout =
    this.copy(spacing = value)

  def withSpaceAlignment(value: SpaceAlignment): Layout =
    this.copy(spaceAlignment = value)

  def withRatio(value: Ratio): Layout =
    this.copy(ratio = value)
  // Is this the nicest way to express this?
  def ratio1: Layout  = withRatio(Ratio.one)
  def ratio2: Layout  = withRatio(Ratio(2))
  def ratio3: Layout  = withRatio(Ratio(3))
  def ratio4: Layout  = withRatio(Ratio(4))
  def ratio5: Layout  = withRatio(Ratio(5))
  def ratio6: Layout  = withRatio(Ratio(6))
  def ratio7: Layout  = withRatio(Ratio(7))
  def ratio8: Layout  = withRatio(Ratio(8))
  def ratio9: Layout  = withRatio(Ratio(9))
  def ratio10: Layout = withRatio(Ratio(10))

  def withWrapping(value: Wrapping): Layout =
    this.copy(wrapping = value)
  def wrap: Layout =
    withWrapping(Wrapping.Wrap)
  def noWrap: Layout =
    withWrapping(Wrapping.NoWrap)

  // Aid to memory: "'Justify' the main axis, 'Align' the cross."

  def spaceAround: Layout =
    withSpaceAlignment(SpaceAlignment.SpaceAround)

  def spaceBetween: Layout =
    withSpaceAlignment(SpaceAlignment.SpaceBetween)

  def spaceEvenly: Layout =
    withSpaceAlignment(SpaceAlignment.SpaceEvenly)

  def stretch: Layout =
    withSpaceAlignment(SpaceAlignment.Stretch)

  def withClassNames(classes: Set[String]): Layout =
    this.copy(classNames = classes)

  def withId(id: String): Layout =
    this.copy(id = Some(id))

  def themeLens: Lens[Theme.Default, Unit] =
    Lens.unit

  def withThemeOverride(value: ThemeOverride[Unit]): Layout =
    this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Layout.toHtml(this)

object Layout:

  import tyrian.Html.*
  import tyrian.Style

  def apply(children: UIElement[?, ?]*): Layout =
    Layout(LayoutDirection.Row, Batch.fromSeq(children))

  def apply(direction: LayoutDirection, children: Batch[UIElement[?, ?]]): Layout =
    Layout(
      direction = direction,
      children = children,
      spacing = Spacing.None,
      spaceAlignment = SpaceAlignment.Stretch,
      ratio = Ratio.default,
      wrapping = Wrapping.NoWrap,
      classNames = Set(),
      id = None,
      themeOverride = ThemeOverride.NoOverride
    )

  def apply(direction: LayoutDirection, children: UIElement[?, ?]*): Layout =
    Layout(direction, Batch.fromSeq(children))

  def row(children: UIElement[?, ?]*): Layout =
    Layout(LayoutDirection.Row, Batch.fromSeq(children))

  def column(children: UIElement[?, ?]*): Layout =
    Layout(LayoutDirection.Column, Batch.fromSeq(children))

  def toHtml(layout: Layout)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val baseStyles = Style(
      "display"         -> "flex",
      "justify-content" -> layout.spaceAlignment.toCSSValue,
      "align-items"     -> "stretch",
      "gap"             -> layout.spacing.toCSSValue,
      "flex-wrap"       -> layout.wrapping.toFlexCSSValue
    ) |+| layout.direction.toStyle |+| layout.ratio.toStyle

    val classAttribute =
      if layout.classNames.isEmpty then EmptyAttribute
      else cls := layout.classNames.mkString(" ")

    val idAttribute =
      layout.id.fold(EmptyAttribute)(id.:=.apply)

    val childrenHtml = layout.children.map(_.toElem)

    div(style(baseStyles), classAttribute, idAttribute)(childrenHtml.toList*)
