package roguelikestarterkit.ui.components.group

import indigo.*
import roguelikestarterkit.ui.component.*
import roguelikestarterkit.ui.datatypes.*

import scala.annotation.tailrec

/** Describes a dynamic list of 'stateless' components, their realtive layout, and propagates update
  * and presention calls.
  */
final case class ComponentList[ReferenceData] private (
    content: ReferenceData => Batch[ComponentEntry[?, ReferenceData]],
    // Config
    boundsType: BoundsType,
    layout: ComponentLayout,
    // View model data
    contentHash: Int,
    computedComponents: Batch[ComponentEntry[?, ReferenceData]],
    computedBounds: Bounds,
    referenceBounds: Batch[Bounds]
):

  // This is private and is only used for testing
  private[group] def withComponents(
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): ComponentList[ReferenceData] =
    this.copy(computedComponents = components)

  lazy val contentBounds: Bounds =
    val allBounds: Batch[Rectangle] = referenceBounds.map(_.unsafeToRectangle)

    if allBounds.isEmpty then Bounds.zero
    else
      val h = allBounds.head
      val t = allBounds.tail

      Bounds(t.foldLeft(h) { case (acc, r) => acc.expandToInclude(r) })

  def withBounds(value: Bounds): ComponentList[ReferenceData] =
    this.copy(computedBounds = value).reflow

  def withBoundsType(value: BoundsType): ComponentList[ReferenceData] =
    value match
      case BoundsType.Fixed(bounds) =>
        this.copy(boundsType = value, computedBounds = bounds).reflow

      case _ =>
        this.copy(boundsType = value).reflow

  def defaultBounds: ComponentList[ReferenceData] =
    withBoundsType(BoundsType.default)
  def fixedBounds(value: Bounds): ComponentList[ReferenceData] =
    withBoundsType(BoundsType.Fixed(value))
  def inheritBounds: ComponentList[ReferenceData] =
    withBoundsType(BoundsType.Inherit)
  def relative(x: Double, y: Double, width: Double, height: Double): ComponentList[ReferenceData] =
    withBoundsType(BoundsType.Relative(x, y, width, height))
  def relativePosition(x: Double, y: Double): ComponentList[ReferenceData] =
    withBoundsType(BoundsType.RelativePosition(x, y))
  def relativeSize(width: Double, height: Double): ComponentList[ReferenceData] =
    withBoundsType(BoundsType.RelativeSize(width, height))
  def offset(amountPosition: Coords, amountSize: Dimensions): ComponentList[ReferenceData] =
    withBoundsType(BoundsType.Offset(amountPosition, amountSize))
  def offset(x: Int, y: Int, width: Int, height: Int): ComponentList[ReferenceData] =
    offset(Coords(x, y), Dimensions(width, height))
  def offsetPosition(amount: Coords): ComponentList[ReferenceData] =
    withBoundsType(BoundsType.OffsetPosition(amount))
  def offsetPosition(x: Int, y: Int): ComponentList[ReferenceData] =
    offsetPosition(Coords(x, y))
  def offsetSize(amount: Dimensions): ComponentList[ReferenceData] =
    withBoundsType(BoundsType.OffsetSize(amount))
  def offsetSize(width: Int, height: Int): ComponentList[ReferenceData] =
    offsetSize(Dimensions(width, height))
  def dynamicBounds(width: FitMode, height: FitMode): ComponentList[ReferenceData] =
    withBoundsType(BoundsType.Dynamic(width, height))

  def withLayout(value: ComponentLayout): ComponentList[ReferenceData] =
    this.copy(layout = value).reflow

  def withPosition(value: Coords): ComponentList[ReferenceData] =
    withBounds(computedBounds.withPosition(value))
  def moveTo(position: Coords): ComponentList[ReferenceData] =
    withPosition(position)
  def moveTo(x: Int, y: Int): ComponentList[ReferenceData] =
    moveTo(Coords(x, y))
  def moveBy(amount: Coords): ComponentList[ReferenceData] =
    withPosition(computedBounds.coords + amount)
  def moveBy(x: Int, y: Int): ComponentList[ReferenceData] =
    moveBy(Coords(x, y))

  def withDimensions(value: Dimensions): ComponentList[ReferenceData] =
    withBounds(computedBounds.withDimensions(value))
  def resizeTo(size: Dimensions): ComponentList[ReferenceData] =
    withDimensions(size)
  def resizeTo(x: Int, y: Int): ComponentList[ReferenceData] =
    resizeTo(Dimensions(x, y))
  def resizeBy(amount: Dimensions): ComponentList[ReferenceData] =
    withDimensions(computedBounds.dimensions + amount)
  def resizeBy(x: Int, y: Int): ComponentList[ReferenceData] =
    resizeBy(Dimensions(x, y))

  // Delegates, for convenience.

  def update[StartupData, ContextData](
      context: UiContext[ReferenceData]
  ): GlobalEvent => Outcome[ComponentList[ReferenceData]] =
    summon[Component[ComponentList[ReferenceData], ReferenceData]].updateModel(context, this)

  def present[StartupData, ContextData](
      context: UiContext[ReferenceData]
  ): Outcome[ComponentFragment] =
    summon[Component[ComponentList[ReferenceData], ReferenceData]].present(context, this)

  def reflow: ComponentList[ReferenceData] =
    summon[Component[ComponentList[ReferenceData], ReferenceData]].reflow(this)

  def cascade(parentBounds: Bounds): ComponentList[ReferenceData] =
    summon[Component[ComponentList[ReferenceData], ReferenceData]].cascade(this, parentBounds)

