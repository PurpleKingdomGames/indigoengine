package indigo.internal

import indigo.FrameRatePolicy
import indigo.internal.models.TickUpdateResult
import tyrian.*

object Utils:

  private[indigo] def processFrameTick(
      runningTime: Seconds,
      timeDelta: Seconds,
      frameRatePolicy: FrameRatePolicy
  ): TickUpdateResult =
    frameRatePolicy match
      case FrameRatePolicy.Unlimited =>
        TickUpdateResult.RunNow(timeDelta, runningTime)

      case FrameRatePolicy.Skip(target) =>
        val targetFrameDuration = target.asFrameDuration // E.g. 16.7ms or 0.016s for 60fps

        if timeDelta >= targetFrameDuration then TickUpdateResult.RunNow(timeDelta, runningTime)
        else TickUpdateResult.Wait
