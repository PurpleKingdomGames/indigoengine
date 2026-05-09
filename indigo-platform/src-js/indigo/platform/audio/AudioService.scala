package indigo.platform.audio

import indigo.platform.assets.LoadedAudioAsset
import indigo.scenegraph.SceneAudio

/** Platform-managed audio service. Indigo's GameLoop calls `playAudio` synchronously every frame to drive scene audio,
  * and `addAudioAssets` whenever assets are (re)loaded. One-shot SFX (`PlaySound`) flows through the regular event
  * stream, not this interface.
  */
trait AudioService:

  def playAudio(sceneAudioOption: Option[SceneAudio]): Unit

  def addAudioAssets(audioAssets: Set[LoadedAudioAsset]): Unit

  def kill(): Unit
