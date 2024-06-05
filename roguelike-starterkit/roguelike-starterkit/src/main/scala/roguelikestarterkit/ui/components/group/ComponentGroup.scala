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
    // Internal
    bounds: Bounds,        // The actual cached bounds of the group
    contentBounds: Bounds, // The calculated and cached bounds of the content
    dirty: Boolean // Whether the groups content needs to be refreshed, and it's bounds recalculated
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
    this.copy(boundsType = value, dirty = true)

  def withLayout(value: ComponentLayout): ComponentGroup[ReferenceData] =
    this.copy(layout = value, dirty = true)

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

  def apply[ReferenceData](dimensions: Dimensions): ComponentGroup[ReferenceData] =
    ComponentGroup(
      BoundsType.fixed(dimensions),
      ComponentLayout.Horizontal(Padding.zero, Overflow.Wrap),
      Batch.empty,
      Bounds(dimensions),
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
        // Sub-groups will naturally refresh themselves as needed
        updateComponents(context, model)(FrameTick).map { updated =>
          if model.dirty then refresh(context.reference, updated, context.bounds)
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
              components = updatedComponents
            )
          }

    def present(
        context: UiContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): Outcome[ComponentFragment] =
      ContainerLikeFunctions.present(context, model.components)

    def refresh(
        reference: ReferenceData,
        model: ComponentGroup[ReferenceData],
        parentBounds: Bounds
    ): ComponentGroup[ReferenceData] =

      // First, calculate the bounds without content
      val boundsWithoutContent =
        model.boundsType match
          case BoundsType(FitMode.Available, FitMode.Available) =>
            parentBounds

          case BoundsType(FitMode.Available, FitMode.Content) =>
            parentBounds.withHeight(0)

          case BoundsType(FitMode.Available, FitMode.Fixed(height)) =>
            parentBounds.withHeight(height)

          case BoundsType(FitMode.Available, FitMode.Relative(amountH)) =>
            parentBounds.withHeight((parentBounds.height * amountH).toInt)

          case BoundsType(FitMode.Content, FitMode.Available) =>
            Bounds.zero.withHeight(parentBounds.height)

          case BoundsType(FitMode.Content, FitMode.Content) =>
            Bounds.zero

          case BoundsType(FitMode.Content, FitMode.Fixed(height)) =>
            Bounds.zero.withHeight(height)

          case BoundsType(FitMode.Content, FitMode.Relative(amountH)) =>
            Bounds.zero.withHeight((parentBounds.height * amountH).toInt)

          case BoundsType(FitMode.Fixed(width), FitMode.Available) =>
            Bounds.zero.withDimensions(width, parentBounds.height)

          case BoundsType(FitMode.Fixed(width), FitMode.Content) =>
            Bounds.zero.withWidth(width)

          case BoundsType(FitMode.Fixed(width), FitMode.Fixed(height)) =>
            Bounds.zero.withDimensions(width, height)

          case BoundsType(FitMode.Fixed(width), FitMode.Relative(amountH)) =>
            Bounds.zero.withDimensions(width, (parentBounds.height * amountH).toInt)

          case BoundsType(FitMode.Relative(amountW), FitMode.Available) =>
            Bounds.zero.withDimensions((parentBounds.width * amountW).toInt, parentBounds.height)

          case BoundsType(FitMode.Relative(amountW), FitMode.Content) =>
            Bounds.zero.withWidth((parentBounds.width * amountW).toInt)

          case BoundsType(FitMode.Relative(amountW), FitMode.Fixed(height)) =>
            Bounds.zero.withDimensions((parentBounds.width * amountW).toInt, height)

          case BoundsType(FitMode.Relative(amountW), FitMode.Relative(amountH)) =>
            Bounds.zero.withDimensions(
              (parentBounds.width * amountW).toInt,
              (parentBounds.height * amountH).toInt
            )

      // Next, loop over all the children, calling refresh on each one, and supplying the best guess for the bounds
      val updatedComponents =
        model.components.map { c =>
          val refreshed = c.component.refresh(reference, c.model, boundsWithoutContent)
          c.copy(model = refreshed)
        }

      // Now we need to set the offset of each child, based on the layout
      val withOffsets =
        updatedComponents.foldLeft(Batch.empty[ComponentEntry[?, ReferenceData]]) { (acc, next) =>
          val nextOffset =
            ContainerLikeFunctions.calculateNextOffset[ReferenceData](
              boundsWithoutContent,
              model.layout
            )(reference, acc)

          acc :+ next.copy(offset = nextOffset)
        }

      // Now we can calculate the content bounds
      val contentBounds: Bounds =
        withOffsets.foldLeft(Bounds.zero) { (acc, c) =>
          val bounds = c.component.bounds(reference, c.model).moveTo(c.offset)
          acc.expandToInclude(bounds)
        }

      // And finally, we can calculate the boundsWithoutContent updating in the FitMode.Content cases and leaving as-is in others
      val updatedBounds =
        model.boundsType match
          case BoundsType(FitMode.Content, FitMode.Content) =>
            boundsWithoutContent.withDimensions(contentBounds.dimensions)

          case BoundsType(FitMode.Content, _) =>
            boundsWithoutContent.withWidth(contentBounds.width)

          case BoundsType(_, FitMode.Content) =>
            boundsWithoutContent.withHeight(contentBounds.height)

          case _ =>
            boundsWithoutContent

      // Return the updated model with the new bounds and content bounds and dirty flag reset
      model.copy(
        dirty = false,
        contentBounds = contentBounds,
        bounds = updatedBounds,
        components = withOffsets
      )
