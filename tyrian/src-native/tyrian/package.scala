package tyrian

object aliases:

  export indigoengine.shared.aliases.*

  // Cmds

  val Logger: tyrian.classic.cmds.Logger.type = tyrian.classic.cmds.Logger
  val Random: tyrian.classic.cmds.Random.type = tyrian.classic.cmds.Random

  // Extensions

  type Extension = tyrian.extensions.Extension

  type ExtensionId = tyrian.extensions.ExtensionId
  val ExtensionId: tyrian.extensions.ExtensionId.type = tyrian.extensions.ExtensionId

export aliases.*
