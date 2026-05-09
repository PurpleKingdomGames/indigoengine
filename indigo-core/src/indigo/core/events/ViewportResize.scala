package indigo.core.events

import indigo.core.datatypes.Size

/** Fired whenever the game window changes size, so that the view can respond.
  *
  * @param newSize
  *   The actual size in pixels
  */
final case class ViewportResize(newSize: Size) extends GlobalEvent
