package indigo.next.bridge

import indigo.core.events.GlobalEvent

enum BridgeEvent[Data] extends GlobalEvent:
  case Send(data: Data)
  case Receive(data: Data)
