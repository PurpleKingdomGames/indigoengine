package indigo.core.animation.timeline

import indigoengine.shared.collections.Batch
import indigo.core.temporal.SignalFunction
import indigoengine.shared.datatypes.Seconds

class TimeWindowTests extends munit.FunSuite {

  test("length") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assert(tw.length == Seconds(3))
  }

  test("totalTime") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assert(tw.totalTime == Seconds(2))
  }

  test("within") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assert(!tw.within(Seconds(0)))
    assert(tw.within(Seconds(1)))
    assert(tw.within(Seconds(2)))
    assert(tw.within(Seconds(3)))
    assert(!tw.within(Seconds(4)))
    assert(!tw.within(Seconds(5)))
  }

  test("withStart") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assert(tw.withStart(Seconds(0)).start == Seconds(0))
  }

  test("withEnd") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assert(tw.withEnd(Seconds(10)).end == Seconds(10))
  }

  test("withModifier") {
    val f1  = (i: Int) => SignalFunction((_: Seconds) => i)
    val f2  = (_: Int) => SignalFunction((_: Seconds) => 10)
    val tw1 = TimeWindow(Seconds(1), Seconds(3), f1)
    val tw2 = TimeWindow(Seconds(1), Seconds(3), f2)

    assertEquals(Timeline(tw1).at(Seconds(1))(1), Some(1))
    assertEquals(Timeline(tw2).at(Seconds(1))(1), Some(10))
  }

  test("contractBy") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assertEquals(tw.contractBy(Seconds(1)).end, Seconds(2))
  }

  test("expandBy") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assertEquals(tw.expandBy(Seconds(3)).end, Seconds(6))
  }

  test("multiply") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assertEquals(tw.multiply(2.0).start, Seconds(2.0))
    assertEquals(tw.multiply(2.0).end, Seconds(6.0))
  }

  test("shiftBy") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assertEquals(tw.shiftBy(Seconds(10)).start, Seconds(11))
    assertEquals(tw.shiftBy(Seconds(10)).end, Seconds(13))
  }

  test("trim") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(Seconds(1), Seconds(3), f)

    assert(tw.start == Seconds(1))
    assert(tw.trim.start == Seconds(0))
    assert(tw.trim.end == Seconds(2))
  }

}
