package indigo

object jsaliases:

  // Bridge

  type BridgeData = bridge.BridgeData

  type BridgeEvent = bridge.BridgeEvent
  val BridgeEvent: bridge.BridgeEvent.type = bridge.BridgeEvent

  type BridgeMsg = bridge.BridgeMsg
  val BridgeMsg: bridge.BridgeMsg.type = bridge.BridgeMsg

export jsaliases.*
