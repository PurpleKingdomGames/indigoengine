package tyrian.classic.cmds

import cats.effect.kernel.Sync
import indigoengine.shared.logging.LogEntry
import indigoengine.shared.logging.LogLevel
import tyrian.platform.Cmd

import scala.collection.mutable

/** A very, very simple logger that logs to stdout with a few standard headers and the log message.
  */
object Logger:

  private val ORIGIN: String = "Tyrian"

  private val onceLogs: mutable.HashSet[String] = mutable.HashSet.empty

  private def logString(level: LogLevel, message: String): Unit =
    println(LogEntry.format(level, ORIGIN, message))

  private def logOnceString(level: LogLevel, message: String): Unit =
    val formatted = LogEntry.format(level, ORIGIN, message)
    val isNew     = onceLogs.synchronized(onceLogs.add(formatted))
    if isNew then println(formatted)

  /** Log at the given level. */
  def log[F[_]: Sync](level: LogLevel, messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      logString(level, messages.toList.mkString(", "))
    }

  /** Log at the given level, but only log each message once. */
  def logOnce[F[_]: Sync](level: LogLevel, messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      logOnceString(level, messages.toList.mkString(", "))
    }

  /** Immediately logs the given entry, for use when there will be no chance to run a Cmd, e.g. during a crash. */
  private[tyrian] def unsafeLogNow(entry: LogEntry): Unit =
    entry match
      case LogEntry.Log(level, message)     => logString(level, message)
      case LogEntry.LogOnce(level, message) => logOnceString(level, message)

  /** Log the given entry. */
  def log[F[_]: Sync](entry: LogEntry): Cmd.SideEffect[F] =
    entry match
      case LogEntry.Log(level, message)     => log(level, message)
      case LogEntry.LogOnce(level, message) => logOnce(level, message)

  /** stdout logs to the platforms standard output with no decoration or embellishments. */
  def stdout[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    log(LogLevel.StdOut, messages*)

  /** stdout logs to the platforms standard output with no decoration or embellishments, but only logs each message
    * once.
    */
  def stdoutOnce[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    logOnce(LogLevel.StdOut, messages*)

  /** Log at an info level */
  def info[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    log(LogLevel.Info, messages*)

  /** Log at an info level, but only log each message once. */
  def infoOnce[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    logOnce(LogLevel.Info, messages*)

  /** Log at a warn level */
  def warn[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    log(LogLevel.Warn, messages*)

  /** Log at a warn level, but only log each message once. */
  def warnOnce[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    logOnce(LogLevel.Warn, messages*)

  /** Log at an error level */
  def error[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    log(LogLevel.Error, messages*)

  /** Log at an error level, but only log each message once. */
  def errorOnce[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    logOnce(LogLevel.Error, messages*)

  /** Log at an debug level */
  def debug[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    log(LogLevel.Debug, messages*)

  /** Log at an debug level, but only log each message once. */
  def debugOnce[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    logOnce(LogLevel.Debug, messages*)
