package indigo.platform.assets

import indigo.core.datatypes.PowerOfTwo

final case class Atlas(
    size: PowerOfTwo,
    // Yuk. Only optional so that testing is bearable.
    imageData: Option[Array[Byte]]
) derives CanEqual
