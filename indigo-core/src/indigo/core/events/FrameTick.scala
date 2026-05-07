package indigo.core.events

/** A special event that happens once per frame, at the end of the frame. Useful for updating anything in your model
  * that "just happens" on every frame without any other prompting event. Like gravity.
  */
case object FrameTick extends GlobalEvent
