package indigo.internal

import indigo.Indigo
import indigo.core.events.*
import org.scalajs.dom
import org.scalajs.dom.html
import tyrian.*

import DomEventSyntax.*

object WorldEventWatcherImpls:

  def onPointerMove(canvas: html.Canvas)(e: dom.PointerEvent): Option[Indigo.Msg.WorldEvents] =
    val position         = e.position(canvas)
    val buttons          = e.indigoButtons
    val movementPosition = e.movementPosition
    val pointerType      = e.toPointerType

    val pointerMoveEvent =
      PointerEvent.Move(
        PointerId(e.pointerId),
        position,
        buttons,
        e.altKey,
        e.ctrlKey,
        e.metaKey,
        e.shiftKey,
        movementPosition,
        e.width.toInt,
        e.height.toInt,
        e.pressure,
        e.tangentialPressure,
        Radians.fromDegrees(Degrees(e.tiltX)),
        Radians.fromDegrees(Degrees(e.tiltY)),
        Radians.fromDegrees(Degrees(e.twist)),
        pointerType,
        e.isPrimary
      )

    val events =
      pointerType match
        case PointerType.Mouse =>
          Batch(
            MouseEvent.Move(
              PointerId(e.pointerId),
              position,
              buttons,
              e.altKey,
              e.ctrlKey,
              e.metaKey,
              e.shiftKey,
              movementPosition
            )
          )

        case PointerType.Touch =>
          Batch(
            TouchEvent.Move(
              PointerId(e.pointerId),
              FingerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Pen =>
          Batch(
            PenEvent.Move(
              PointerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Unknown =>
          Batch.empty

    e.preventDefault()

    Option(Indigo.Msg.WorldEvents(Batch(pointerMoveEvent) ++ events))
