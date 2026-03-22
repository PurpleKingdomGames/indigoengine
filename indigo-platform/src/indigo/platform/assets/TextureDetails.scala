package indigo.platform.assets

import indigo.core.assets.AssetTag
import indigo.core.datatypes.PowerOfTwo

final case class TextureDetails(imageRef: ImageRef, size: PowerOfTwo, tag: Option[AssetTag]) derives CanEqual
