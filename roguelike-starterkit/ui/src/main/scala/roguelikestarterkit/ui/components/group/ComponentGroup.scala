package roguelikestarterkit.ui.components.group

import indigo.*
import roguelikestarterkit.ui.component.*
import roguelikestarterkit.ui.components.common.Anchor
import roguelikestarterkit.ui.components.common.ComponentEntry
import roguelikestarterkit.ui.components.common.ComponentId
import roguelikestarterkit.ui.components.common.ComponentLayout
import roguelikestarterkit.ui.components.common.ContainerLikeFunctions
import roguelikestarterkit.ui.components.common.Overflow
import roguelikestarterkit.ui.components.common.Padding
import roguelikestarterkit.ui.datatypes.*

import scala.annotation.tailrec

/** Describes a fixed arrangement of components, manages their layout, which may include anchored
  * components.
  */
final case class ComponentGroup[ReferenceData] private[group] (
    boundsType: BoundsMode,
    layout: ComponentLayout,
    components: Batch[ComponentEntry[?, ReferenceData]],
    background: Bounds => Layer,
    // Internal
    dimensions: Dimensions, // The actual cached dimensions of the group
    contentBounds: Bounds,  // The calculated and cached bounds of the content
    dirty: Boolean // Whether the groups content needs to be refreshed, and it's bounds recalculated
):

  private def addSingle[A](entry: A)(using
      c: Component[A, ReferenceData]
  ): ComponentGroup[ReferenceData] =
    this.copy(
      components = components :+ ComponentEntry(ComponentId.None, Coords.zero, entry, c, None),
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

  def anchor[A](entry: A, anchor: Anchor)(using
      c: Component[A, ReferenceData]
  ): ComponentGroup[ReferenceData] =
    this.copy(
      components =
        components :+ ComponentEntry(ComponentId.None, Coords.zero, entry, c, Option(anchor)),
      dirty = true
    )

  def withDimensions(value: Dimensions): ComponentGroup[ReferenceData] =
    this.copy(dimensions = value, dirty = true)

  def withBoundsMode(value: BoundsMode): ComponentGroup[ReferenceData] =
    this.copy(boundsType = value, dirty = true)

  def withLayout(value: ComponentLayout): ComponentGroup[ReferenceData] =
    this.copy(layout = value, dirty = true)

  def withBackground(present: Bounds => Layer): ComponentGroup[ReferenceData] =
    this.copy(background = present, dirty = true)

object ComponentGroup:

  def apply[ReferenceData](): ComponentGroup[ReferenceData] =
    ComponentGroup(
      BoundsMode.default,
      ComponentLayout.Horizontal(Padding.zero, Overflow.Wrap),
      Batch.empty,
      _ => Layer.empty,
      Dimensions.zero,
      Bounds.zero,
      dirty = true
    )

  def apply[ReferenceData](boundsType: BoundsMode): ComponentGroup[ReferenceData] =
    ComponentGroup(
      boundsType,
      ComponentLayout.Horizontal(Padding.zero, Overflow.Wrap),
      Batch.empty,
      _ => Layer.empty,
      Dimensions.zero,
      Bounds.zero,
      dirty = true
    )

  def apply[ReferenceData](dimensions: Dimensions): ComponentGroup[ReferenceData] =
    ComponentGroup(
      BoundsMode.fixed(dimensions),
      ComponentLayout.Horizontal(Padding.zero, Overflow.Wrap),
      Batch.empty,
      _ => Layer.empty,
      dimensions,
      Bounds.zero,
      dirty = true
    )

  def apply[ReferenceData](width: Int, height: Int): ComponentGroup[ReferenceData] =
    ComponentGroup(Dimensions(width, height))

  given [ReferenceData]: Component[ComponentGroup[ReferenceData], ReferenceData] with

    def bounds(reference: ReferenceData, model: ComponentGroup[ReferenceData]): Bounds =
      Bounds(model.dimensions)

    def updateModel(
        context: UIContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): GlobalEvent => Outcome[ComponentGroup[ReferenceData]] =
      case FrameTick =>
        // Sub-groups will naturally refresh themselves as needed
        updateComponents(context, model)(FrameTick).map { updated =>
          if model.dirty then refresh(context.reference, updated, context.bounds.dimensions)
          else updated
        }

      case e =>
        updateComponents(context, model)(e)

    private def updateComponents[StartupData, ContextData](
        context: UIContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): GlobalEvent => Outcome[ComponentGroup[ReferenceData]] =
      e =>
        model.components
          .map { c =>
            c.component
              .updateModel(
                context
                  .copy(bounds = Bounds(context.bounds.moveBy(c.offset).coords, model.dimensions)),
                c.model
              )(e)
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
        context: UIContext[ReferenceData],
        model: ComponentGroup[ReferenceData]
    ): Outcome[Layer] =
      ContainerLikeFunctions.present(context, model.dimensions, model.components).map {
        components =>
          val background = model.background(Bounds(context.bounds.coords, model.dimensions))
          Layer.Stack(background, components)
      }

    def refresh(
        reference: ReferenceData,
        model: ComponentGroup[ReferenceData],
        parentDimensions: Dimensions
    ): ComponentGroup[ReferenceData] =

      // First, calculate the bounds without content
      val boundsWithoutContent =
        model.boundsType match

          // Available

          case BoundsMode(FitMode.Available, FitMode.Available) =>
            parentDimensions

          case BoundsMode(FitMode.Available, FitMode.Content) =>
            parentDimensions.withHeight(0)

          case BoundsMode(FitMode.Available, FitMode.Fixed(height)) =>
            parentDimensions.withHeight(height)

          case BoundsMode(FitMode.Available, FitMode.Relative(amountH)) =>
            parentDimensions.withHeight((parentDimensions.height * amountH).toInt)

          case BoundsMode(FitMode.Available, FitMode.Offset(amount)) =>
            parentDimensions.withHeight(parentDimensions.height + amount)

          // Content

          case BoundsMode(FitMode.Content, FitMode.Available) =>
            Dimensions(0, parentDimensions.height)

          case BoundsMode(FitMode.Content, FitMode.Content) =>
            Dimensions.zero

          case BoundsMode(FitMode.Content, FitMode.Fixed(height)) =>
            Dimensions(0, height)

          case BoundsMode(FitMode.Content, FitMode.Relative(amountH)) =>
            Dimensions(0, (parentDimensions.height * amountH).toInt)

          case BoundsMode(FitMode.Content, FitMode.Offset(amount)) =>
            Dimensions(0, parentDimensions.height + amount)

          // Fixed

          case BoundsMode(FitMode.Fixed(width), FitMode.Available) =>
            Dimensions(width, parentDimensions.height)

          case BoundsMode(FitMode.Fixed(width), FitMode.Content) =>
            Dimensions(width, 0)

          case BoundsMode(FitMode.Fixed(width), FitMode.Fixed(height)) =>
            Dimensions(width, height)

          case BoundsMode(FitMode.Fixed(width), FitMode.Relative(amountH)) =>
            Dimensions(width, (parentDimensions.height * amountH).toInt)

          case BoundsMode(FitMode.Fixed(width), FitMode.Offset(amount)) =>
            Dimensions(width, parentDimensions.height + amount)

          // Relative

          case BoundsMode(FitMode.Relative(amountW), FitMode.Available) =>
            Dimensions((parentDimensions.width * amountW).toInt, parentDimensions.height)

          case BoundsMode(FitMode.Relative(amountW), FitMode.Content) =>
            Dimensions((parentDimensions.width * amountW).toInt, 0)

          case BoundsMode(FitMode.Relative(amountW), FitMode.Fixed(height)) =>
            Dimensions((parentDimensions.width * amountW).toInt, height)

          case BoundsMode(FitMode.Relative(amountW), FitMode.Relative(amountH)) =>
            Dimensions(
              (parentDimensions.width * amountW).toInt,
              (parentDimensions.height * amountH).toInt
            )

          case BoundsMode(FitMode.Relative(amountW), FitMode.Offset(amount)) =>
            Dimensions((parentDimensions.width * amountW).toInt, parentDimensions.height + amount)

          // Offset

          case BoundsMode(FitMode.Offset(amount), FitMode.Available) =>
            parentDimensions.withWidth(parentDimensions.width + amount)

          case BoundsMode(FitMode.Offset(amount), FitMode.Content) =>
            Dimensions(parentDimensions.width + amount, 0)

          case BoundsMode(FitMode.Offset(amount), FitMode.Fixed(height)) =>
            Dimensions(parentDimensions.width + amount, height)

          case BoundsMode(FitMode.Offset(amount), FitMode.Relative(amountH)) =>
            Dimensions(parentDimensions.width + amount, (parentDimensions.height * amountH).toInt)

          case BoundsMode(FitMode.Offset(w), FitMode.Offset(h)) =>
            parentDimensions + Dimensions(w, h)

      // Next, loop over all the children, calling refresh on each one, and supplying the best guess for the bounds
      val updatedComponents =
        model.components.map { c =>
          val refreshed = c.component.refresh(reference, c.model, boundsWithoutContent)
          c.copy(model = refreshed)
        }

      // Now we need to set the offset of each child, based on the layout
      val withOffsets =
        updatedComponents.foldLeft(Batch.empty[ComponentEntry[?, ReferenceData]]) { (acc, next) =>
          next.anchor match
            case None =>
              val nextOffset =
                ContainerLikeFunctions.calculateNextOffset[ReferenceData](
                  boundsWithoutContent,
                  model.layout
                )(reference, acc)

              acc :+ next.copy(offset = nextOffset)

            case _ =>
              acc :+ next
        }

      // Now we can calculate the content bounds
      val contentBounds: Bounds =
        withOffsets.foldLeft(Bounds.zero) { (acc, c) =>
          val bounds = c.component.bounds(reference, c.model).moveTo(c.offset)
          acc.expandToInclude(bounds)
        }

      // We can now calculate the boundsWithoutContent updating in the FitMode.Content cases and leaving as-is in others
      val updatedBounds =
        model.boundsType match
          case BoundsMode(FitMode.Content, FitMode.Content) =>
            contentBounds.dimensions

          case BoundsMode(FitMode.Content, _) =>
            boundsWithoutContent.withWidth(contentBounds.width)

          case BoundsMode(_, FitMode.Content) =>
            boundsWithoutContent.withHeight(contentBounds.height)

          case _ =>
            boundsWithoutContent

      // Finally, we can apply the anchors to the components that are not set to Anchor.None based on the updatedBounds
      val withAnchors =
        withOffsets.map { c =>
          c.anchor match
            case None =>
              c

            case Some(a) =>
              val componentBounds = c.component.bounds(reference, c.model)
              val offset          = a.calculatePosition(updatedBounds, componentBounds.dimensions)

              c.copy(offset = offset)
        }

      // Return the updated model with the new bounds and content bounds and dirty flag reset
      model.copy(
        dirty = false,
        contentBounds = contentBounds,
        dimensions = updatedBounds,
        components = withAnchors
      )
