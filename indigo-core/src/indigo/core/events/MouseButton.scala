package indigo.core.events

/** Relies on the ordinal behavior of Scala 3 enums to match the button number
  */
enum MouseButton derives CanEqual:
  case LeftMouseButton, MiddleMouseButton, RightMouseButton, BrowserBackButton, BrowserForwardButton

object MouseButton:
  def fromOrdinalOpt(ordinal: Int): Option[MouseButton] =
    if ordinal >= LeftMouseButton.ordinal && ordinal <= BrowserForwardButton.ordinal then
      Some(MouseButton.fromOrdinal(ordinal))
    else Option.empty[MouseButton]
