package indigoengine.shared.logging

class LogEntryTests extends munit.FunSuite {

  test("StdOut messages are not decorated") {
    assertEquals(LogEntry.Log(LogLevel.StdOut, "hello").format("Test"), "hello")
    assertEquals(LogEntry.LogOnce(LogLevel.StdOut, "hello").format("Test"), "hello")
  }

  test("Levelled messages are decorated with level and origin") {
    assertEquals(LogEntry.Log(LogLevel.Info, "hello").format("Test"), "[INFO] [Test] hello")
    assertEquals(LogEntry.Log(LogLevel.Warn, "hello").format("Test"), "[WARN] [Test] hello")
    assertEquals(LogEntry.Log(LogLevel.Error, "hello").format("Test"), "[ERROR] [Test] hello")
    assertEquals(LogEntry.LogOnce(LogLevel.Debug, "hello").format("Test"), "[DEBUG] [Test] hello")
  }

  test("Entries expose level, message, and once-ness") {
    val a = LogEntry.Log(LogLevel.Warn, "a")
    val b = LogEntry.LogOnce(LogLevel.Debug, "b")

    assertEquals(a.level, LogLevel.Warn)
    assertEquals(a.message, "a")
    assert(!a.isOnce)

    assertEquals(b.level, LogLevel.Debug)
    assertEquals(b.message, "b")
    assert(b.isOnce)
  }

  test("Helpers build entries at the expected level") {
    assertEquals(LogEntry.stdOut("a"), LogEntry.Log(LogLevel.StdOut, "a"))
    assertEquals(LogEntry.info("a"), LogEntry.Log(LogLevel.Info, "a"))
    assertEquals(LogEntry.warn("a"), LogEntry.Log(LogLevel.Warn, "a"))
    assertEquals(LogEntry.error("a"), LogEntry.Log(LogLevel.Error, "a"))
    assertEquals(LogEntry.debug("a"), LogEntry.Log(LogLevel.Debug, "a"))
  }

}
