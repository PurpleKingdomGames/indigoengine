// package indigo.internal

// import cats.effect.IO
// import indigo.*
// import indigo.core.events.ScreenCaptureEvent
// import indigo.internal.models.FullScreenRequest
// import indigo.internal.models.Msg
// import indigo.platform.events.GlobalEventCallback
// import org.scalajs.dom.HTMLElement
// import org.scalajs.dom.ResizeObserver
// import org.scalajs.dom.html
// import tyrian.*
// import tyrian.extensions.ExtensionId
// import tyrian.platform.Sub
// import tyrian.syntax.*

// object IndigoWatchers:

//   def indigoEventWatcher(
//       extensionId: ExtensionId,
//       eventMapping: PartialIso[GlobalMsg, GlobalEvent],
//       globalEventStream: GlobalEventCallback
//   ): Watcher =
//     val toMsgHandler: GlobalEvent => Option[GlobalMsg] = {
//       case FullScreenEvent.Enter =>
//         Some(Msg.FullScreen(FullScreenRequest.Enter))

//       case FullScreenEvent.Exit =>
//         Some(Msg.FullScreen(FullScreenRequest.Exit))

//       case FullScreenEvent.Toggle =>
//         Some(Msg.FullScreen(FullScreenRequest.Toggle))

//       case AssetEvent.LoadAssets(batch, key, makeAvailable) =>
//         Some(Msg.LoadAssets(batch, key, makeAvailable))

//       case ScreenCaptureEvent.Capture(config, key) =>
//         Some(Msg.CaptureScreen(config, key))

//       case PlaySound(assetName, volume, policy) =>
//         Some(Msg.PlaySound(assetName, volume, policy))

//       case event =>
//         eventMapping.from(event)
//     }

//     val sub = Sub.Observe[IO, GlobalEvent, GlobalMsg, Unit](
//       id = "indigo-event-exchange-" + extensionId.toString,
//       acquire = (callback: Either[Throwable, GlobalEvent] => Unit) =>
//         IO(
//           globalEventStream.registerEventCallback(event => callback(Right(event)))
//         ),
//       release = (_: Unit) =>
//         IO(
//           globalEventStream.clearEventCallback()
//         ),
//       toMsg = toMsgHandler
//     )
//     Watcher(sub)

//   def tick(gameId: GameId): Watcher =
//     Watcher.animationFrameTick(s"[indigo-tick:${gameId.asString}]") { runningTime =>
//       Msg.GameTick(gameId, runningTime)
//     }

//   def resize(gameId: GameId, canvas: html.Canvas, container: HTMLElement): Watcher = {
//     val toMsg: ((Double, Double)) => Option[GlobalMsg] =
//       dimensions => Some(Msg.CanvasResize(dimensions._1.toInt, dimensions._2.toInt))

//     val sub: Sub[IO, GlobalMsg] =
//       Sub.make[IO, (Double, Double), GlobalMsg, ResizeObserver](s"[indigo-resize:${gameId.asString}]") { callback =>
//         val ro =
//           new ResizeObserver((_, _) => {
//             /*
//             This process does not currently respect `devicePixelRatio`, i.e.:

//             ```
//             val dpr    = Option(window.devicePixelRatio).getOrElse(1d)
//             canvas.width = (bounds.width.toDouble * dpr).toInt
//             canvas.height = (bounds.height.toDouble * dpr).toInt
//             ```
//             This keeps it consistent with the renderer. If the renderer
//             stops reading the canvas directly, then we could bring this back.

//             In theory, without accounting for device pixel ratio we could see
//             blurry pixels on devices with high physical to css pixel ratios.
//              */

//             val bounds = container.getBoundingClientRect()

//             canvas.width = bounds.width.toDouble.toInt
//             canvas.height = bounds.height.toDouble.toInt

//             callback(Right((bounds.width, bounds.height)))
//           })

//         ro.observe(container)

//         IO(ro)
//       }(ro => IO(ro.disconnect()))(toMsg)

//     Watcher.fromSub(sub)
//   }
