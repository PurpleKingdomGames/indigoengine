package indigo.internal

import indigo.FPS
import indigo.FrameRatePolicy
import indigo.Seconds
import indigo.internal.models.TickUpdateResult

class UtilsTests extends munit.FunSuite:

  val lastUpdated: Seconds             = Seconds(10)
  val frameRatePolicy: FrameRatePolicy = FrameRatePolicy.Skip(FPS(60))

  test("processFrameTick - frame update slower than framerate") {
    val runningTime: Seconds = Seconds(10.032) // Two frames worth of time

    val actual =
      Utils.processFrameTick(lastUpdated, runningTime, frameRatePolicy)

    val expected: TickUpdateResult =
      TickUpdateResult.RunNow(Seconds(0.032), runningTime)

    assert(resultCloseEnough(clue(actual.unsafeGet), clue(expected)))
  }

  test("processFrameTick - frame update too early") {
    val runningTime: Seconds = Seconds(10.001)

    val actual =
      Utils.processFrameTick(lastUpdated, runningTime, frameRatePolicy)

    val expected: TickUpdateResult =
      TickUpdateResult.Wait

    assert(resultCloseEnough(clue(actual.unsafeGet), clue(expected)))
  }

  // JavaScript floating point precision comparison helper
  def resultCloseEnough(a: TickUpdateResult, b: TickUpdateResult): Boolean =
    (a, b) match
      case (TickUpdateResult.Wait, TickUpdateResult.Wait) =>
        true

      case (TickUpdateResult.RunNow(tdA, rtA), TickUpdateResult.RunNow(tdB, rtB)) =>
        closeEnough(tdA.toDouble, tdB.toDouble) && closeEnough(rtA.toDouble, rtB.toDouble)

      case _ =>
        false

  def closeEnough(a: Double, b: Double): Boolean =
    Math.abs(a - b) <= 0.001
