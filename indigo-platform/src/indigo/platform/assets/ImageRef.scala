package indigo.platform.assets

import indigo.core.assets.AssetName
import indigo.core.assets.AssetTag

// Input
final case class ImageRef(name: AssetName, width: Int, height: Int, tag: Option[AssetTag]) derives CanEqual
