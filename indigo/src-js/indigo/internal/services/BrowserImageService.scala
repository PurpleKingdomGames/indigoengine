package indigo.internal.services

import indigo.platform.imaging.BlitInstruction
import indigo.platform.imaging.ImageService
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.ImageData
import org.scalajs.dom.html

object BrowserImageService:

  def apply(): ImageService =
    new ImageService:
      def composeImage(width: Int, height: Int, blits: Seq[BlitInstruction]): ImageData =
        val canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
        canvas.width = width
        canvas.height = height

        val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

        blits.foreach: b =>
          ctx.drawImage(b.source, b.x.toDouble, b.y.toDouble, b.width.toDouble, b.height.toDouble)

        ctx.getImageData(0, 0, width, height).asInstanceOf[ImageData]
