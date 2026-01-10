package indigo.next

// Indigo

export indigo.aliases.*

type BootResult[BootData, Model] = indigo.BootResult[BootData, Model]
val BootResult: indigo.BootResult.type = indigo.BootResult

// Bridge

type BridgeData = bridge.BridgeData

type BridgeEvent = bridge.BridgeEvent
val BridgeEvent: bridge.BridgeEvent.type = bridge.BridgeEvent

type BridgeMsg = bridge.BridgeMsg
val BridgeMsg: bridge.BridgeMsg.type = bridge.BridgeMsg

// Scenes

type Scene[StartUpData, GameModel] = indigo.next.scenes.Scene[StartUpData, GameModel]
val Scene: indigo.next.scenes.Scene.type = indigo.next.scenes.Scene

type SceneName = indigo.next.scenes.SceneName
val SceneName: indigo.next.scenes.SceneName.type = indigo.next.scenes.SceneName

type SceneContext[StartupData] = indigo.next.scenes.SceneContext[StartupData]
val SceneContext: indigo.next.scenes.SceneContext.type = indigo.next.scenes.SceneContext

type SceneEvent = indigo.next.scenes.SceneEvent
val SceneEvent: indigo.next.scenes.SceneEvent.type = indigo.next.scenes.SceneEvent

type SceneFinder = indigo.next.scenes.SceneFinder
val SceneFinder: indigo.next.scenes.SceneFinder.type = indigo.next.scenes.SceneFinder

type SceneManager[StartUpData, GameModel] = indigo.next.scenes.SceneManager[StartUpData, GameModel]
val SceneManager: indigo.next.scenes.SceneManager.type = indigo.next.scenes.SceneManager
