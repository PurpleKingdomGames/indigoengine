package indigo.internal

import indigo.*
import indigo.internal.models.LaunchStatus
import indigo.internal.models.Msg
import indigo.platform.IndigoCoreServices
import org.scalajs.dom.ImageData
import org.scalajs.dom.html
import tyrian.*
import tyrian.extensions.ExtensionId

object IndigoActions:

  def launch(
      extensionId: ExtensionId,
      game: Game[?, ?, ?],
      maybeCanvas: Option[html.Canvas],
      flags: Map[String, String],
      services: IndigoCoreServices[html.Image, ImageData]
  ): Action =
    Action.run {
      maybeCanvas match
        case Some(canvas) =>
          val bounds = canvas.parentElement.getBoundingClientRect()
          canvas.width = bounds.width.toInt
          canvas.height = bounds.height.toInt

          game.launch(flags, services)
          Msg.Launch(LaunchStatus.Started(extensionId))

        case _ =>
          Msg.Launch(LaunchStatus.Retry(extensionId))
    }
