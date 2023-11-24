package roguelikestarterkit.ui.component

import indigo.*
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

import scala.annotation.tailrec

final case class ComponentGroup(
    bounds: Bounds,
    layout: ComponentLayout,
    components: Batch[ComponentEntry[_]]
):

  extension (b: Bounds)
    def withPadding(p: Padding): Bounds =
      b.moveBy(p.left, p.top).resize(b.width + p.right, b.height + p.bottom)

  def add[A](entry: A)(using c: Component[A]): ComponentGroup =
    val offset: Coords =
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
              else Coords(0, maxY)
            }
            .getOrElse(Coords(padding.left, padding.top))

        case ComponentLayout.Vertical(padding) =>
          components
            .takeRight(1)
            .headOption
            .map(c => c.offset + Coords(0, c.component.bounds(c.model).withPadding(padding).bottom))
            .getOrElse(Coords(padding.left, padding.top))

    this.copy(components = components :+ ComponentEntry(offset, entry, c))

  def add[A](entries: Batch[A])(using c: Component[A]): ComponentGroup =
    entries.foldLeft(this) { case (acc, next) => acc.add(next) }
  def add[A](entries: A*)(using c: Component[A]): ComponentGroup =
    add(Batch.fromSeq(entries))

  def update[StartupData, ContextData](
      context: UiContext[StartupData, ContextData]
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

  def present[StartupData, ContextData](
      context: UiContext[StartupData, ContextData]
  ): Outcome[ComponentFragment] =
    components
      .map { c =>
        c.component.present(context.copy(bounds = context.bounds.moveBy(c.offset)), c.model)
      }
      .sequence
      .map(_.foldLeft(ComponentFragment.empty)(_ |+| _))

  def withBounds(value: Bounds): ComponentGroup =
    this.copy(bounds = value)

  def withLayout(value: ComponentLayout): ComponentGroup =
    this.copy(layout = value)

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
    ComponentGroup(bounds, ComponentLayout.None, Batch.empty)

  given Component[ComponentGroup] with

    def bounds(model: ComponentGroup): Bounds =
      model.bounds

    def updateModel[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: ComponentGroup
    ): GlobalEvent => Outcome[ComponentGroup] =
      case e => model.update(context)(e)

    def present[StartupData, ContextData](
        context: UiContext[StartupData, ContextData],
        model: ComponentGroup
    ): Outcome[ComponentFragment] =
      model.present(context)
