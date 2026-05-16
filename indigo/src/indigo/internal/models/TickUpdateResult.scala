package indigo.internal.models

import indigo.Seconds

// TODO: Still used in JS and Native?

enum TickUpdateResult derives CanEqual:
  case Wait
  case RunNow(timeDelta: Seconds, updatedAt: Seconds)
