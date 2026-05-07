package indigo.core.events

import indigo.core.assets.AssetName
import indigo.core.audio.PlaybackPolicy
import indigo.core.audio.Volume

/** Can be emitted to trigger the one time play back of a sound asset.
  *
  * @param assetName
  *   Reference to the loaded asset
  * @param volume
  *   What volume level to play at
  * @param policy
  *   How to handle the previous sounds
  */
final case class PlaySound(assetName: AssetName, volume: Volume, policy: PlaybackPolicy) extends GlobalEvent:
  def withVolume(newVolume: Volume): PlaySound =
    this.copy(volume = newVolume)
  def withPlaybackPolicy(newPolicy: PlaybackPolicy): PlaySound =
    this.copy(policy = newPolicy)

object PlaySound:

  def apply(assetName: AssetName): PlaySound =
    PlaySound(assetName, Volume.Max, PlaybackPolicy.Continue)

  def apply(assetName: AssetName, volume: Volume): PlaySound =
    PlaySound(assetName, volume, PlaybackPolicy.Continue)
