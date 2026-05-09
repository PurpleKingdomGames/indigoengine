package indigo.platform.imaging

import org.scalajs.dom.ImageData
import org.scalajs.dom.html

/** Platform-managed image composition service. Indigo's atlas builder uses this to copy source images into a single
  * larger texture buffer; the service hides the underlying canvas (or other platform mechanism) used to perform the
  * pixel copy.
  */
trait ImageService:

  def composeImage(width: Int, height: Int, blits: Seq[BlitInstruction]): ImageData

final case class BlitInstruction(
    source: html.Image,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) derives CanEqual
