package roguelikestarterkit.ui.components.group

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import roguelikestarterkit.ComponentFragment
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.UiContext

import java.lang.ref.Reference

object GroupFunctions:

  extension (b: Bounds)
    private def withPadding(p: Padding): Bounds =
      b.moveBy(p.left, p.top).resize(b.width + p.right, b.height + p.bottom)

  def calculateNextOffset[ReferenceData](reference: ReferenceData, bounds: Bounds, layout: ComponentLayout)(
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): Coords =
    layout match
      case ComponentLayout.None =>
        Coords.zero

      case ComponentLayout.Horizontal(padding, Overflow.Hidden) =>
        components
          .takeRight(1)
          .headOption
          .map(c => c.offset + Coords(c.component.bounds(reference, c.model).withPadding(padding).right, 0))
          .getOrElse(Coords(padding.left, padding.top))

      case ComponentLayout.Horizontal(padding, Overflow.Wrap) =>
        val maxY = components
          .map(c => c.offset.y + c.component.bounds(reference, c.model).withPadding(padding).height)
          .sortWith(_ > _)
          .headOption
          .getOrElse(0)

        components
          .takeRight(1)
          .headOption
          .map { c =>
            val padded      = c.component.bounds(reference, c.model).withPadding(padding)
            val maybeOffset = c.offset + Coords(padded.right, 0)

            if padded.moveBy(maybeOffset).right < bounds.width then maybeOffset
            else Coords(padding.left, maxY)
          }
          .getOrElse(Coords(padding.left, padding.top))

      case ComponentLayout.Vertical(padding) =>
        components
          .takeRight(1)
          .headOption
          .map(c => c.offset + Coords(0, c.component.bounds(reference, c.model).withPadding(padding).bottom))
          .getOrElse(Coords(padding.left, padding.top))

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

  def present[ReferenceData](
      context: UiContext[ReferenceData],
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): Outcome[ComponentFragment] =
    components
      .map { c =>
        c.component.present(context.copy(bounds = context.bounds.moveBy(c.offset)), c.model)
      }
      .sequence
      .map(_.foldLeft(ComponentFragment.empty)(_ |+| _))
