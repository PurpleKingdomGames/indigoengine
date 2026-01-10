package indigo.next.bridge

import tyrian.next.GlobalMsg

enum BridgeMsg extends GlobalMsg:
  case Send(data: BridgeData)
  case Receive(data: BridgeData)
