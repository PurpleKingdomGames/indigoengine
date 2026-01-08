package indigo.next.bridge

import tyrian.next.GlobalMsg

enum BridgeMsg[Data] extends GlobalMsg:
  case Send(data: Data)
  case Receive(data: Data)
