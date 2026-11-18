package indigoengine.shared.logging

/** A message to be logged at a given level. `LogOnce` entries are only logged the first time a given message is seen.
  */
enum LogEntry(val level: LogLevel, val message: String, val isOnce: Boolean) derives CanEqual:
  case Log(l: LogLevel, m: String)     extends LogEntry(l, m, false)
  case LogOnce(l: LogLevel, m: String) extends LogEntry(l, m, true)

  /** Formats the message as `[LEVEL] [origin] message`, or just the message for `StdOut`. */
  def format(origin: String): String =
    LogEntry.format(level, origin, message)

object LogEntry:

  def stdOut(message: String): LogEntry = LogEntry.Log(LogLevel.StdOut, message)
  def info(message: String): LogEntry   = LogEntry.Log(LogLevel.Info, message)
  def warn(message: String): LogEntry   = LogEntry.Log(LogLevel.Warn, message)
  def error(message: String): LogEntry  = LogEntry.Log(LogLevel.Error, message)
  def debug(message: String): LogEntry  = LogEntry.Log(LogLevel.Debug, message)

  def format(level: LogLevel, origin: String, message: String): String =
    level match
      case LogLevel.StdOut => message
      case l               => s"[${l.label}] [$origin] $message"
