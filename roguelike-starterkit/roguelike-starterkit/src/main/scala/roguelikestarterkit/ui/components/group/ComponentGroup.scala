package roguelikestarterkit.ui.components.group

import indigo.*
import roguelikestarterkit.tiles.Tile
import roguelikestarterkit.ui.component.*
import roguelikestarterkit.ui.components.common.ComponentEntry
import roguelikestarterkit.ui.components.common.ComponentLayout
import roguelikestarterkit.ui.components.common.ContainerLikeFunctions
import roguelikestarterkit.ui.components.common.Overflow
import roguelikestarterkit.ui.components.common.Padding
import roguelikestarterkit.ui.datatypes.*

import scala.annotation.tailrec

/** Describes a fixed arrangement of components, manages their layout, and propagates updates and
  * presention calls.
  */
final case class ComponentGroup[ReferenceData] private[group] (
    boundsType: BoundsType,
    layout: ComponentLayout,
    components: Batch[ComponentEntry[?, ReferenceData]],
    bounds: Bounds,
    contentBounds: Bounds,
    dirty: Boolean
):

  private def addSingle[A](entry: A)(using
      c: Component[A, ReferenceData]
  ): ComponentGroup[ReferenceData] =
    this.copy(
      components = components :+ ComponentEntry(Coords.zero, entry, c),
      dirty = true
    )

  def add[A](entry: A)(using c: Component[A, ReferenceData]): ComponentGroup[ReferenceData] =
    addSingle(entry)

  def add[A](entries: Batch[A])(using
      c: Component[A, ReferenceData]
  ): ComponentGroup[ReferenceData] =
    entries.foldLeft(this.copy(dirty = true)) { case (acc, next) => acc.addSingle(next) }
  def add[A](entries: A*)(using c: Component[A, ReferenceData]): ComponentGroup[ReferenceData] =
    add(Batch.fromSeq(entries))

  def withBounds(value: Bounds): ComponentGroup[ReferenceData] =
    this.copy(bounds = value, dirty = true)

  def withBoundsType(value: BoundsType): ComponentGroup[ReferenceData] =
    value match
      case BoundsType.Fixed(bounds) =>
        this.copy(boundsType = value, bounds = bounds, dirty = true)

      case _ =>
        this.copy(boundsType = value, dirty = true)

  def defaultBounds: ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.default)
  def fixedBounds(value: Bounds): ComponentGroup[ReferenceData] =
    withBoundsType(BoundsType.Fixed(value))
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
    this.copy(layout = value, dirty = true)

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

  def apply[ReferenceData](): ComponentGroup[ReferenceData] =
    ComponentGroup(
      BoundsType.default,
      ComponentLayout.Horizontal(Padding.zero, Overflow.Wrap),
      Batch.empty,
      Bounds.zero,
      Bounds.zero,
      dirty = true
    )

  def apply[ReferenceData](boundsType: BoundsType): ComponentGroup[ReferenceData] =
    ComponentGroup(
      boundsType,
      ComponentLayout.Horizontal(Padding.zero, Overflow.Wrap),
      Batch.empty,
      Bounds.zero,
      Bounds.zero,
      dirty = true
    )

  def apply[ReferenceData](bounds: Bounds): ComponentGroup[ReferenceData] =
    ComponentGroup(
      BoundsType.Fixed(bounds),
      ComponentLayout.Horizontal(Padding.zero, Overflow.Wrap),
      Batch.empty,
      bounds,
      Bounds.zero,
      dirty = true
    )

  given [ReferenceData]: Component[ComponentGroup[ReferenceData], ReferenceData] with

    def bounds(reference: ReferenceData, model: ComponentGroup[ReferenceData]): Bounds =
      model.bounds

    def updateModel(
        context: UiContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): GlobalEvent => Outcome[ComponentGroup[ReferenceData]] =
      case FrameTick =>
        updateComponents(context, model)(FrameTick).map { updated =>
          if model.dirty then
            refreshLayout(context.reference, context.bounds, updated)
          else updated
        }

      case e =>
        updateComponents(context, model)(e)

    private[group] def refreshLayout(
        reference: ReferenceData,
        parentBounds: Bounds,
        model: ComponentGroup[ReferenceData]
    ) =
      val reflowed: ComponentGroup[ReferenceData] = reflow(reference, model)
      val cascaded: ComponentGroup[ReferenceData] = cascade(reflowed, parentBounds)
      val contentBounds: Bounds =
        calculateContentBounds(reference, cascaded.components)

      val updatedBounds =
        cascaded.boundsType match
          case BoundsType.Dynamic(FitMode.Content, FitMode.Content) =>
            cascaded.bounds.withDimensions(contentBounds.dimensions)

          case BoundsType.Dynamic(FitMode.Content, _) =>
            cascaded.bounds.withWidth(contentBounds.width)

          case BoundsType.Dynamic(_, FitMode.Content) =>
            cascaded.bounds.withHeight(contentBounds.height)

          case _ =>
            cascaded.bounds

      cascaded.copy(
        dirty = false,
        contentBounds = contentBounds,
        bounds = updatedBounds
      )

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
              components = updatedComponents
            )
          }

    def present(
        context: UiContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): Outcome[ComponentFragment] =
      ContainerLikeFunctions.present(context, model.components)

    def reflow(
        reference: ReferenceData,
        model: ComponentGroup[ReferenceData]
    ): ComponentGroup[ReferenceData] =
      val reflowed: Batch[ComponentEntry[?, ReferenceData]] = model.components.map { c =>
        c.copy(
          model = c.component.reflow(reference, c.model)
        )
      }

      val nextOffset =
        ContainerLikeFunctions.calculateNextOffset[ReferenceData](model.bounds, model.layout)

      val newComponents = reflowed.foldLeft(Batch.empty[ComponentEntry[?, ReferenceData]]) {
        (acc, entry) =>
          val reflowed = entry.copy(
            offset = nextOffset(reference, acc),
            model = entry.component.reflow(reference, entry.model)
          )

          acc :+ reflowed
      }

      model.copy(
        components = newComponents
      )

    def cascade(
        model: ComponentGroup[ReferenceData],
        parentBounds: Bounds
    ): ComponentGroup[ReferenceData] =
      val newBounds: Bounds =
        calculateCascadeBounds(
          model.bounds,
          model.contentBounds,
          parentBounds,
          model.boundsType
        )

      model
        .copy(
          bounds = newBounds,
          components = model.components.map(_.cascade(newBounds)),
          dirty = true
        )

  def calculateCascadeBounds(
      currentBounds: Bounds,
      contentBounds: Bounds,
      parentBounds: Bounds,
      boundsType: BoundsType
  ): Bounds =
    boundsType match
      case BoundsType.Fixed(b) =>
        b

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
        currentBounds.withPosition(
          (parentBounds.width.toDouble * x).toInt,
          (parentBounds.height.toDouble * y).toInt
        )

      case BoundsType.RelativeSize(width, height) =>
        currentBounds.withDimensions(
          (parentBounds.width.toDouble * width).toInt,
          (parentBounds.height.toDouble * height).toInt
        )

      case BoundsType.Offset(amountPosition, amountSize) =>
        Bounds(parentBounds.coords + amountPosition, parentBounds.dimensions + amountSize)

      case BoundsType.OffsetPosition(amount) =>
        currentBounds.withPosition(parentBounds.coords + amount)

      case BoundsType.OffsetSize(amount) =>
        currentBounds.withDimensions(parentBounds.dimensions + amount)

      case BoundsType.Dynamic(FitMode.Available, FitMode.Available) =>
        parentBounds

      case BoundsType.Dynamic(FitMode.Available, FitMode.Content) =>
        parentBounds.withDimensions(
          parentBounds.dimensions.width,
          contentBounds.dimensions.height
        )

      case BoundsType.Dynamic(FitMode.Available, FitMode.Fixed(units)) =>
        parentBounds.withDimensions(parentBounds.dimensions.width, units)

      case BoundsType.Dynamic(FitMode.Content, FitMode.Available) =>
        parentBounds.withDimensions(
          contentBounds.dimensions.height,
          parentBounds.dimensions.width
        )

      case BoundsType.Dynamic(FitMode.Content, FitMode.Content) =>
        contentBounds

      case BoundsType.Dynamic(FitMode.Content, FitMode.Fixed(units)) =>
        contentBounds.withDimensions(contentBounds.dimensions.width, units)

      case BoundsType.Dynamic(FitMode.Fixed(units), FitMode.Available) =>
        parentBounds.withDimensions(units, parentBounds.dimensions.height)

      case BoundsType.Dynamic(FitMode.Fixed(units), FitMode.Content) =>
        contentBounds.withDimensions(units, contentBounds.dimensions.height)

      case BoundsType.Dynamic(FitMode.Fixed(unitsW), FitMode.Fixed(unitsH)) =>
        currentBounds.withDimensions(unitsW, unitsH)

  def calculateContentBounds[ReferenceData](
      reference: ReferenceData,
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): Bounds =
    components.foldLeft(Bounds.zero) { (acc, c) =>
      val bounds = c.component.bounds(reference, c.model).moveTo(c.offset)
      acc.expandToInclude(bounds)
    }
