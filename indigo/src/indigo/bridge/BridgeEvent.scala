package indigo.bridge

import indigo.core.events.GlobalEvent

enum BridgeEvent extends GlobalEvent:
  case Send(data: BridgeData)
  case Receive(data: BridgeData)
