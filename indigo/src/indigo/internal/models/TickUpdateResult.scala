package indigo.internal.models

import indigo.Seconds
import indigo.internal.FrameScheduler

enum TickUpdateResult derives CanEqual:
  case Wait(scheduler: FrameScheduler)
  case RunNow(timeDelta: Seconds, runningTime: Seconds, scheduler: FrameScheduler)
