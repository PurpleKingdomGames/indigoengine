package tyrian.next

import cats.effect.IO

object aliases:

  export indigoengine.shared.aliases.*

  // Cmds

  val Dom: tyrian.classic.cmds.Dom.type                   = tyrian.classic.cmds.Dom
  val Download: tyrian.classic.cmds.Download.type         = tyrian.classic.cmds.Download
  val File: tyrian.classic.cmds.File.type                 = tyrian.classic.cmds.File
  val FileReader: tyrian.classic.cmds.FileReader.type     = tyrian.classic.cmds.FileReader
  val ImageLoader: tyrian.classic.cmds.ImageLoader.type   = tyrian.classic.cmds.ImageLoader
  val LocalStorage: tyrian.classic.cmds.LocalStorage.type = tyrian.classic.cmds.LocalStorage
  val Logger: tyrian.classic.cmds.Logger.type             = tyrian.classic.cmds.Logger
  val Random: tyrian.classic.cmds.Random.type             = tyrian.classic.cmds.Random

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
