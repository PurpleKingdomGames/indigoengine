package indigo.core.events

import scala.annotation.nowarn

@deprecated("Use `WheelDirection` instead", "0.22.0")
enum MouseWheel derives CanEqual:
  @nowarn("msg=deprecated") case ScrollUp, ScrollDown
