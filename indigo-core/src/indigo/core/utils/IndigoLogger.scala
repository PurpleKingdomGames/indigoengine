package indigo.core.utils

import indigoengine.shared.logging.LogEntry
import indigoengine.shared.logging.LogLevel

import scala.collection.mutable
import scala.collection.mutable.Queue

/** A very, very simple logger.
  *
  * Calls are pushed into an internal buffer. The Indigo extension drains the buffer once per frame and routes each
  * entry through Tyrian's logging Cmd.
  */
object IndigoLogger:

  private val ORIGIN: String = "Indigo"

  private val onceLogs: mutable.HashSet[String] = mutable.HashSet.empty

  private val buffer: Queue[(LogLevel, String)] = Queue.empty[(LogLevel, String)]

  private def push(level: LogLevel, message: String): Unit =
    buffer.synchronized {
      buffer.enqueue((level, LogEntry.format(level, ORIGIN, message)))
      ()
    }

  private def pushOnce(level: LogLevel, message: String): Unit =
    buffer.synchronized {
      if onceLogs.add(LogEntry.format(level, ORIGIN, message)) then push(level, message)
    }

  private[indigo] def drainAll(): List[(LogLevel, String)] =
    buffer.synchronized {
      val out = buffer.toList
      buffer.clear()
      out
    }

  /** Immediately prints the given entry, bypassing the buffer. For use when there will be no chance to drain the
    * buffer, e.g. during a crash.
    */
  private[indigo] def unsafeLogNow(entry: LogEntry): Unit =
    val formatted = entry.format(ORIGIN)
    val shouldLog =
      if entry.isOnce then buffer.synchronized(onceLogs.add(formatted))
      else true

    if shouldLog then println(formatted)

  /** Log the given entry. */
  def log(entry: LogEntry): Unit =
    entry match
      case LogEntry.Log(level, message)     => push(level, message)
      case LogEntry.LogOnce(level, message) => pushOnce(level, message)

  /** Log at the given level. */
  def log(level: LogLevel, messages: String*): Unit =
    push(level, messages.toList.mkString(", "))

  /** Log at the given level, but only log each message once. */
  def logOnce(level: LogLevel, messages: String*): Unit =
    pushOnce(level, messages.toList.mkString(", "))

  def consoleLog(messages: String*): Unit =
    log(LogLevel.StdOut, messages*)

  def consoleLogOnce(messages: String*): Unit =
    logOnce(LogLevel.StdOut, messages*)

  def info(messages: String*): Unit =
    log(LogLevel.Info, messages*)

  def infoOnce(messages: String*): Unit =
    logOnce(LogLevel.Info, messages*)

  def warn(messages: String*): Unit =
    log(LogLevel.Warn, messages*)

  def warnOnce(messages: String*): Unit =
    logOnce(LogLevel.Warn, messages*)

  def error(messages: String*): Unit =
    log(LogLevel.Error, messages*)

  def errorOnce(messages: String*): Unit =
    logOnce(LogLevel.Error, messages*)

  def debug(messages: String*): Unit =
    log(LogLevel.Debug, messages*)

  def debugOnce(messages: String*): Unit =
    logOnce(LogLevel.Debug, messages*)
