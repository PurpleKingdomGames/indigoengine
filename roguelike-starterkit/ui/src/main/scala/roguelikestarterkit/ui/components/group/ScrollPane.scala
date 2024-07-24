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
import roguelikestarterkit.ui.shaders.LayerMask

import java.lang.ref.Reference
import scala.annotation.tailrec

/** Describes a fixed arrangement of components, manages their layout, which may include anchored
  * components.
  */
final case class ScrollPane[A, ReferenceData] private[group] (
    boundsType: BoundsMode,
    component: ComponentEntry[A, ReferenceData],
    dimensions: Dimensions, // The actual cached dimensions of the scroll pane
    contentBounds: Bounds,   // The calculated and cached bounds of the content
    scrollAmount: Double
):

  def withComponent[B](component: B)(using
      c: Component[B, ReferenceData]
  ): ScrollPane[B, ReferenceData] =
    this.copy(
      component = ScrollPane.makeComponentEntry(component)
    )

  def withDimensions(value: Dimensions): ScrollPane[A, ReferenceData] =
    this.copy(dimensions = value)

  def withBoundsMode(value: BoundsMode): ScrollPane[A, ReferenceData] =
    this.copy(boundsType = value)

object ScrollPane:

  private def makeComponentEntry[A, ReferenceData](
      component: A
  )(using c: Component[A, ReferenceData]): ComponentEntry[A, ReferenceData] =
    ComponentEntry(
      ComponentId("scroll pane component"),
      Coords.zero,
      component,
      c,
      None
    )

  def apply[A, ReferenceData](
      component: A
  )(using c: Component[A, ReferenceData]): ScrollPane[A, ReferenceData] =
    ScrollPane(
      BoundsMode.default,
      ScrollPane.makeComponentEntry(component),
      Dimensions.zero,
      Bounds.zero,
      0.0
    )

  def apply[A, ReferenceData](boundsType: BoundsMode, component: A)(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    ScrollPane(
      boundsType,
      ScrollPane.makeComponentEntry(component),
      Dimensions.zero,
      Bounds.zero,
      0.0
    )

  def apply[A, ReferenceData](dimensions: Dimensions, component: A)(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    ScrollPane(
      BoundsMode.fixed(dimensions),
      ScrollPane.makeComponentEntry(component),
      dimensions,
      Bounds.zero,
      0.0
    )

  def apply[A, ReferenceData](width: Int, height: Int, component: A)(using
      c: Component[A, ReferenceData]
  ): ScrollPane[A, ReferenceData] =
    ScrollPane(Dimensions(width, height), component)

  given [A, ReferenceData]: Component[ScrollPane[A, ReferenceData], ReferenceData] with

    def bounds(reference: ReferenceData, model: ScrollPane[A, ReferenceData]): Bounds =
      Bounds(model.dimensions)

    def updateModel(
        context: UIContext[ReferenceData],
        model: ScrollPane[A, ReferenceData]
    ): GlobalEvent => Outcome[ScrollPane[A, ReferenceData]] =
      case FrameTick =>
        // Sub-groups will naturally refresh themselves as needed
        updateComponents(context, model)(FrameTick).map { updated =>
          refresh(context.reference, updated, context.bounds.dimensions)
        }

      case e =>
        updateComponents(context, model)(e)

    private def updateComponents[StartupData, ContextData](
        context: UIContext[ReferenceData],
        model: ScrollPane[A, ReferenceData]
    ): GlobalEvent => Outcome[ScrollPane[A, ReferenceData]] =
      e =>
        model.component.component
          .updateModel(context, model.component.model)(e)
          .map { updatedComponent =>
            model.copy(component = model.component.copy(model = updatedComponent))
          }

    def present(
        context: UIContext[ReferenceData],
        model: ScrollPane[A, ReferenceData]
    ): Outcome[Layer] =
      val scrollOffset: Coords =
        if model.contentBounds.height > model.dimensions.height then
          Coords(0, ((model.dimensions.height.toDouble - model.contentBounds.height.toDouble) * model.scrollAmount).toInt)
        else
          Coords.zero

      ContainerLikeFunctions
        .present(
          context.moveBoundsBy(scrollOffset),
          Batch(model.component)
        )
        .map {
          case l: Layer.Content =>
            l.withBlendMaterial(
              LayerMask(
                Bounds(
                  context.bounds.coords,
                  model.dimensions
                ).toScreenSpace(context.snapGrid * context.magnification)
              )
            )

          case l: Layer.Stack =>
            val masked =
              l.toBatch.map {
                _.withBlendMaterial(
                  LayerMask(
                    Bounds(
                      context.bounds.coords,
                      model.dimensions
                    ).toScreenSpace(context.snapGrid * context.magnification)
                  )
                )
              }

            l.copy(layers = masked)
        }
        .map { // TODO: Remove. Temporary, for debugging.
          case l: Layer.Content =>
            l.addNodes(
              Shape.Box(
                Rectangle(
                  context.bounds.coords.toScreenSpace(context.snapGrid),
                  model.dimensions.toScreenSpace(context.snapGrid)
                ),
                Fill.None,
                Stroke(4, RGBA.Magenta)
              )
            )

          case l: Layer.Stack =>
            l :+ Layer(
              Shape.Box(
                Rectangle(
                  context.bounds.coords.toScreenSpace(context.snapGrid),
                  model.dimensions.toScreenSpace(context.snapGrid)
                ),
                Fill.None,
                Stroke(1, RGBA.Magenta)
              )
            )
        }

    def refresh(
        reference: ReferenceData,
        model: ScrollPane[A, ReferenceData],
        parentDimensions: Dimensions
    ): ScrollPane[A, ReferenceData] =
      // Note: This is note _quite_ the same process as found in ComponentGroup

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

      // Next, call refresh on the component, and supplying the best guess for the bounds
      val updatedComponent =
        model.component.copy(
          model = model.component.component
            .refresh(reference, model.component.model, boundsWithoutContent)
        )

      // Now we can calculate the content bounds
      val contentBounds: Bounds =
        model.component.component.bounds(reference, updatedComponent.model)

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

      // Return the updated model with the new bounds and content bounds and dirty flag reset
      model.copy(
        contentBounds = contentBounds,
        dimensions = updatedBounds,
        component = updatedComponent
      )
