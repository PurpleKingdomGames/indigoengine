package roguelikestarterkit.ui.components.group

import indigo.*
import roguelikestarterkit.ui.component.*
import roguelikestarterkit.ui.datatypes.*

import scala.annotation.tailrec

/** Encapsulates a collection of components and describes and manages their layout, as well as
  * propagating update and presention calls.
  */
final case class ComponentGroup[ReferenceData] private (
    bounds: Bounds,
    boundsType: BoundsType,
    layout: ComponentLayout,
    components: Batch[ComponentEntry[?, ReferenceData]],
    referenceBounds: Batch[Bounds]
):

  lazy val contentBounds: Bounds =
    val allBounds: Batch[Rectangle] = referenceBounds.map(_.unsafeToRectangle)

    if allBounds.isEmpty then Bounds.zero
    else
      val h = allBounds.head
      val t = allBounds.tail

      Bounds(t.foldLeft(h) { case (acc, r) => acc.expandToInclude(r) })

  private def addSingle[A](entry: A)(using
      c: Component[A, ReferenceData]
  ): ComponentGroup[ReferenceData] =
    this.copy(
      components = components :+
        ComponentEntry(ComponentGroup.calculateNextOffset(bounds, layout)(components), entry, c),
      referenceBounds = referenceBounds :+ c.bounds(entry)
    )

  def add[A](entry: A)(using c: Component[A, ReferenceData]): ComponentGroup[ReferenceData] =
    addSingle(entry).reflow.cascade(bounds)

  def add[A](entries: Batch[A])(using
      c: Component[A, ReferenceData]
  ): ComponentGroup[ReferenceData] =
    entries.foldLeft(this) { case (acc, next) => acc.addSingle(next) }.reflow.cascade(bounds)
  def add[A](entries: A*)(using c: Component[A, ReferenceData]): ComponentGroup[ReferenceData] =
    add(Batch.fromSeq(entries))

  def withBounds(value: Bounds): ComponentGroup[ReferenceData] =
    this.copy(bounds = value).reflow

  def withBoundsType(value: BoundsType): ComponentGroup[ReferenceData] =
    this.copy(boundsType = value).reflow

  def defaultBounds: ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.default)
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
  def dynamicBounds(width: FitMode, height: FitMode): ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.Dynamic(width, height))

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

  // Delegates, for convenience.

  def update[StartupData, ContextData](
      context: UiContext[ReferenceData]
  ): GlobalEvent => Outcome[ComponentGroup[ReferenceData]] =
    summon[Component[ComponentGroup[ReferenceData], ReferenceData]].updateModel(context, this)

  def present[StartupData, ContextData](
      context: UiContext[ReferenceData]
  ): Outcome[ComponentFragment] =
    summon[Component[ComponentGroup[ReferenceData], ReferenceData]].present(context, this)

  def reflow: ComponentGroup[ReferenceData] =
    summon[Component[ComponentGroup[ReferenceData], ReferenceData]].reflow(this)

  def cascade(parentBounds: Bounds): ComponentGroup[ReferenceData] =
    summon[Component[ComponentGroup[ReferenceData], ReferenceData]].cascade(this, parentBounds)

