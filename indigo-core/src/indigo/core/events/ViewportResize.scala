package indigo.core.events

import indigo.core.datatypes.Size

/** Fired whenever the game window changes size, so that the view can respond.
  *
  * @param gameViewPort
  *   The actual size in pixels, you can ask it to apply magnification.
  */
final case class ViewportResize(newSize: Size) extends GlobalEvent
