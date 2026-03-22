package indigo.platform.assets

import indigo.core.assets.AssetName
import org.scalajs.dom

final case class LoadedAudioAsset(val name: AssetName, val data: dom.AudioBuffer)
