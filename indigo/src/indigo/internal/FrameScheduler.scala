package indigo.internal

import indigo.FrameRatePolicy
import indigo.Seconds
import indigo.internal.models.TickUpdateResult

/** Carries the frame pacing state between ticks, so that `FrameRatePolicy.Skip` can hit the requested frame rate on
  * average rather than drifting below it.
  *
  *   - `lastTickAt` is the time of the previous callback, whether or not it ran a frame. It is only used to measure the
  *     display's refresh interval.
  *   - `lastUpdatedAt` is the time of the previous callback that actually ran a frame, and is what `timeDelta` is
  *     measured against.
  *   - `nextFrameDue` is the deadline for the next frame. It advances by exactly one target frame duration per frame,
  *     so any remainder is carried forward instead of being discarded.
  */
enum FrameScheduler derives CanEqual:
  case Uninitialised
  case Running(lastTickAt: Seconds, lastUpdatedAt: Seconds, nextFrameDue: Seconds)

object FrameScheduler:

  private[indigo] def processFrameTick(
      scheduler: FrameScheduler,
      runningTime: Seconds,
      frameRatePolicy: FrameRatePolicy
  ): TickUpdateResult =
    scheduler match
      case FrameScheduler.Uninitialised =>
        val nextFrameDue =
          frameRatePolicy match
            case FrameRatePolicy.Unlimited    => runningTime
            case FrameRatePolicy.Skip(target) => runningTime + target.asFrameDuration

        TickUpdateResult.RunNow(
          Seconds.zero,
          runningTime,
          FrameScheduler.Running(runningTime, runningTime, nextFrameDue)
        )

      case FrameScheduler.Running(lastTickAt, lastUpdatedAt, nextFrameDue) =>
        frameRatePolicy match
          case FrameRatePolicy.Unlimited =>
            TickUpdateResult.RunNow(
              runningTime - lastUpdatedAt,
              runningTime,
              FrameScheduler.Running(runningTime, runningTime, runningTime)
            )

          case FrameRatePolicy.Skip(target) =>
            val targetFrameDuration = target.asFrameDuration // E.g. 16.7ms or 0.016s for 60fps

            // Half the previous callback interval, so that we run whichever tick lands closest to the
            // deadline rather than always the first one past it. Without this, a tick arriving a hair
            // early costs a whole display frame.
            val tolerance = (runningTime - lastTickAt) * 0.5

            if runningTime >= nextFrameDue - tolerance then
              val advanced = nextFrameDue + targetFrameDuration

              TickUpdateResult.RunNow(
                runningTime - lastUpdatedAt,
                runningTime,
                FrameScheduler.Running(
                  runningTime,
                  runningTime,
                  // If the deadline is already in the past we've stalled or can't keep up, so resync
                  // rather than burning through a backlog of catch-up frames.
                  if advanced < runningTime then runningTime + targetFrameDuration else advanced
                )
              )
            else TickUpdateResult.Wait(FrameScheduler.Running(runningTime, lastUpdatedAt, nextFrameDue))
