package tyrian

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

  // Extensions

  type Extension = tyrian.extensions.Extension

  type ExtensionId = tyrian.extensions.ExtensionId
  val ExtensionId: tyrian.extensions.ExtensionId.type = tyrian.extensions.ExtensionId

export aliases.*
