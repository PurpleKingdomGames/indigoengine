package indigo.internal

import cats.effect.IO
import indigo.Game
import indigo.internal.models.Msg
import tyrian.GlobalMsg
import tyrian.SDLMsg
import tyrian.Watcher
import tyrian.platform.Sub
import tyrian.runtime.SDLEventListenerHandle
import tyrian.runtime.SDLRuntime

object LogDrainWatcher:

  def apply(game: Game[?, ?, ?]): Watcher =
    Watcher.fromSub {
      val id = s"indigo-log-drain-${game.gameId.asString}"

      Sub.make[IO, GlobalMsg, GlobalMsg, SDLEventListenerHandle](id) { cb =>
        IO.delay {
          SDLRuntime.current.get.addSDLEventListener(SDLEventListenerHandle(id)) {
            case SDLMsg.Frame(_) =>
              game.system.logs.collect().foreach { case (level, message) =>
                cb(Right(Msg.Log(level, message)))
              }

            case _ =>
              ()
          }
        }
      } { handle =>
        IO.delay(SDLRuntime.current.get.removeSDLEventListener(handle))
      }(Some.apply)
    }
