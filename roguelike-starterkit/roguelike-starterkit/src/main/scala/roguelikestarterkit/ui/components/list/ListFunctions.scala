package roguelikestarterkit.ui.components.list

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import roguelikestarterkit.ComponentFragment
import roguelikestarterkit.ui.components.group.BoundsType
import roguelikestarterkit.ui.components.group.ComponentLayout
import roguelikestarterkit.ui.components.group.FitMode
import roguelikestarterkit.ui.components.group.Overflow
import roguelikestarterkit.ui.components.group.Padding
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.UiContext

import java.lang.ref.Reference

object ListFunctions:

  extension (b: Bounds)
    private def withPadding(p: Padding): Bounds =
      b.moveBy(p.left, p.top).resize(b.width + p.right, b.height + p.bottom)

  def calculateNextOffset[ReferenceData](bounds: Bounds, layout: ComponentLayout)(
      context: ReferenceData,
      components: Batch[ComponentListEntry[?, ReferenceData]]
  ): Coords =
    layout match
      case ComponentLayout.None =>
        Coords.zero

      case ComponentLayout.Horizontal(padding, Overflow.Hidden) =>
        components
          .takeRight(1)
          .headOption
          .map(c =>
            c.offset + Coords(c.component.bounds(context, c.model).withPadding(padding).right, 0)
          )
          .getOrElse(Coords(padding.left, padding.top))

      case ComponentLayout.Horizontal(padding, Overflow.Wrap) =>
        val maxY = components
          .map(c => c.offset.y + c.component.bounds(context, c.model).withPadding(padding).height)
          .sortWith(_ > _)
          .headOption
          .getOrElse(0)

        components
          .takeRight(1)
          .headOption
          .map { c =>
            val padded      = c.component.bounds(context, c.model).withPadding(padding)
            val maybeOffset = c.offset + Coords(padded.right, 0)

            if padded.moveBy(maybeOffset).right < bounds.width then maybeOffset
            else Coords(padding.left, maxY)
          }
          .getOrElse(Coords(padding.left, padding.top))

      case ComponentLayout.Vertical(padding) =>
        components
          .takeRight(1)
          .headOption
          .map(c =>
            c.offset + Coords(0, c.component.bounds(context, c.model).withPadding(padding).bottom)
          )
          .getOrElse(Coords(padding.left, padding.top))

  def present[ReferenceData](
      context: UiContext[ReferenceData],
      components: Batch[ComponentListEntry[?, ReferenceData]]
  ): Outcome[ComponentFragment] =
    components
      .map { c =>
        c.component.present(context.copy(bounds = context.bounds.moveBy(c.offset)), c.model)
      }
      .sequence
      .map(_.foldLeft(ComponentFragment.empty)(_ |+| _))
