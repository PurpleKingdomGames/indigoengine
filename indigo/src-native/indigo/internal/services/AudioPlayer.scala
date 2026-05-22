package indigo.internal.services

import indigo.platform.assets.LoadedAudioAsset
import indigo.platform.audio.AudioService
import indigo.scenegraph.SceneAudio

final class AudioPlayer() extends AudioService:

  def playAudio(sceneAudioOption: Option[SceneAudio]): Unit =
    ()

  def addAudioAssets(audioAssets: Set[LoadedAudioAsset]): Unit =
    ()

  def kill(): Unit =
    ()
