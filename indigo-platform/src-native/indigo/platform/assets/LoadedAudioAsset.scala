package indigo.platform.assets

import indigo.core.assets.AssetName

final case class LoadedAudioAsset(val name: AssetName, val data: Array[Byte])
