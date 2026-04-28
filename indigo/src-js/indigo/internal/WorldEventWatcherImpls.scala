package indigo.internal

import indigo.Indigo
import indigo.core.constants.Key
import indigo.core.constants.KeyCode
import indigo.core.constants.KeyLocation
import indigo.core.events.*
import org.scalajs.dom
import org.scalajs.dom.html
import tyrian.*

import scala.annotation.nowarn
import scala.scalajs.js.Date

import DomEventSyntax.*

final class WorldEventWatcherImpls(canvas: html.Canvas):

  // TODO: Consider tracking pointer button state in the ExtensionModel
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var pointerButtons: Map[Double, Batch[(Int, Date)]] = Map.empty

  def onPointerMove(e: dom.PointerEvent): Option[Indigo.Msg.WorldEvents] =
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

  def onPointerEnter(e: dom.PointerEvent): Option[Indigo.Msg.WorldEvents] =
    val position         = e.position(canvas)
    val buttons          = e.indigoButtons
    val movementPosition = e.movementPosition
    val pointerType      = e.toPointerType

    val enterEvent =
      PointerEvent.Enter(
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
          @nowarn("msg=deprecated")
          val mouseEnter =
            MouseEvent.Enter(
              PointerId(e.pointerId),
              position,
              buttons,
              e.altKey,
              e.ctrlKey,
              e.metaKey,
              e.shiftKey,
              movementPosition
            )

          Batch(mouseEnter)

        case PointerType.Touch =>
          Batch(
            TouchEvent.Enter(
              PointerId(e.pointerId),
              FingerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Pen =>
          Batch(
            PenEvent.Enter(
              PointerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Unknown =>
          Batch.empty

    Option(Indigo.Msg.WorldEvents(Batch(enterEvent) ++ events))

  def onPointerLeave(e: dom.PointerEvent): Option[Indigo.Msg.WorldEvents] =
    val position         = e.position(canvas)
    val buttons          = e.indigoButtons
    val movementPosition = e.movementPosition
    val pointerType      = e.toPointerType

    val leaveEvent =
      PointerEvent.Leave(
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

    @nowarn("msg=deprecated")
    val outEvent =
      PointerEvent.Out(
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
          @nowarn("msg=deprecated")
          val mouseLeave =
            MouseEvent.Leave(
              PointerId(e.pointerId),
              position,
              buttons,
              e.altKey,
              e.ctrlKey,
              e.metaKey,
              e.shiftKey,
              movementPosition
            )

          Batch(mouseLeave)

        case PointerType.Touch =>
          Batch(
            TouchEvent.Leave(
              PointerId(e.pointerId),
              FingerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Pen =>
          Batch(
            PenEvent.Leave(
              PointerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Unknown =>
          Batch.empty

    Option(Indigo.Msg.WorldEvents(Batch(leaveEvent, outEvent) ++ events))

  def onPointerDown(e: dom.PointerEvent): Option[Indigo.Msg.WorldEvents] =
    val position         = e.position(canvas)
    val pointerType      = e.toPointerType
    val buttons          = e.indigoButtons
    val movementPosition = e.movementPosition

    // A pen being touched to a touchpad, or a finger touching a screen both result in a left button being registered
    // This is misleading, and so here we reduce the button count by 1 to remove the left button. This also
    // has the result of making what was the middle button on a pen to a left button, and a right button to a middle buton
    val button = if pointerType == PointerType.Mouse then e.button else e.button - 1

    pointerButtons = pointerButtons.updated(
      e.pointerId,
      pointerButtons
        .getOrElse(e.pointerId, Batch.empty) :+ (button -> new Date(Date.now()))
    )

    val downEvent =
      PointerEvent.Down(
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
        e.isPrimary,
        MouseButton.fromOrdinalOpt(button)
      )

    val events =
      pointerType match
        case PointerType.Mouse =>
          MouseButton.fromOrdinalOpt(button) match
            case Some(btn) =>
              @nowarn("msg=deprecated")
              val mouseDown =
                MouseEvent.MouseDown(
                  PointerId(e.pointerId),
                  position,
                  buttons,
                  e.altKey,
                  e.ctrlKey,
                  e.metaKey,
                  e.shiftKey,
                  movementPosition,
                  btn
                )

              Batch(
                mouseDown,
                MouseEvent.Down(
                  PointerId(e.pointerId),
                  position,
                  movementPosition,
                  btn
                )
              )

            case None =>
              Batch.empty

        case PointerType.Touch =>
          Batch(
            TouchEvent.Down(
              PointerId(e.pointerId),
              FingerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Pen =>
          Batch(
            PenEvent.Down(
              PointerId(e.pointerId),
              position,
              movementPosition,
              e.pressure,
              MouseButton.fromOrdinalOpt(button)
            )
          )

        case PointerType.Unknown =>
          Batch.empty

    e.preventDefault()

    Option(Indigo.Msg.WorldEvents(Batch(downEvent) ++ events))

  def onPointerUp(clickTimeMs: Long)(e: dom.PointerEvent): Option[Indigo.Msg.WorldEvents] =
    val position         = e.position(canvas)
    val pointerType      = e.toPointerType
    val buttons          = e.indigoButtons
    val movementPosition = e.movementPosition

    val button = if pointerType == PointerType.Mouse then e.button else e.button - 1

    val clickEvents: Batch[GlobalEvent] =
      pointerButtons.getOrElse(e.pointerId, Batch.empty).find(_._1 == button) match
        case Some((_, downTime)) if Date.now() - downTime.getTime() <= clickTimeMs =>
          val btn = MouseButton.fromOrdinalOpt(button)
          val click =
            PointerEvent.Click(
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
              e.isPrimary,
              btn
            )

          val typed: Batch[GlobalEvent] =
            pointerType match
              case PointerType.Mouse if btn.isDefined =>
                Batch(
                  MouseEvent.Click(
                    PointerId(e.pointerId),
                    position,
                    buttons,
                    e.altKey,
                    e.ctrlKey,
                    e.metaKey,
                    e.shiftKey,
                    movementPosition,
                    btn.get
                  )
                )

              case PointerType.Touch =>
                Batch(
                  TouchEvent.Tap(
                    PointerId(e.pointerId),
                    FingerId(e.pointerId.toInt),
                    position,
                    movementPosition,
                    e.pressure
                  )
                )

              case PointerType.Pen =>
                Batch(
                  PenEvent.Click(
                    PointerId(e.pointerId),
                    position,
                    movementPosition,
                    e.pressure,
                    btn
                  )
                )

              case PointerType.Unknown | PointerType.Mouse =>
                Batch.empty

          Batch(click) ++ typed

        case _ =>
          Batch.empty

    pointerButtons = pointerButtons.updated(
      e.pointerId,
      pointerButtons
        .getOrElse(e.pointerId, Batch.empty)
        .filterNot(_._1 == button)
    )

    val upEvent =
      PointerEvent.Up(
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
        e.isPrimary,
        MouseButton.fromOrdinalOpt(button)
      )

    val typedUpEvents: Batch[GlobalEvent] =
      pointerType match
        case PointerType.Mouse =>
          MouseButton.fromOrdinalOpt(e.button) match
            case Some(btn) =>
              @nowarn("msg=deprecated")
              val mouseUp =
                MouseEvent.MouseUp(
                  PointerId(e.pointerId),
                  position,
                  buttons,
                  e.altKey,
                  e.ctrlKey,
                  e.metaKey,
                  e.shiftKey,
                  movementPosition,
                  btn
                )

              Batch(
                mouseUp,
                MouseEvent.Up(
                  PointerId(e.pointerId),
                  position,
                  movementPosition,
                  btn
                )
              )

            case None =>
              Batch.empty

        case PointerType.Touch =>
          Batch(
            TouchEvent.Up(
              PointerId(e.pointerId),
              FingerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Pen =>
          Batch(
            PenEvent.Up(
              PointerId(e.pointerId),
              position,
              movementPosition,
              e.pressure,
              MouseButton.fromOrdinalOpt(button)
            )
          )

        case PointerType.Unknown =>
          Batch.empty

    e.preventDefault()

    Option(Indigo.Msg.WorldEvents(clickEvents ++ Batch(upEvent) ++ typedUpEvents))

  def onPointerCancel(e: dom.PointerEvent): Option[Indigo.Msg.WorldEvents] =
    val position         = e.position(canvas)
    val buttons          = e.indigoButtons
    val movementPosition = e.movementPosition
    val pointerType      = e.toPointerType

    val cancelEvent =
      PointerEvent.Cancel(
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
            MouseEvent.Cancel(
              PointerId(e.pointerId),
              position,
              movementPosition
            )
          )

        case PointerType.Touch =>
          Batch(
            TouchEvent.Cancel(
              PointerId(e.pointerId),
              FingerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Pen =>
          Batch(
            PenEvent.Cancel(
              PointerId(e.pointerId),
              position,
              movementPosition,
              e.pressure
            )
          )

        case PointerType.Unknown =>
          Batch.empty

    e.preventDefault()

    Option(Indigo.Msg.WorldEvents(Batch(cancelEvent) ++ events))

  def onKeyDown(e: dom.KeyboardEvent): Option[Indigo.Msg.WorldEvents] =
    Option(
      Indigo.Msg.WorldEvents(
        Batch(
          KeyboardEvent.KeyDown(
            Key(
              KeyCode.fromString(e.code),
              e.key,
              KeyLocation.fromInt(e.location)
            ),
            e.repeat,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey
          )
        )
      )
    )

  def onKeyUp(e: dom.KeyboardEvent): Option[Indigo.Msg.WorldEvents] =
    Option(
      Indigo.Msg.WorldEvents(
        Batch(
          KeyboardEvent.KeyUp(
            Key(
              KeyCode.fromString(e.code),
              e.key,
              KeyLocation.fromInt(e.location)
            ),
            e.repeat,
            e.altKey,
            e.ctrlKey,
            e.metaKey,
            e.shiftKey
          )
        )
      )
    )

  def onWheel(e: dom.WheelEvent): Option[Indigo.Msg.WorldEvents] =
    val position         = e.position(canvas)
    val buttons          = e.indigoButtons
    val movementPosition = e.movementPosition

    @nowarn("msg=deprecated")
    val wheel =
      MouseEvent.Wheel(
        position,
        buttons,
        e.altKey,
        e.ctrlKey,
        e.metaKey,
        e.shiftKey,
        movementPosition,
        e.deltaX,
        e.deltaY,
        e.deltaZ
      )

    val deltaMode =
      e.deltaMode match
        case dom.WheelEvent.DOM_DELTA_PIXEL => WheelEvent.DeltaMode.Pixel
        case dom.WheelEvent.DOM_DELTA_LINE  => WheelEvent.DeltaMode.Line
        case dom.WheelEvent.DOM_DELTA_PAGE  => WheelEvent.DeltaMode.Page
        case _                              => WheelEvent.DeltaMode.Page

    val move =
      WheelEvent.Move(e.deltaX, e.deltaY, e.deltaZ, deltaMode)

    val axisEvents: Batch[GlobalEvent] =
      val xs = if e.deltaX != 0 then Batch(WheelEvent.Horizontal(e.deltaX, deltaMode)) else Batch.empty
      val ys = if e.deltaY != 0 then Batch(WheelEvent.Vertical(e.deltaY, deltaMode)) else Batch.empty
      val zs = if e.deltaZ != 0 then Batch(WheelEvent.Depth(e.deltaZ, deltaMode)) else Batch.empty
      xs ++ ys ++ zs

    Option(Indigo.Msg.WorldEvents(Batch(wheel, move) ++ axisEvents))

  @nowarn("msg=unused")
  def onCanvasFocus(e: dom.FocusEvent): Option[Indigo.Msg.WorldEvents] =
    Option(Indigo.Msg.WorldEvents(Batch(CanvasGainedFocus)))

  @nowarn("msg=unused")
  def onCanvasBlur(e: dom.FocusEvent): Option[Indigo.Msg.WorldEvents] =
    Option(Indigo.Msg.WorldEvents(Batch(CanvasLostFocus)))

  @nowarn("msg=unused")
  def onWindowFocus(e: dom.FocusEvent): Option[Indigo.Msg.WorldEvents] =
    Option(Indigo.Msg.WorldEvents(Batch(ApplicationGainedFocus)))

  @nowarn("msg=unused")
  def onWindowBlur(e: dom.FocusEvent): Option[Indigo.Msg.WorldEvents] =
    Option(Indigo.Msg.WorldEvents(Batch(ApplicationLostFocus)))
