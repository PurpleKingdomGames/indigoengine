package indigo.platform.events

import indigo.core.config.ResizePolicy
import indigo.core.datatypes.Size
import org.scalajs.dom
import org.scalajs.dom.html

final class WorldEvents:

  final case class Handlers(
      canvas: html.Canvas,
      resizePolicy: ResizePolicy,
      resizeObserver: dom.ResizeObserver
  ) {
    resizeObserver.observe(canvas.parentElement)

    def unbind(): Unit =
      resizeObserver.disconnect()
  }

  object Handlers {
    def apply(
        canvas: html.Canvas,
        resizePolicy: ResizePolicy
    ): Handlers = Handlers(
      canvas = canvas,
      resizePolicy,
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
      )
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _handlers: Option[Handlers] = None

  def init(
      canvas: html.Canvas,
      resizePolicy: ResizePolicy
  ): Unit =
    if (_handlers.isEmpty)
      _handlers = Some(
        Handlers(canvas, resizePolicy)
      )

  def kill(): Unit = _handlers.foreach { x =>
    x.unbind()
    _handlers = None
  }

end WorldEvents
