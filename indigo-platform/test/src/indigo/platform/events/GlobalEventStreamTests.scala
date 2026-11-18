package indigo.platform.events

import indigo.core.events.GlobalEvent
import indigo.core.events.LogEvent
import indigo.core.utils.IndigoLogger
import indigoengine.shared.collections.Batch
import indigoengine.shared.logging.LogEntry
import indigoengine.shared.logging.LogLevel

class GlobalEventStreamTests extends munit.FunSuite {

  test("LogEvents are routed to the logger, not the event queue") {
    IndigoLogger.drainAll(): Unit

    val stream = new GlobalEventStream

    stream.pushGlobalEvent(StreamTestEvent("a"))
    stream.pushGlobalEvent(LogEvent(LogEntry.Log(LogLevel.Info, "hello")))
    stream.pushGlobalEvent(StreamTestEvent("b"))

    assertEquals(stream.collect, Batch[GlobalEvent](StreamTestEvent("a"), StreamTestEvent("b")))
    assertEquals(IndigoLogger.drainAll(), List(LogLevel.Info -> "[INFO] [Indigo] hello"))
  }

}

final case class StreamTestEvent(message: String) extends GlobalEvent
