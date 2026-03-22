package indigo.platform.assets

import indigo.core.datatypes.PowerOfTwo
import org.scalajs.dom.ImageData

final case class Atlas(
    size: PowerOfTwo,
    // Yuk. Only optional so that testing is bearable.
    imageData: Option[ImageData]
) derives CanEqual
