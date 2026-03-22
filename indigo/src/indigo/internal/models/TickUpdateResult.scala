package indigo.internal.models

import indigo.Seconds

enum TickUpdateResult derives CanEqual:
  case Wait
  case RunNow(timeDelta: Seconds, updatedAt: Seconds)
