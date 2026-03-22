package indigo.platform.assets

import indigo.core.assets.AssetName
import indigo.core.assets.AssetTag

final case class LoadedImageAsset(val name: AssetName, val data: TempImageData, val tag: Option[AssetTag])
