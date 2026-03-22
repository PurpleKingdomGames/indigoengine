// package indigo.internal

// import indigo.*
// import indigo.internal.models.LaunchStatus
// import indigo.internal.models.Msg
// import indigo.platform.IndigoCoreServices
// import indigo.render.facades.WebGL2RenderingContext
// import org.scalajs.dom.html
// import tyrian.*
// import tyrian.extensions.ExtensionId
// import org.scalajs.dom.ImageData

// object IndigoActions:

//   def launch(
//       extensionId: ExtensionId,
//       game: Game[?, ?, ?],
//       maybeCanvas: Option[html.Canvas],
//       flags: Map[String, String],
//       settings: Settings,
//       services: IndigoCoreServices[html.Image, ImageData]
//   ): Action =
//     Action.run {
//       maybeCanvas match
//         case Some(canvas) =>
//           val bounds = canvas.parentElement.getBoundingClientRect()
//           canvas.width = bounds.width.toInt
//           canvas.height = bounds.height.toInt

//           val context: WebGL2RenderingContext =
//             CanvasAndContext.setupContext(
//               canvas,
//               settings.premultipliedAlpha,
//               settings.transparentBackground,
//               settings.antiAliasing
//             )

//           game.launch(canvas.width, canvas.height, context, flags, services)
//           Msg.Launch(LaunchStatus.Started(extensionId))

//         case _ =>
//           Msg.Launch(LaunchStatus.Retry(extensionId))
//     }
