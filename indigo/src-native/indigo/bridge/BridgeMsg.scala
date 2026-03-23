package indigo.bridge

import tyrian.GlobalMsg

enum BridgeMsg extends GlobalMsg:
  case Send(data: BridgeData)
  case Receive(data: BridgeData)
