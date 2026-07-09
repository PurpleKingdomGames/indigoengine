package indigoextras.ui.datatypes

import indigo.*

object PointerRouting:

  def isRoutedEvent(event: GlobalEvent): Boolean =
    event match
      case _: PointerEvent => true
      case _: WheelEvent   => true
      case _               => false

  def enterFrom(move: PointerEvent.Move): PointerEvent.Enter =
    PointerEvent.Enter(
      pointerId = move.pointerId,
      position = move.position,
      movementPosition = move.movementPosition,
      pointerType = move.pointerType
    )

  def leaveFrom(move: PointerEvent.Move): PointerEvent.Leave =
    PointerEvent.Leave(
      pointerId = move.pointerId,
      position = move.position,
      movementPosition = move.movementPosition,
      pointerType = move.pointerType
    )
