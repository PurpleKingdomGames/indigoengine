package indigo.internal

import indigo.FPS
import indigo.FrameRatePolicy
import indigo.Seconds
import indigo.internal.models.TickUpdateResult

class FrameSchedulerTests extends munit.FunSuite:

  val skip60: FrameRatePolicy  = FrameRatePolicy.Skip(FPS(60))
  val skip144: FrameRatePolicy = FrameRatePolicy.Skip(FPS(144))

  test("the first tick runs immediately, with a zero time delta") {
    val actual =
      FrameScheduler.processFrameTick(FrameScheduler.Uninitialised, Seconds(10), skip60)

    actual match
      case TickUpdateResult.RunNow(timeDelta, time, FrameScheduler.Running(tickAt, updatedAt, due)) =>
        assert(closeEnough(timeDelta, Seconds.zero))
        assert(closeEnough(time, Seconds(10)))
        assert(closeEnough(tickAt, Seconds(10)))
        assert(closeEnough(updatedAt, Seconds(10)))
        assert(closeEnough(due, Seconds(10.0 + 1.0 / 60.0)))

      case r =>
        fail("Expected an initialised RunNow, got: " + r.toString)
  }

  test("waits when the deadline is still more than half a refresh away") {
    val scheduler =
      FrameScheduler.Running(Seconds(10), Seconds(10), Seconds(10.0 + 1.0 / 60.0))

    // One 144Hz refresh later, so we're only 6.9ms into a 16.7ms frame.
    val actual =
      FrameScheduler.processFrameTick(scheduler, Seconds(10.0 + refresh144), skip60)

    actual match
      case TickUpdateResult.Wait(FrameScheduler.Running(tickAt, updatedAt, due)) =>
        // lastTickAt must advance even on a skipped frame, it is what measures the refresh rate.
        assert(closeEnough(tickAt, Seconds(10.0 + refresh144)))
        assert(closeEnough(updatedAt, Seconds(10)))
        assert(closeEnough(due, Seconds(10.0 + 1.0 / 60.0)))

      case r =>
        fail("Expected a Wait, got: " + r.toString)
  }

  test("runs a tick that arrives early, but within half a refresh of the deadline") {
    val scheduler =
      FrameScheduler.Running(Seconds(10), Seconds(10), Seconds(10.0 + 1.0 / 144.0))

    // A hair short of a full 144Hz refresh - under the old policy this cost a whole frame.
    val runningTime = Seconds(10.0 + refresh144 - 0.0005)

    val actual =
      FrameScheduler.processFrameTick(scheduler, runningTime, skip144)

    actual match
      case TickUpdateResult.RunNow(timeDelta, time, _) =>
        assert(closeEnough(timeDelta, Seconds(refresh144 - 0.0005)))
        assert(closeEnough(time, runningTime))

      case r =>
        fail("Expected a RunNow, got: " + r.toString)
  }

  test("a target matching the refresh rate runs every tick") {
    assertEquals(runFor(refresh144, skip144, 144), 144)
  }

  test("carries the remainder forward - 60fps on a 144Hz display") {
    assertEquals(runFor(refresh144, skip60, 144), 60)
  }

  test("carries the remainder forward - 60fps on a 60Hz display") {
    assertEquals(runFor(refresh60, skip60, 60), 60)
  }

  test("carries the remainder forward - 30fps on a 144Hz display") {
    assertEquals(runFor(refresh144, FrameRatePolicy.Skip(FPS(30)), 144), 30)
  }

  test("the display refresh rate is a ceiling") {
    assertEquals(runFor(refresh144, FrameRatePolicy.Skip(FPS(240)), 144), 144)
  }

  test("a stall resyncs rather than running a backlog of catch up frames") {
    val scheduler =
      FrameScheduler.Running(Seconds(10), Seconds(10), Seconds(10.0 + 1.0 / 60.0))

    // Five seconds later - a backgrounded tab, say.
    val actual =
      FrameScheduler.processFrameTick(scheduler, Seconds(15), skip60)

    actual match
      case TickUpdateResult.RunNow(_, _, FrameScheduler.Running(_, _, due)) =>
        assert(closeEnough(due, Seconds(15.0 + 1.0 / 60.0)))

      case r =>
        fail("Expected a RunNow, got: " + r.toString)
  }

  test("Unlimited runs every tick") {
    assertEquals(runFor(refresh144, FrameRatePolicy.Unlimited, 144), 144)
  }

  test("Unlimited reports the real time delta") {
    val scheduler =
      FrameScheduler.Running(Seconds(10), Seconds(10), Seconds(10))

    val actual =
      FrameScheduler.processFrameTick(scheduler, Seconds(10.25), FrameRatePolicy.Unlimited)

    actual match
      case TickUpdateResult.RunNow(timeDelta, _, _) =>
        assert(closeEnough(timeDelta, Seconds(0.25)))

      case r =>
        fail("Expected a RunNow, got: " + r.toString)
  }

  val refresh144: Double = 1.0 / 144.0
  val refresh60: Double  = 1.0 / 60.0

  /** Feeds `ticks` evenly spaced refreshes to the scheduler and counts how many ran a frame. */
  def runFor(refreshInterval: Double, policy: FrameRatePolicy, ticks: Int): Int =
    (1 to ticks)
      .foldLeft((FrameScheduler.Uninitialised: FrameScheduler, 0)) { case ((scheduler, ran), n) =>
        FrameScheduler.processFrameTick(scheduler, Seconds(refreshInterval * n.toDouble), policy) match
          case TickUpdateResult.Wait(next)         => (next, ran)
          case TickUpdateResult.RunNow(_, _, next) => (next, ran + 1)
      }
      ._2

  // JavaScript floating point precision comparison helper
  def closeEnough(a: Seconds, b: Seconds): Boolean =
    Math.abs(a.toDouble - b.toDouble) <= 0.001