object ComponentGroup:
  def apply[ReferenceData](initalBounds: Bounds): ComponentGroup[ReferenceData] =
    ComponentGroup(
      initalBounds,
      BoundsType.default,
      ComponentLayout.None,
      Batch.empty,
      Batch.empty
    )

  given [ReferenceData]: Component[ComponentGroup[ReferenceData], ReferenceData] with

    def bounds(model: ComponentGroup[ReferenceData]): Bounds =
      model.bounds

    def updateModel(
        context: UiContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): GlobalEvent => Outcome[ComponentGroup[ReferenceData]] =
      case FrameTick =>
        updateComponents(context, model)(FrameTick).map { updated =>
          if updated.referenceBounds != model.referenceBounds then updated.reflow
          else updated
        }

      case e =>
        updateComponents(context, model)(e)

    private def updateComponents[StartupData, ContextData](
        context: UiContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): GlobalEvent => Outcome[ComponentGroup[ReferenceData]] =
      e =>
        model.components
          .map { c =>
            c.component
              .updateModel(context.copy(bounds = context.bounds.moveBy(c.offset)), c.model)(e)
              .map { updated =>
                c.copy(model = updated)
              }
          }
          .sequence
          .map { updatedComponents =>
            model.copy(
              components = updatedComponents,
              referenceBounds = updatedComponents.map { c =>
                c.component.bounds(c.model)
              }
            )
          }

    def present(
        context: UiContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): Outcome[ComponentFragment] =
      model.components
        .map { c =>
          c.component.present(context.copy(bounds = context.bounds.moveBy(c.offset)), c.model)
        }
        .sequence
        .map(_.foldLeft(ComponentFragment.empty)(_ |+| _))

    def reflow(model: ComponentGroup[ReferenceData]): ComponentGroup[ReferenceData] =
      val reflowed: Batch[ComponentEntry[?, ReferenceData]] = model.components.map { c =>
        c.copy(
          model = c.component.reflow(c.model)
        )
      }

      val nextOffset = calculateNextOffset[ReferenceData](model.bounds, model.layout)

      val newComponents = reflowed.foldLeft(Batch.empty[ComponentEntry[?, ReferenceData]]) {
        (acc, entry) =>
          val reflowed = entry.copy(
            offset = nextOffset(acc),
            model = entry.component.reflow(entry.model)
          )

          acc :+ reflowed
      }

      val newReferenceBounds =
        newComponents.map(c => c.component.bounds(c.model).withPosition(c.offset))

      model.copy(
        components = newComponents,
        referenceBounds = newReferenceBounds
      )

    def cascade(
        model: ComponentGroup[ReferenceData],
        parentBounds: Bounds
    ): ComponentGroup[ReferenceData] =
      val newBounds: Bounds =
        model.boundsType match
          case BoundsType.Fixed =>
            model.bounds

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
            model.bounds.withPosition(
              (parentBounds.width.toDouble * x).toInt,
              (parentBounds.height.toDouble * y).toInt
            )

          case BoundsType.RelativeSize(width, height) =>
            model.bounds.withDimensions(
              (parentBounds.width.toDouble * width).toInt,
              (parentBounds.height.toDouble * height).toInt
            )

          case BoundsType.Offset(amountPosition, amountSize) =>
            Bounds(parentBounds.coords + amountPosition, parentBounds.dimensions + amountSize)

          case BoundsType.OffsetPosition(amount) =>
            model.bounds.withPosition(parentBounds.coords + amount)

          case BoundsType.OffsetSize(amount) =>
            model.bounds.withDimensions(parentBounds.dimensions + amount)

          case BoundsType.Dynamic(FitMode.Available, FitMode.Available) =>
            parentBounds

          case BoundsType.Dynamic(FitMode.Available, FitMode.Content) =>
            parentBounds.withDimensions(
              parentBounds.dimensions.width,
              model.contentBounds.dimensions.height
            )

          case BoundsType.Dynamic(FitMode.Available, FitMode.Fixed(units)) =>
            parentBounds.withDimensions(parentBounds.dimensions.width, units)

          case BoundsType.Dynamic(FitMode.Content, FitMode.Available) =>
            parentBounds.withDimensions(
              model.contentBounds.dimensions.height,
              parentBounds.dimensions.width
            )

          case BoundsType.Dynamic(FitMode.Content, FitMode.Content) =>
            model.contentBounds

          case BoundsType.Dynamic(FitMode.Content, FitMode.Fixed(units)) =>
            model.contentBounds.withDimensions(model.contentBounds.dimensions.width, units)

          case BoundsType.Dynamic(FitMode.Fixed(units), FitMode.Available) =>
            parentBounds.withDimensions(units, parentBounds.dimensions.height)

          case BoundsType.Dynamic(FitMode.Fixed(units), FitMode.Content) =>
            model.contentBounds.withDimensions(units, model.contentBounds.dimensions.height)

          case BoundsType.Dynamic(FitMode.Fixed(unitsW), FitMode.Fixed(unitsH)) =>
            model.bounds.withDimensions(unitsW, unitsH)

      model
        .copy(
          bounds = newBounds,
          components = model.components.map(_.cascade(newBounds))
        )

  extension (b: Bounds)
    private def withPadding(p: Padding): Bounds =
      b.moveBy(p.left, p.top).resize(b.width + p.right, b.height + p.bottom)

  private def calculateNextOffset[ReferenceData](bounds: Bounds, layout: ComponentLayout)(
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): Coords =
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
