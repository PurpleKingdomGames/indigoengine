package tyrian.next

import cats.effect.IO

object aliases:

  export indigoengine.shared.aliases.*

  // Cmds

  val Dom: tyrian.cmds.Dom.type                   = tyrian.cmds.Dom
  val Download: tyrian.cmds.Download.type         = tyrian.cmds.Download
  val File: tyrian.cmds.File.type                 = tyrian.cmds.File
  val FileReader: tyrian.cmds.FileReader.type     = tyrian.cmds.FileReader
  val ImageLoader: tyrian.cmds.ImageLoader.type   = tyrian.cmds.ImageLoader
  val LocalStorage: tyrian.cmds.LocalStorage.type = tyrian.cmds.LocalStorage
  val Logger: tyrian.cmds.Logger.type             = tyrian.cmds.Logger
  val Random: tyrian.cmds.Random.type             = tyrian.cmds.Random

  // Bridge

  type TyrianSubSystem[Event, Model] = tyrian.bridge.TyrianSubSystem[IO, Event, Model]
  val TyrianSubSystem: tyrian.bridge.TyrianSubSystem.type = tyrian.bridge.TyrianSubSystem

  type TyrianIndigoBridge[Event, Model] = tyrian.bridge.TyrianIndigoBridge[IO, Event, Model]
  val TyrianIndigoBridge: tyrian.bridge.TyrianIndigoBridge.type = tyrian.bridge.TyrianIndigoBridge

  type IndigoGameId = tyrian.bridge.IndigoGameId
  val IndigoGameId: tyrian.bridge.IndigoGameId.type = tyrian.bridge.IndigoGameId

  // Extensions

  type Extension = tyrian.next.extensions.Extension

  type ExtensionId = tyrian.next.extensions.ExtensionId
  val ExtensionId: tyrian.next.extensions.ExtensionId.type = tyrian.next.extensions.ExtensionId

export aliases.*
