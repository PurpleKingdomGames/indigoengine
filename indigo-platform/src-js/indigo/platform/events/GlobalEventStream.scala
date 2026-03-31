package indigo.platform.events

import indigo.core.events.AssetEvent
import indigo.core.events.EnterFullScreen
import indigo.core.events.ExitFullScreen
import indigo.core.events.GlobalEvent
import indigo.core.events.PlaySound
import indigo.core.events.ToggleFullScreen
import indigo.platform.PlatformFullScreen
import indigo.platform.assets.AssetLoader
import indigo.platform.audio.AudioPlayer
import indigo.render.EmitGlobalEvent
import indigoengine.shared.collections.Batch

import scala.collection.mutable

final class GlobalEventStream(
    audioPlayer: AudioPlayer,
    platform: => PlatformFullScreen
) extends EmitGlobalEvent
    with GlobalEventCallback:

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var callback: Option[GlobalEvent => Unit] =
    None

  private val eventQueue: mutable.Queue[GlobalEvent] =
    new mutable.Queue[GlobalEvent]()

  def registerEventCallback(cb: GlobalEvent => Unit): Unit =
    callback = Some(cb)

  def clearEventCallback(): Unit =
    callback = None

  def kill(): Unit =
    eventQueue.clear()
    clearEventCallback()
    ()

  val pushGlobalEvent: GlobalEvent => Unit = {
    // Audio
    case PlaySound(assetName, volume, switch) =>
      audioPlayer.playSound(assetName, volume, switch)

    // Assets
    case AssetEvent.LoadAssetBatch(batch, key, makeAvailable) =>
      AssetLoader.backgroundLoadAssets(this, batch, key, makeAvailable)

    case AssetEvent.LoadAsset(asset, key, makeAvailable) =>
      AssetLoader.backgroundLoadAssets(this, Set(asset), key, makeAvailable)

    // Fullscreen
    case ToggleFullScreen =>
      platform.toggleFullScreen()

    case EnterFullScreen =>
      platform.enterFullScreen()

    case ExitFullScreen =>
      platform.exitFullScreen()

    // Default
    case e =>
      eventQueue.enqueue(e)
  }

  def collect: Batch[GlobalEvent] =
    val res = Batch.fromSeq(eventQueue.dequeueAll(_ => true))

    // If a callback listener is registered, give it all the dequeued events
    callback.foreach: cb =>
      res.foreach(cb)

    res
