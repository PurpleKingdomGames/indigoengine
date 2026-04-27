package indigo.platform.events

import indigo.core.config.ResizePolicy
import indigo.core.datatypes.Point
import indigo.core.datatypes.Size
import indigo.core.events.ApplicationGainedFocus
import indigo.core.events.ApplicationLostFocus
import indigo.core.events.CanvasGainedFocus
import indigo.core.events.CanvasLostFocus
import indigo.core.events.MouseButton
import indigo.core.events.MouseEvent
import indigo.core.events.WheelEvent
import indigoengine.shared.collections.Batch
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.window

import scala.annotation.nowarn

final class WorldEvents:

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

  final case class Handlers(
      canvas: html.Canvas,
      resizePolicy: ResizePolicy,
      onWheel: dom.WheelEvent => Unit,
      onContextMenu: Option[dom.MouseEvent => Unit],
      onBlur: dom.FocusEvent => Unit,
      onFocus: dom.FocusEvent => Unit,
      resizeObserver: dom.ResizeObserver,
      clickTimeMs: Long
  ) {
    canvas.addEventListener("wheel", onWheel)
    canvas.addEventListener("focus", onFocus)
    canvas.addEventListener("blur", onBlur)
    window.addEventListener("focus", onFocus)
    window.addEventListener("blur", onBlur)
    onContextMenu.foreach(canvas.addEventListener("contextmenu", _))
    resizeObserver.observe(canvas.parentElement)

    def unbind(): Unit = {
      canvas.removeEventListener("wheel", onWheel)
      canvas.removeEventListener("focus", onFocus)
      canvas.removeEventListener("blur", onBlur)
      window.removeEventListener("focus", onFocus)
      window.removeEventListener("blur", onBlur)
      onContextMenu.foreach(canvas.removeEventListener("contextmenu", _))
      resizeObserver.disconnect()
    }
  }

  object Handlers {
    def apply(
        canvas: html.Canvas,
        resizePolicy: ResizePolicy,
        magnification: Int,
        disableContextMenu: Boolean,
        globalEventStream: GlobalEventStream,
        clickTimeMs: Long
    ): Handlers = Handlers(
      canvas = canvas,
      resizePolicy,
      /*
          Follows the most conventional, basic definition of wheel.
          To be fair, the wheel event doesn't necessarily mean that the device is a mouse, or even that the
          deltaY represents the direction of the vertical scrolling (usually negative is upwards and positive downwards).
          For the sake of simplicity, we're assuming a common mouse with a simple wheel.

          More info: https://developer.mozilla.org/en-US/docs/Web/API/WheelEvent
       */
      onWheel = { e =>
        val position         = e.position(magnification, canvas)
        val buttons          = e.indigoButtons
        val movementPosition = e.movementPosition(magnification)

        @nowarn("msg=deprecated")
        val wheel = MouseEvent.Wheel(
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

        globalEventStream.pushGlobalEvent(wheel)

        val deltaMode =
          e.deltaMode match {
            case dom.WheelEvent.DOM_DELTA_PIXEL => WheelEvent.DeltaMode.Pixel
            case dom.WheelEvent.DOM_DELTA_LINE  => WheelEvent.DeltaMode.Line
            case dom.WheelEvent.DOM_DELTA_PAGE  => WheelEvent.DeltaMode.Page
            case _                              => WheelEvent.DeltaMode.Page
          }
        val newWheel = WheelEvent.Move(
          e.deltaX,
          e.deltaY,
          e.deltaZ,
          deltaMode
        )
        globalEventStream.pushGlobalEvent(newWheel)

        if (e.deltaX != 0)
          globalEventStream.pushGlobalEvent(WheelEvent.Horizontal(e.deltaX, deltaMode))

        if (e.deltaY != 0)
          globalEventStream.pushGlobalEvent(WheelEvent.Vertical(e.deltaY, deltaMode))

        if (e.deltaZ != 0)
          globalEventStream.pushGlobalEvent(WheelEvent.Depth(e.deltaZ, deltaMode))
      },
      // Prevent right mouse button from popping up the context menu
      onContextMenu = if disableContextMenu then Some((e: dom.MouseEvent) => e.preventDefault()) else None,
      onFocus = { e =>
        globalEventStream.pushGlobalEvent(
          if e.isWindowTarget then ApplicationGainedFocus
          else CanvasGainedFocus
        )
      },
      onBlur = { e =>
        globalEventStream.pushGlobalEvent(
          if e.isWindowTarget then ApplicationLostFocus
          else CanvasLostFocus
        )
      },
      resizeObserver = new dom.ResizeObserver((entries, _) =>
        entries.foreach { entry =>
          entry.target.childNodes.foreach { child =>
            child match {
              case child: dom.Element
                  if child.attributes.getNamedItem("id").value == canvas.attributes.getNamedItem("id").value =>
                val containerSize = new Size(
                  Math.floor(entry.contentRect.width).toInt,
                  Math.floor(entry.contentRect.height).toInt
                )
                val canvasSize = new Size(canvas.width, canvas.height)
                if resizePolicy != ResizePolicy.NoResize then
                  val newSize = resizePolicy match {
                    case ResizePolicy.Resize => containerSize
                    case ResizePolicy.ResizePreserveAspect =>
                      val width       = canvas.width.toDouble
                      val height      = canvas.height.toDouble
                      val aspectRatio = Math.min(width, height) / Math.max(width, height)

                      if width > height then
                        val newHeight = containerSize.width.toDouble * aspectRatio
                        if newHeight > containerSize.height then
                          Size(
                            (containerSize.height / aspectRatio).toInt,
                            containerSize.height
                          )
                        else
                          Size(
                            containerSize.width,
                            newHeight.toInt
                          )
                      else
                        val newWidth = containerSize.height.toDouble * aspectRatio
                        if newWidth > containerSize.width then
                          Size(
                            containerSize.width,
                            (containerSize.width / aspectRatio).toInt
                          )
                        else
                          Size(
                            newWidth.toInt,
                            containerSize.height
                          )
                    case _ => canvasSize
                  }

                  if (newSize != canvasSize) {
                    canvas.width = Math.min(newSize.width, containerSize.width)
                    canvas.height = Math.min(newSize.height, containerSize.height)
                  }
            }
          }
        }
      ),
      clickTimeMs
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _handlers: Option[Handlers] = None

  def init(
      canvas: html.Canvas,
      resizePolicy: ResizePolicy,
      magnification: Int,
      disableContextMenu: Boolean,
      globalEventStream: GlobalEventStream,
      clickTimeMs: Long
  ): Unit =
    if (_handlers.isEmpty)
      _handlers = Some(
        Handlers(canvas, resizePolicy, magnification, disableContextMenu, globalEventStream, clickTimeMs)
      )

  def kill(): Unit = _handlers.foreach { x =>
    x.unbind()
    _handlers = None
  }

  extension (e: dom.FocusEvent)
    def isWindowTarget: Boolean =
      val target = e.target
      target match {
        case e: dom.Element if e.tagName == "WINDOW" => true
        case _                                       => false
      }

  extension (e: dom.MouseEvent)
    /** @return
      *   position relative to magnification level
      */
    def position(magnification: Int, canvas: html.Canvas): Point =
      val rect = canvas.getBoundingClientRect()

      Point(
        absoluteCoordsX(e.pageX.toInt - rect.left.toInt) / magnification,
        absoluteCoordsY(e.pageY.toInt - rect.top.toInt) / magnification
      )

    def movementPosition(magnification: Int): Point =
      Point(
        (e.movementX / magnification).toInt,
        (e.movementY / magnification).toInt
      )

    /** The property indicates which buttons are pressed on the mouse (or other input device) when a mouse event is
      * triggered.
      */
    def indigoButtons =
      WorldEvents.buttonsFromInt(e.buttons)

end WorldEvents

object WorldEvents:

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
