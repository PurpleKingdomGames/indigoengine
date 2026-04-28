package indigo.platform.events

import indigo.core.config.ResizePolicy
import indigo.core.datatypes.Size
import org.scalajs.dom
import org.scalajs.dom.html

import scala.annotation.nowarn

final class WorldEvents:

  final case class Handlers(
      canvas: html.Canvas,
      resizePolicy: ResizePolicy,
      onContextMenu: Option[dom.MouseEvent => Unit],
      resizeObserver: dom.ResizeObserver,
      clickTimeMs: Long
  ) {
    onContextMenu.foreach(canvas.addEventListener("contextmenu", _))
    resizeObserver.observe(canvas.parentElement)

    def unbind(): Unit = {
      onContextMenu.foreach(canvas.removeEventListener("contextmenu", _))
      resizeObserver.disconnect()
    }
  }

  object Handlers {
    @nowarn("msg=unused")
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
      // Prevent right mouse button from popping up the context menu
      onContextMenu = if disableContextMenu then Some((e: dom.MouseEvent) => e.preventDefault()) else None,
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

end WorldEvents
