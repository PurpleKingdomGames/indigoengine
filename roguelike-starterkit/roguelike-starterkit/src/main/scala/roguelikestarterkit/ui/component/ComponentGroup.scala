package roguelikestarterkit.ui.component

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

import scala.annotation.tailrec

/** Encapsulates a collection of components and describes and manages their layout, as well as
  * propagating update and presention calls.
  */
final case class ComponentGroup[ReferenceData](
    bounds: Bounds,
    boundsType: BoundsType,
    layout: ComponentLayout,
    components: Batch[ComponentEntry[?, ReferenceData]]
):

  extension (b: Bounds)
    def withPadding(p: Padding): Bounds =
      b.moveBy(p.left, p.top).resize(b.width + p.right, b.height + p.bottom)

  def nextOffset(components: Batch[ComponentEntry[?, ReferenceData]]): Coords =
    layout match
      case ComponentLayout.None =>
        Coords.zero

      case ComponentLayout.Horizontal(padding, Overflow.Hidden) =>
        components
          .takeRight(1)
          .headOption
          .map(c => c.offset + Coords(c.component.bounds(c.model).withPadding(padding).right, 0))
          .getOrElse(Coords(padding.left, padding.top))

      case ComponentLayout.Horizontal(padding, Overflow.Wrap) =>
        val maxY = components
          .map(c => c.offset.y + c.component.bounds(c.model).withPadding(padding).height)
          .sortWith(_ > _)
          .headOption
          .getOrElse(0)

        components
          .takeRight(1)
          .headOption
          .map { c =>
            val padded      = c.component.bounds(c.model).withPadding(padding)
            val maybeOffset = c.offset + Coords(padded.right, 0)

            if padded.moveBy(maybeOffset).right < bounds.width then maybeOffset
            else Coords(padding.left, maxY)
          }
          .getOrElse(Coords(padding.left, padding.top))

      case ComponentLayout.Vertical(padding) =>
        components
          .takeRight(1)
          .headOption
          .map(c => c.offset + Coords(0, c.component.bounds(c.model).withPadding(padding).bottom))
          .getOrElse(Coords(padding.left, padding.top))

  def reflow: ComponentGroup[ReferenceData] =
    val newComponents = components.foldLeft(Batch.empty[ComponentEntry[?, ReferenceData]]) {
      (acc, entry) =>
        val reflowed = entry.copy(
          offset = nextOffset(acc),
          model = entry.component.reflow(entry.model)
        )

        acc :+ reflowed
    }

    this.copy(components = newComponents)

  def cascade(parentBounds: Bounds): ComponentGroup[ReferenceData] =
    val newBounds =
      boundsType match
        case BoundsType.Fixed =>
          bounds

        case BoundsType.Inherit =>
          parentBounds

        case BoundsType.Relative(x, y, width, height) =>
          Bounds(
            (parentBounds.width.toDouble * x).toInt,
            (parentBounds.height.toDouble * y).toInt,
            (parentBounds.width.toDouble * width).toInt,
            (parentBounds.height.toDouble * height).toInt
          )

        case BoundsType.RelativePosition(x, y) =>
          bounds.withPosition(
            (parentBounds.width.toDouble * x).toInt,
            (parentBounds.height.toDouble * y).toInt
          )

        case BoundsType.RelativeSize(width, height) =>
          bounds.withDimensions(
            (parentBounds.width.toDouble * width).toInt,
            (parentBounds.height.toDouble * height).toInt
          )

        case BoundsType.Offset(amountPosition, amountSize) =>
          Bounds(parentBounds.coords + amountPosition, parentBounds.dimensions + amountSize)

        case BoundsType.OffsetPosition(amount) =>
          bounds.withPosition(parentBounds.coords + amount)

        case BoundsType.OffsetSize(amount) =>
          bounds.withDimensions(parentBounds.dimensions + amount)

    withBounds(newBounds)
      .copy(
        components = components.map(_.cascade(newBounds))
      )
      .reflow

  def add[A](entry: A)(using c: Component[A, ReferenceData]): ComponentGroup[ReferenceData] =
    this.copy(components = components :+ ComponentEntry(nextOffset(components), entry, c))

  def add[A](entries: Batch[A])(using
      c: Component[A, ReferenceData]
  ): ComponentGroup[ReferenceData] =
    entries.foldLeft(this) { case (acc, next) => acc.add(next) }
  def add[A](entries: A*)(using c: Component[A, ReferenceData]): ComponentGroup[ReferenceData] =
    add(Batch.fromSeq(entries))

  def update[StartupData, ContextData](
      context: UiContext[ReferenceData]
  ): GlobalEvent => Outcome[ComponentGroup[ReferenceData]] =
    e =>
      components
        .map { c =>
          c.component
            .updateModel(context.copy(bounds = context.bounds.moveBy(c.offset)), c.model)(e)
            .map { updated =>
              c.copy(model = updated)
            }
        }
        .sequence
        .map { updatedComponents =>
          this.copy(
            components = updatedComponents
          )
        }

  def present[StartupData, ContextData](
      context: UiContext[ReferenceData]
  ): Outcome[ComponentFragment] =
    components
      .map { c =>
        c.component.present(context.copy(bounds = context.bounds.moveBy(c.offset)), c.model)
      }
      .sequence
      .map(_.foldLeft(ComponentFragment.empty)(_ |+| _))

  def withBounds(value: Bounds): ComponentGroup[ReferenceData] =
    this.copy(bounds = value).reflow

  def withBoundsType(value: BoundsType): ComponentGroup[ReferenceData] =
    this.copy(boundsType = value).reflow

  def fixedBounds: ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.Fixed)
  def inheritBounds: ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.Inherit)
  def relative(x: Double, y: Double, width: Double, height: Double): ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.Relative(x, y, width, height))
  def relativePosition(x: Double, y: Double): ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.RelativePosition(x, y))
  def relativeSize(width: Double, height: Double): ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.RelativeSize(width, height))
  def offset(amountPosition: Coords, amountSize: Dimensions): ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.Offset(amountPosition, amountSize))
  def offset(x: Int, y: Int, width: Int, height: Int): ComponentGroup[ReferenceData] =
    offset(Coords(x, y), Dimensions(width, height))
  def offsetPosition(amount: Coords): ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.OffsetPosition(amount))
  def offsetPosition(x: Int, y: Int): ComponentGroup[ReferenceData] =
    offsetPosition(Coords(x, y))
  def offsetSize(amount: Dimensions): ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.OffsetSize(amount))
  def offsetSize(width: Int, height: Int): ComponentGroup[ReferenceData] =
    offsetSize(Dimensions(width, height))

  def withLayout(value: ComponentLayout): ComponentGroup[ReferenceData] =
    this.copy(layout = value).reflow

  def withPosition(value: Coords): ComponentGroup[ReferenceData] =
    withBounds(bounds.withPosition(value))
  def moveTo(position: Coords): ComponentGroup[ReferenceData] =
    withPosition(position)
  def moveTo(x: Int, y: Int): ComponentGroup[ReferenceData] =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): ComponentGroup[ReferenceData] =
    withPosition(bounds.coords + amount)
  def moveBy(x: Int, y: Int): ComponentGroup[ReferenceData] =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): ComponentGroup[ReferenceData] =
    withBounds(bounds.withDimensions(value))
  def resizeTo(size: Dimensions): ComponentGroup[ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): ComponentGroup[ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): ComponentGroup[ReferenceData] =
    withDimensions(bounds.dimensions + amount)
  def resizeBy(x: Int, y: Int): ComponentGroup[ReferenceData] =
    resizeBy(Dimensions(x, y))

object ComponentGroup:
  def apply[ReferenceData](bounds: Bounds): ComponentGroup[ReferenceData] =
    ComponentGroup(bounds, BoundsType.Fixed, ComponentLayout.None, Batch.empty)

  given [ReferenceData]: Component[ComponentGroup[ReferenceData], ReferenceData] with

    def bounds(model: ComponentGroup[ReferenceData]): Bounds =
      model.bounds

    def updateModel(
        context: UiContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): GlobalEvent => Outcome[ComponentGroup[ReferenceData]] =
      case e => model.update(context)(e)

    def present(
        context: UiContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): Outcome[ComponentFragment] =
      model.present(context)

    def reflow(model: ComponentGroup[ReferenceData]): ComponentGroup[ReferenceData] =
      val reflowed: Batch[ComponentEntry[?, ReferenceData]] = model.components.map { c =>
        c.copy(
          model = c.component.reflow(c.model)
        )
      }
      model.reflow.copy(components = reflowed)

    def cascade(
        model: ComponentGroup[ReferenceData],
        parentBounds: Bounds
    ): ComponentGroup[ReferenceData] =
      model.cascade(parentBounds)
