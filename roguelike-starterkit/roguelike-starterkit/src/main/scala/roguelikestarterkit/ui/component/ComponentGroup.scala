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
final case class ComponentGroup(
    bounds: Bounds,
    boundsType: BoundsType,
    layout: ComponentLayout,
    components: Batch[ComponentEntry[?]]
):

  extension (b: Bounds)
    def withPadding(p: Padding): Bounds =
      b.moveBy(p.left, p.top).resize(b.width + p.right, b.height + p.bottom)

  def nextOffset(components: Batch[ComponentEntry[?]]): Coords =
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

  def reflow: ComponentGroup =
    val newComponents = components.foldLeft(Batch.empty[ComponentEntry[?]]) { (acc, entry) =>
      val reflowed = entry.copy(
        offset = nextOffset(acc),
        model = entry.component.reflow(entry.model)
      )

      acc :+ reflowed
    }

    this.copy(components = newComponents)

  def cascade(parentBounds: Bounds): ComponentGroup =
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

  def add[A](entry: A)(using c: Component[A]): ComponentGroup =
    this.copy(components = components :+ ComponentEntry(nextOffset(components), entry, c))

  def add[A](entries: Batch[A])(using c: Component[A]): ComponentGroup =
    entries.foldLeft(this) { case (acc, next) => acc.add(next) }
  def add[A](entries: A*)(using c: Component[A]): ComponentGroup =
    add(Batch.fromSeq(entries))

  def update[StartupData, ContextData, ReferenceData](
      context: UiContext[ReferenceData]
  ): GlobalEvent => Outcome[ComponentGroup] =
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

  def present[StartupData, ContextData, ReferenceData](
      context: UiContext[ReferenceData]
  ): Outcome[ComponentFragment] =
    components
      .map { c =>
        c.component.present(context.copy(bounds = context.bounds.moveBy(c.offset)), c.model)
      }
      .sequence
      .map(_.foldLeft(ComponentFragment.empty)(_ |+| _))

  def withBounds(value: Bounds): ComponentGroup =
    this.copy(bounds = value).reflow

  def withBoundsType(value: BoundsType): ComponentGroup =
    this.copy(boundsType = value).reflow

  def fixedBounds: ComponentGroup =
    withBoundsType(BoundsType.Fixed)
  def inheritBounds: ComponentGroup =
    withBoundsType(BoundsType.Inherit)
  def relative(x: Double, y: Double, width: Double, height: Double): ComponentGroup =
    withBoundsType(BoundsType.Relative(x, y, width, height))
  def relativePosition(x: Double, y: Double): ComponentGroup =
    withBoundsType(BoundsType.RelativePosition(x, y))
  def relativeSize(width: Double, height: Double): ComponentGroup =
    withBoundsType(BoundsType.RelativeSize(width, height))
  def offset(amountPosition: Coords, amountSize: Dimensions): ComponentGroup =
    withBoundsType(BoundsType.Offset(amountPosition, amountSize))
  def offset(x: Int, y: Int, width: Int, height: Int): ComponentGroup =
    offset(Coords(x, y), Dimensions(width, height))
  def offsetPosition(amount: Coords): ComponentGroup =
    withBoundsType(BoundsType.OffsetPosition(amount))
  def offsetPosition(x: Int, y: Int): ComponentGroup =
    offsetPosition(Coords(x, y))
  def offsetSize(amount: Dimensions): ComponentGroup =
    withBoundsType(BoundsType.OffsetSize(amount))
  def offsetSize(width: Int, height: Int): ComponentGroup =
    offsetSize(Dimensions(width, height))

  def withLayout(value: ComponentLayout): ComponentGroup =
    this.copy(layout = value).reflow

  def withPosition(value: Coords): ComponentGroup =
    withBounds(bounds.withPosition(value))
  def moveTo(position: Coords): ComponentGroup =
    withPosition(position)
  def moveTo(x: Int, y: Int): ComponentGroup =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): ComponentGroup =
    withPosition(bounds.coords + amount)
  def moveBy(x: Int, y: Int): ComponentGroup =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): ComponentGroup =
    withBounds(bounds.withDimensions(value))
  def resizeTo(size: Dimensions): ComponentGroup =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): ComponentGroup =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): ComponentGroup =
    withDimensions(bounds.dimensions + amount)
  def resizeBy(x: Int, y: Int): ComponentGroup =
    resizeBy(Dimensions(x, y))

object ComponentGroup:
  def apply(bounds: Bounds): ComponentGroup =
    ComponentGroup(bounds, BoundsType.Fixed, ComponentLayout.None, Batch.empty)

  given Component[ComponentGroup] with

    def bounds(model: ComponentGroup): Bounds =
      model.bounds

    def updateModel[ReferenceData](
        context: UiContext[ReferenceData],
        model: ComponentGroup
    ): GlobalEvent => Outcome[ComponentGroup] =
      case e => model.update(context)(e)

    def present[ReferenceData](
        context: UiContext[ReferenceData],
        model: ComponentGroup
    ): Outcome[ComponentFragment] =
      model.present(context)

    def reflow(model: ComponentGroup): ComponentGroup =
      val reflowed: Batch[ComponentEntry[?]] = model.components.map { c =>
        c.copy(
          model = c.component.reflow(c.model)
        )
      }
      model.reflow.copy(components = reflowed)

    def cascade(model: ComponentGroup, parentBounds: Bounds): ComponentGroup =
      model.cascade(parentBounds)
