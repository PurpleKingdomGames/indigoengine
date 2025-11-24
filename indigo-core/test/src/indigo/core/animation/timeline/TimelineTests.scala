package indigo.core.animation.timeline

import indigo.core.Outcome
import indigoengine.shared.collections.Batch
import indigo.core.events.GlobalEvent
import indigo.core.temporal.Signal
import indigo.core.temporal.SignalFunction
import indigoengine.shared.datatypes.Seconds

class TimelineTests extends munit.FunSuite {

  test("check the core types work (no sugar)") {

    val startingValue: Int = 10

    val f1 = (a: Int) => SignalFunction((t: Seconds) => a * t.toInt)
    val f2 = (a: Int) => SignalFunction((t: Seconds) => a * (t.toInt * 2))
    val f3 = (a: Int) => SignalFunction((_: Seconds) => a + 10)

    assert((Signal.Time |> f1(startingValue)).at(Seconds(0)) == 0)
    assert((Signal.Time |> f1(startingValue)).at(Seconds(1)) == 10)
    assert((Signal.Time |> f1(startingValue)).at(Seconds(2)) == 20)
    assert((Signal.Time |> f2(startingValue)).at(Seconds(0)) == 0)
    assert((Signal.Time |> f2(startingValue)).at(Seconds(1)) == 20)
    assert((Signal.Time |> f2(startingValue)).at(Seconds(2)) == 40)
    assert((Signal.Time |> f3(startingValue)).at(Seconds(0)) == 20)
    assert((Signal.Time |> f3(startingValue)).at(Seconds(1)) == 20)
    assert((Signal.Time |> f3(startingValue)).at(Seconds(2)) == 20)

    val windows = Batch(
      TimeWindow(Seconds(1), Seconds(3), f1),
      TimeWindow(Seconds(4), Seconds(6), f2),
      TimeWindow(Seconds(5), Seconds(6), f3)
    )

    val tl = Timeline(windows)

    val actual: List[Option[Int]] =
      List(
        tl.at(Seconds(0))(startingValue),
        tl.at(Seconds(1))(startingValue),
        tl.at(Seconds(2))(startingValue),
        tl.at(Seconds(3))(startingValue),
        tl.at(Seconds(3.5))(startingValue),
        tl.at(Seconds(4))(startingValue),
        tl.at(Seconds(5))(startingValue),
        tl.at(Seconds(6))(startingValue),
        tl.at(Seconds(7))(startingValue)
      )

    val expected: List[Option[Int]] =
      List(
        None,     // tl.at(Seconds(0))
        Some(0),  // tl.at(Seconds(1))
        Some(10), // tl.at(Seconds(2))
        Some(20), // tl.at(Seconds(3))
        None,     // tl.at((3.5).seconds)
        Some(0),  // tl.at(Seconds(4))
        Some(30), // tl.at(Seconds(5))
        Some(50), // tl.at(Seconds(6))
        None      // tl.at(Seconds(7))
      )

    assertEquals(actual, expected)
  }

  test("How to emit an event.") {

    case object MyTestEvent extends GlobalEvent

    val f: Outcome[Int] => SignalFunction[Seconds, Outcome[Int]] = i =>
      SignalFunction { (t: Seconds) =>
        if t < Seconds(1.5) then i
        else i.addGlobalEvents(MyTestEvent)
      }

    val tw = TimeWindow[Outcome[Int]](Seconds(1), Seconds(3), f)

    assertEquals(Timeline(tw).at(Seconds(1))(Outcome(1)), Some(Outcome(1)))
    assertEquals(Timeline(tw).at(Seconds(3))(Outcome(2)), Some(Outcome(2, Batch(MyTestEvent))))
  }

  test("Query animation for its duration / length") {

    val f = (a: Int) => SignalFunction((t: Seconds) => a * t.toInt)

    val windows = Batch(
      TimeWindow(Seconds(1), Seconds(3), f),
      TimeWindow(Seconds(4), Seconds(7), f),
      TimeWindow(Seconds(5), Seconds(6), f)
    )

    val tl = Timeline(windows)

    val expected = Seconds(7)

    assertEquals(tl.duration, expected)
    assertEquals(tl.length, expected)
  }

  test("Returning a default via atOrElse") {

    val f = (a: Int) =>
      SignalFunction { (t: Seconds) =>
        a * t.toInt
      }

    val windows = Batch(
      TimeWindow(Seconds(2), Seconds(3), f)
    )

    val actual = Timeline(windows)

    assertEquals(actual.at(Seconds.zero)(10), None)
    assertEquals(actual.atOrElse(Seconds.zero)(10), 10)
    assertEquals(actual.atOrElse(Seconds.zero, 11)(10), 11)
    assertEquals(actual.at(Seconds(3))(25), Some(25))
    assertEquals(actual.at(Seconds(5))(10), None)
    assertEquals(actual.atOrElse(Seconds(5))(10), 10)
    assertEquals(actual.atOrElse(Seconds(5), 11)(10), 11)
  }

  test("Always return the last frame with atOrLast") {

    val f = (a: Int) =>
      SignalFunction { (t: Seconds) =>
        a * t.toInt
      }

    val windows = Batch(
      TimeWindow(Seconds(2), Seconds(3), f)
    )

    val actual = Timeline(windows)

    assertEquals(actual.at(Seconds(0))(10), None)
    assertEquals(actual.at(Seconds(1))(10), None)
    assertEquals(actual.atOrLast(Seconds(2))(10), Some(0))
    assertEquals(actual.atOrLast(Seconds(3))(10), Some(10))
    assertEquals(actual.atOrLast(Seconds(4))(10), Some(10))
    assertEquals(actual.atOrLast(Seconds(5))(10), Some(10))
    assertEquals(actual.atOrLast(Seconds(50))(10), Some(10))
  }

}
