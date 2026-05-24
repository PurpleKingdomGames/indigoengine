package indigo.core.utils

import scala.collection.mutable
import scala.collection.mutable.Queue

enum LogLevel derives CanEqual:
  case Info, Error, Debug, Raw

/** A very, very simple logger.
  *
  * Calls are pushed into an internal buffer. The Indigo extension drains the buffer once per frame and routes each
  * entry through Tyrian's logging Cmd.
  */
object IndigoLogger:

  private val INFO: String  = "INFO"
  private val ERROR: String = "ERROR"
  private val DEBUG: String = "DEBUG"

  private val errorLogs: mutable.HashSet[String] = mutable.HashSet.empty
  private val debugLogs: mutable.HashSet[String] = mutable.HashSet.empty

  private val buffer: Queue[(LogLevel, String)] = Queue.empty[(LogLevel, String)]

  private def push(level: LogLevel, message: String): Unit =
    buffer.synchronized {
      buffer.enqueue((level, message))
      ()
    }

  private[indigo] def drainAll(): List[(LogLevel, String)] =
    buffer.synchronized {
      val out = buffer.toList
      buffer.clear()
      out
    }

  private def formatMessage(level: String, message: String): String =
    s"""[$level] [Indigo] $message"""

  def consoleLog(messages: String*): Unit =
    push(LogLevel.Raw, messages.toList.mkString(", "))

  def info(messages: String*): Unit =
    push(LogLevel.Info, formatMessage(INFO, messages.toList.mkString(", ")))

  def error(messages: String*): Unit =
    push(LogLevel.Error, formatMessage(ERROR, messages.toList.mkString(", ")))

  def errorOnce(messages: String*): Unit =
    val msg = messages.toList.mkString(", ")
    buffer.synchronized {
      if errorLogs.add(msg) then push(LogLevel.Error, formatMessage(ERROR, msg))
    }

  def debug(messages: String*): Unit =
    push(LogLevel.Debug, formatMessage(DEBUG, messages.toList.mkString(", ")))

  def debugOnce(messages: String*): Unit =
    val msg = messages.toList.mkString(", ")
    buffer.synchronized {
      if debugLogs.add(msg) then push(LogLevel.Debug, formatMessage(DEBUG, msg))
    }
