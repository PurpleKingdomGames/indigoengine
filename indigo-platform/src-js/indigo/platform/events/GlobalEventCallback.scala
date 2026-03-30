package indigo.platform.events

import indigo.core.events.GlobalEvent

trait GlobalEventCallback:
  def registerEventCallback(cb: GlobalEvent => Unit): Unit

  def clearEventCallback(): Unit
