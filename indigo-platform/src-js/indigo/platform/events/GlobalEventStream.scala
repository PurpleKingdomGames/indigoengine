package indigo.platform.events

import indigo.core.events.GlobalEvent
import indigo.render.EmitGlobalEvent
import indigoengine.shared.collections.Batch

import scala.collection.mutable

final class GlobalEventStream extends EmitGlobalEvent with GlobalEventCallback:

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

  def pushGlobalEvent(e: GlobalEvent): Unit =
    eventQueue.enqueue(e)

  def collect: Batch[GlobalEvent] =
    val res = Batch.fromSeq(eventQueue.dequeueAll(_ => true))

    callback.foreach: cb =>
      res.foreach(cb)

    res
