package indigo.core.events

/** A trait that tells Indigo to allow this instance into the event loop for the duration of one frame.
  */
trait GlobalEvent

object GlobalEvent:
  given CanEqual[GlobalEvent, GlobalEvent] = CanEqual.derived
