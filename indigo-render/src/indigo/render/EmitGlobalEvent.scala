package indigo.render

import indigo.core.events.GlobalEvent

trait EmitGlobalEvent:
  def pushGlobalEvent: GlobalEvent => Unit
