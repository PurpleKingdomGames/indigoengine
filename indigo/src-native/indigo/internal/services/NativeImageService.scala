package indigo.internal.services

import indigo.platform.assets.TempImageData
import indigo.platform.imaging.BlitInstruction
import indigo.platform.imaging.ImageService
// import org.scalajs.dom
// import org.scalajs.dom.CanvasRenderingContext2D
// import org.scalajs.dom.ImageData
// import org.scalajs.dom.html

object NativeImageService:

  def apply(): ImageService[TempImageData, Array[Byte]] =
    new ImageService[TempImageData, Array[Byte]]:
      def composeImage(width: Int, height: Int, blits: Seq[BlitInstruction[TempImageData]]): Array[Byte] =
        // There are no assets yet, so should never be called for this PoC.
        Array.empty[Byte]
//         val canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
//         canvas.width = width
//         canvas.height = height

//         val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

//         blits.foreach: b =>
//           ctx.drawImage(b.source, b.x.toDouble, b.y.toDouble, b.width.toDouble, b.height.toDouble)

//         ctx.getImageData(0, 0, width, height).asInstanceOf[ImageData]
