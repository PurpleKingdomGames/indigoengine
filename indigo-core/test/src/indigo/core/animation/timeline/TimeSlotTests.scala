package indigo.core.animation.timeline

import indigoengine.shared.collections.Batch
import indigo.core.temporal.SignalFunction
import indigoengine.shared.datatypes.Seconds

class TimeSlotTests extends munit.FunSuite {

  import TimeSlot.*

  val f = (i: Int) => SignalFunction((_: Seconds) => i)

  test("toWindows") {
    val slot: TimeSlot[Int] = pause(Seconds(2)) `andThen`
      animate(Seconds(5))(f) `andThen`
      animate(Seconds(3), f)

    val actual =
      slot.toWindows

    val expected =
      Batch(
        TimeWindow(Seconds(2), Seconds(7), f),
        TimeWindow(Seconds(7), Seconds(10), f)
      )

    assert(actual == expected)
  }

}
