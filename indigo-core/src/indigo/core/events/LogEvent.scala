package indigo.core.events

import indigoengine.shared.logging.LogEntry

/** Requests that the engine logs the given entry. Log events are consumed by the engine and are not delivered to
  * scenes, subsystems, or the game model.
  */
final case class LogEvent(entry: LogEntry) extends GlobalEvent
