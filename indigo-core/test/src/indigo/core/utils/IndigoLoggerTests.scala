package indigo.core.utils

import indigoengine.shared.logging.LogEntry
import indigoengine.shared.logging.LogLevel

class IndigoLoggerTests extends munit.FunSuite {

  test("Log entries are formatted and buffered") {
    IndigoLogger.drainAll(): Unit

    IndigoLogger.log(LogEntry.Log(LogLevel.StdOut, "plain"))
    IndigoLogger.log(LogEntry.Log(LogLevel.Warn, "careful"))
    IndigoLogger.info("a", "b")

    val expected =
      List(
        LogLevel.StdOut -> "plain",
        LogLevel.Warn   -> "[WARN] [Indigo] careful",
        LogLevel.Info   -> "[INFO] [Indigo] a, b"
      )

    assertEquals(IndigoLogger.drainAll(), expected)
    assertEquals(IndigoLogger.drainAll(), Nil)
  }

  test("Once entries are only logged the first time they are seen") {
    IndigoLogger.drainAll(): Unit

    IndigoLogger.log(LogEntry.LogOnce(LogLevel.Debug, "once-test"))
    IndigoLogger.log(LogEntry.LogOnce(LogLevel.Debug, "once-test"))
    IndigoLogger.debugOnce("once-test")
    IndigoLogger.log(LogEntry.LogOnce(LogLevel.Error, "once-test"))

    val expected =
      List(
        LogLevel.Debug -> "[DEBUG] [Indigo] once-test",
        LogLevel.Error -> "[ERROR] [Indigo] once-test"
      )

    assertEquals(IndigoLogger.drainAll(), expected)
  }

  test("unsafeLogNow bypasses the buffer") {
    IndigoLogger.drainAll(): Unit

    IndigoLogger.unsafeLogNow(LogEntry.Log(LogLevel.Info, "now"))

    assertEquals(IndigoLogger.drainAll(), Nil)
  }

}
