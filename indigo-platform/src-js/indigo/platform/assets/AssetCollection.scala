package indigo.platform.assets

import indigo.core.assets.AssetName
import org.scalajs.dom
import org.scalajs.dom.html

final class AssetCollection(
    val images: Set[LoadedImageAsset],
    val texts: Set[LoadedTextAsset],
    val sounds: Set[LoadedAudioAsset]
) {

  val count: Int =
    images.size + texts.size + sounds.size

  def |+|(other: AssetCollection): AssetCollection =
    new AssetCollection(
      images ++ other.images,
      texts ++ other.texts,
      sounds ++ other.sounds
    )

  def exists(name: AssetName): Boolean =
    images.exists(_.name == name) ||
      texts.exists(_.name == name) ||
      sounds.exists(_.name == name)

  def findImageDataByName(name: AssetName): Option[html.Image] =
    images.find(_.name == name).map(_.data)

  def findTextDataByName(name: AssetName): Option[String] =
    texts.find(_.name == name).map(_.data)

  def findAudioDataByName(name: AssetName): Option[dom.AudioBuffer] =
    sounds.find(_.name == name).map(_.data)

}

object AssetCollection {
  def empty: AssetCollection =
    new AssetCollection(Set(), Set(), Set())
}
