package indigo.platform.events

import indigo.core.events.GlobalEvent
import indigo.render.EmitGlobalEvent
import indigoengine.shared.collections.Batch

trait GlobalEventStream extends EmitGlobalEvent:

  def kill(): Unit
  def pushGlobalEvent: GlobalEvent => Unit
  def collect: Batch[GlobalEvent]
