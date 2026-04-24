package indigo

class IndigoTests extends munit.FunSuite:

  val lastUpdated: Seconds             = Seconds(10)
  val frameRatePolicy: FrameRatePolicy = FrameRatePolicy.Skip(FPS(60))
  val tick: Seconds => Unit            = _ => ()

  test("processFrameTick - frame update slower than framerate") {
    val runningTime: Seconds = Seconds(10.032) // Two frames worth of time

    val actual =
      Indigo.processFrameTick(lastUpdated, runningTime, frameRatePolicy)

    val expected: Indigo.TickUpdateResult =
      Indigo.TickUpdateResult.RunNow(Seconds(0.032), runningTime)

    assert(resultCloseEnough(clue(actual.unsafeGet), clue(expected)))
  }

  test("processFrameTick - frame update too early") {
    // Suspiciously quick, but ok for the test. This + the mean is < FPS (0.016)
    val runningTime: Seconds = Seconds(10.001)

    val actual =
      Indigo.processFrameTick(lastUpdated, runningTime, frameRatePolicy)

    val expected: Indigo.TickUpdateResult =
      Indigo.TickUpdateResult.Wait

    assert(resultCloseEnough(clue(actual.unsafeGet), clue(expected)))
  }

  // JavaScript floating point precision comparison helper
  def resultCloseEnough(a: Indigo.TickUpdateResult, b: Indigo.TickUpdateResult): Boolean =
    (a, b) match
      case (Indigo.TickUpdateResult.Wait, Indigo.TickUpdateResult.Wait) =>
        true

      case (Indigo.TickUpdateResult.RunNow(tdA, rtA), Indigo.TickUpdateResult.RunNow(tdB, rtB)) =>
        closeEnough(tdA.toDouble, tdB.toDouble) && closeEnough(rtA.toDouble, rtB.toDouble)

      case _ =>
        false

  def closeEnough(a: Double, b: Double): Boolean =
    Math.abs(a - b) <= 0.001
