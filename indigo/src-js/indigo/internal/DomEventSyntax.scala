package indigo.internal

import indigo.core.datatypes.Point
import indigo.core.events.*
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.window
import tyrian.*

object DomEventSyntax:

  extension (e: dom.PointerEvent)

    def toPointerType =
      e.pointerType match {
        case "mouse" => PointerType.Mouse
        case "pen"   => PointerType.Pen
        case "touch" => PointerType.Touch
        case _       => PointerType.Unknown
      }

    def position(canvas: html.Canvas): Point =
      val rect = canvas.getBoundingClientRect()

      Point(
        absoluteCoordsX(e.pageX.toInt - rect.left.toInt),
        absoluteCoordsY(e.pageY.toInt - rect.top.toInt)
      )

    def movementPosition: Point =
      Point(
        e.movementX.toInt,
        e.movementY.toInt
      )

    /** The property indicates which buttons are pressed on the mouse (or other input device) when a mouse event is
      * triggered.
      */
    def indigoButtons =
      buttonsFromInt(e.buttons)

  /** Work out which buttons are pressed on the mouse (or other input device) based on the `buttons` field from a
    * `dom.MouseEvent`.
    */
  def buttonsFromInt(buttons: Int): Batch[MouseButton] =
    Batch.fromArray(
      (0 to 5)
        .filter(i => ((buttons >> i) & 1) == 1)
        .flatMap(MouseButton.fromOrdinalOpt)
        .toArray
    )

  def absoluteCoordsX(relativeX: Double): Int = {
    val offset: Double =
      if (window.pageXOffset > 0) window.pageXOffset
      else if (document.documentElement.scrollLeft > 0) document.documentElement.scrollLeft
      else if (document.body.scrollLeft > 0) document.body.scrollLeft
      else 0

    (relativeX - offset).toInt
  }

  def absoluteCoordsY(relativeY: Double): Int = {
    val offset: Double =
      if (window.pageYOffset > 0) window.pageYOffset
      else if (document.documentElement.scrollTop > 0) document.documentElement.scrollTop
      else if (document.body.scrollTop > 0) document.body.scrollTop
      else 0

    (relativeY - offset).toInt
  }
