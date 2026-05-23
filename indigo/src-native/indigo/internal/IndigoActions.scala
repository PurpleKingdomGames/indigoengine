package indigo.internal

import indigo.*
import indigo.internal.models.LaunchStatus
import indigo.internal.models.Msg
import indigo.platform.IndigoCoreServices
import indigo.platform.assets.TempImageData
import tyrian.*
import tyrian.extensions.ExtensionId

object IndigoActions:

  def launch(
      extensionId: ExtensionId,
      game: Game[?, ?, ?],
      args: Array[String],
      services: IndigoCoreServices[TempImageData, Array[Byte]]
  ): Action =
    Action.run {
      game.launch(args, services)

      // Succeeds regardless for now, review once more of the implementation is in place.
      Msg.Launch(LaunchStatus.Started(extensionId))
    }
