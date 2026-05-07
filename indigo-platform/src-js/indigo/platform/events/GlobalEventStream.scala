package indigo.platform.events

import indigo.core.events.GlobalEvent
import indigo.core.events.PlaySound
import indigo.platform.audio.AudioPlayer
import indigo.render.EmitGlobalEvent
import indigoengine.shared.collections.Batch

import scala.collection.mutable

final class GlobalEventStream(
    audioPlayer: AudioPlayer
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
