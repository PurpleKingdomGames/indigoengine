package indigo.next

import cats.effect.IO

// Indigo

export indigo.aliases.*

type BootResult[BootData, Model] = indigo.BootResult[BootData, Model]
val BootResult: indigo.BootResult.type = indigo.BootResult

// Bridge

type TyrianSubSystem[Event, Model] = tyrian.bridge.TyrianSubSystem[IO, Event, Model]
val TyrianSubSystem: tyrian.bridge.TyrianSubSystem.type = tyrian.bridge.TyrianSubSystem

type TyrianIndigoBridge[Event, Model] = tyrian.bridge.TyrianIndigoBridge[IO, Event, Model]
val TyrianIndigoBridge: tyrian.bridge.TyrianIndigoBridge.type = tyrian.bridge.TyrianIndigoBridge

type IndigoGameId = tyrian.bridge.IndigoGameId
val IndigoGameId: tyrian.bridge.IndigoGameId.type = tyrian.bridge.IndigoGameId

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