object ComponentList:

  def apply[ReferenceData, A](contents: ReferenceData => Batch[A])(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: ReferenceData => Batch[ComponentEntry[A, ReferenceData]] =
      r => contents(r).map(v => ComponentEntry(Coords.zero, v, c))

    ComponentList(
      f,
      BoundsType.default,
      ComponentLayout.None,
      -1,
      Batch.empty,
      Bounds.zero,
      Batch.empty
    )

  def apply[ReferenceData, A](
      bounds: Bounds
  )(contents: ReferenceData => Batch[A])(using
      c: StatelessComponent[A, ReferenceData]
  ): ComponentList[ReferenceData] =
    val f: ReferenceData => Batch[ComponentEntry[A, ReferenceData]] =
      r => contents(r).map(v => ComponentEntry(Coords.zero, v, c))

    ComponentList(
      f,
      BoundsType.default,
      ComponentLayout.None,
      -1,
      Batch.empty,
      bounds,
      Batch.empty
    )

  // Replace once this issue is resolved: https://github.com/PurpleKingdomGames/indigo/issues/740
  extension [A <: Any](batch: Batch[A])
    @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
    def customHashCode: Int =
      val prime  = 31
      var result = 1
      for (element <- batch)
        result = prime * result + (if (element == null) 0 else element.hashCode)
      result

  given [ReferenceData]: Component[ComponentList[ReferenceData], ReferenceData] with

    def bounds(model: ComponentList[ReferenceData]): Bounds =
      model.computedBounds

    def updateModel(
        context: UiContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): GlobalEvent => Outcome[ComponentList[ReferenceData]] =
      case FrameTick =>
        updateComponents(context, model)(FrameTick).map { updated =>
          if updated.referenceBounds != model.referenceBounds then updated.reflow
          else updated
        }

      case e =>
        updateComponents(context, model)(e)

    private def updateComponents[StartupData, ContextData](
        context: UiContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): GlobalEvent => Outcome[ComponentList[ReferenceData]] =
      e =>
        val entries = model.content(context.reference)

        val modelWithComponents =
          if entries.customHashCode == model.contentHash then model
          else
            model
              .copy(computedComponents = entries, contentHash = entries.customHashCode)
              .reflow
              .cascade(model.computedBounds)

        modelWithComponents.computedComponents
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
              computedComponents = updatedComponents,
              referenceBounds = updatedComponents.map { c =>
                c.component.bounds(c.model)
              }
            )
          }

    def present(
        context: UiContext[ReferenceData],
        model: ComponentList[ReferenceData]
    ): Outcome[ComponentFragment] =
      GroupFunctions.present(context, model.computedComponents)

    def reflow(model: ComponentList[ReferenceData]): ComponentList[ReferenceData] =
      val nextOffset =
        GroupFunctions.calculateNextOffset[ReferenceData](model.computedBounds, model.layout)

      val newComponents =
        model.computedComponents.foldLeft(Batch.empty[ComponentEntry[?, ReferenceData]]) {
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
        computedComponents = newComponents,
        referenceBounds = newReferenceBounds
      )

    def cascade(
        model: ComponentList[ReferenceData],
        parentBounds: Bounds
    ): ComponentList[ReferenceData] =
      val newBounds: Bounds =
        GroupFunctions.calculateCascadeBounds(
          model.computedBounds,
          model.contentBounds,
          parentBounds,
          model.boundsType
        )

      model
        .copy(
          computedBounds = newBounds,
          computedComponents = model.computedComponents.map(_.cascade(newBounds))
        )
