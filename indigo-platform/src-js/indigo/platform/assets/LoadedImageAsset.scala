package indigo.platform.assets

import indigo.core.assets.AssetName
import indigo.core.assets.AssetTag
import org.scalajs.dom.html

final case class LoadedImageAsset(val name: AssetName, val data: html.Image, val tag: Option[AssetTag])
