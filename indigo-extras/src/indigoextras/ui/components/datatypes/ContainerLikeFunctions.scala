package indigoextras.ui.components.datatypes

import indigo.*
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.PointerRouting
import indigoextras.ui.datatypes.UIContext

object ContainerLikeFunctions:

  extension (b: Bounds)
    def withPadding(p: Padding): Bounds =
      b.moveBy(p.left, p.top).resize(b.width + p.right, b.height + p.bottom)

  def calculateNextOffset[ReferenceData](containerDimensions: Dimensions, layout: ComponentLayout)(
      context: UIContext[ReferenceData],
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): Coords =
    layout match
      case ComponentLayout.Horizontal(padding, Overflow.Hidden) =>
        components
          .takeRight(1)
          .headOption
          .map(c => c.offset + Coords(c.component.bounds(context, c.model).withPadding(padding).right, 0))
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

            if padded.moveBy(maybeOffset).right < containerDimensions.width then maybeOffset
            else Coords(padding.left, maxY)
          }
          .getOrElse(Coords(padding.left, padding.top))

      case ComponentLayout.Vertical(padding) =>
        components
          .takeRight(1)
          .headOption
          .map(c => c.offset + Coords(0, c.component.bounds(context, c.model).withPadding(padding).bottom))
          .getOrElse(Coords(padding.left, padding.top))

  def present[ReferenceData](
      context: UIContext[ReferenceData],
      dimensions: Dimensions,
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): Outcome[Layer] =
    components
      .map { c =>
        c.component.present(
          context.withParentBounds(Bounds(context.parent.bounds.moveBy(c.offset).coords, dimensions)),
          c.model
        )
      }
      .sequence
      .map(_.foldLeft(Layer.Stack.empty)(_ :+ _))

  def hitTest[ReferenceData](
      context: UIContext[ReferenceData],
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): Boolean =
    components.exists { c =>
      c.component.hitTest(routedChildContext(context, c), c.model)
    }

  def hitTest[ReferenceData](
      context: UIContext[ReferenceData],
      components: Batch[ComponentEntry[?, ReferenceData]],
      event: GlobalEvent
  ): Boolean =
    components.exists { c =>
      c.component.hitTest(routedChildContext(context, c), c.model, event)
    }

  def routeOrBroadcast[ReferenceData](
      context: UIContext[ReferenceData],
      dimensions: Dimensions,
      components: Batch[ComponentEntry[?, ReferenceData]]
  ): GlobalEvent => Outcome[Batch[ComponentEntry[?, ReferenceData]]] =
    case event if PointerRouting.isRoutedEvent(event) =>
      route(context, components, event)

    case event =>
      components.map { c =>
        updateEntry(childContext(context, dimensions, c), c, Batch(event))
      }.sequence

  private def route[ReferenceData](
      context: UIContext[ReferenceData],
      components: Batch[ComponentEntry[?, ReferenceData]],
      event: GlobalEvent
  ): Outcome[Batch[ComponentEntry[?, ReferenceData]]] =
    val entries = components.toList

    val maybeTargetIndex =
      entries.zipWithIndex.reverse
        .find { case (entry, _) =>
          entry.component.hitTest(routedChildContext(context, entry), entry.model, event)
        }
        .map(_._2)

    Batch
      .fromSeq(entries.zipWithIndex)
      .map { case (entry, index) =>
        val events =
          routedEvents(event, maybeTargetIndex.contains(index))

        if events.isEmpty then Outcome(entry)
        else updateEntry(routedChildContext(context, entry), entry, events)
      }
      .sequence

  private def routedEvents(event: GlobalEvent, isTarget: Boolean): Batch[GlobalEvent] =
    event match
      case move: PointerEvent.Move if isTarget =>
        Batch(PointerRouting.enterFrom(move), move)

      case move: PointerEvent.Move =>
        Batch(PointerRouting.leaveFrom(move))

      case _: PointerEvent.Up | _: PointerEvent.Cancel | _: PointerEvent.Leave =>
        Batch(event)

      case _: PointerEvent if isTarget =>
        Batch(event)

      case _: WheelEvent if isTarget =>
        Batch(event)

      case _ =>
        Batch.empty

  private def updateEntry[A, ReferenceData](
      context: UIContext[ReferenceData],
      entry: ComponentEntry[A, ReferenceData],
      events: Batch[GlobalEvent]
  ): Outcome[ComponentEntry[A, ReferenceData]] =
    events
      .foldLeft(Outcome(entry.model)) { (acc, event) =>
        acc.flatMap(entry.component.updateModel(context, _)(event))
      }
      .map(updated => entry.copy(model = updated))

  private def childContext[ReferenceData](
      context: UIContext[ReferenceData],
      dimensions: Dimensions,
      component: ComponentEntry[?, ReferenceData]
  ): UIContext[ReferenceData] =
    context.withParentBounds(Bounds(context.parent.bounds.moveBy(component.offset).coords, dimensions))

  private def routedChildContext[ReferenceData](
      context: UIContext[ReferenceData],
      component: ComponentEntry[?, ReferenceData]
  ): UIContext[ReferenceData] =
    val bounds = component.component.bounds(context, component.model)

    context.withParentBounds(
      Bounds(
        context.parent.bounds.moveBy(component.offset).coords,
        bounds.dimensions
      )
    )
