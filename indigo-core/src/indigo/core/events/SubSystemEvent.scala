package indigo.core.events

/** A trait whose presence signals that this event should only be routed to subsystems, not the main game.
  */
trait SubSystemEvent extends GlobalEvent
