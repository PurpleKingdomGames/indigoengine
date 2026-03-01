package tyrian.classic.cmds

import cats.effect.kernel.Sync
import tyrian.platform.Cmd

import scala.collection.mutable.ArrayBuffer

/** A very, very simple logger that logs to stdout with a few standard headers and the log message.
  */
object Logger:

  private val INFO: String  = "INFO"
  private val ERROR: String = "ERROR"
  private val DEBUG: String = "DEBUG"

  private val errorLogs: ArrayBuffer[String] = new ArrayBuffer[String]()
  private val debugLogs: ArrayBuffer[String] = new ArrayBuffer[String]()

  private def formatMessage(level: String, message: String): String =
    s"""[$level] [Tyrian] $message"""

  private val stdOutString: String => Unit = message => println(message)

  private val infoString: String => Unit = message => println(formatMessage(INFO, message))

  private val errorString: String => Unit = message => println(formatMessage(ERROR, message))

  private val errorOnceString: String => Unit = message =>
    if !errorLogs.contains(message) then {
      errorLogs += message
      println(formatMessage(ERROR, message))
    }

  private val debugString: String => Unit = message => println(formatMessage(DEBUG, message))

  private val debugOnceString: String => Unit = message =>
    if !debugLogs.contains(message) then {
      debugLogs += message
      println(formatMessage(DEBUG, message))
    }

  /** stdout logs to the platforms standard output with no decoration or embellishments. */
  def stdout[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      stdOutString(messages.toList.mkString(", "))
    }

  /** Log at an info level */
  def info[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      infoString(messages.toList.mkString(", "))
    }

  /** Log at an error level */
  def error[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      errorString(messages.toList.mkString(", "))
    }

  /** Log at an error level, but only log each message once. */
  def errorOnce[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      errorOnceString(messages.toList.mkString(", "))
    }

  /** Log at an debug level */
  def debug[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      debugString(messages.toList.mkString(", "))
    }

  /** Log at an debug level, but only log each message once. */
  def debugOnce[F[_]: Sync](messages: String*): Cmd.SideEffect[F] =
    Cmd.SideEffect {
      debugOnceString(messages.toList.mkString(", "))
    }
