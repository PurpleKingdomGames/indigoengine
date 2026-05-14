package tyrian

import cats.effect.IO
import tyrian.platform.Sub
import tyrian.runtime.SDLEventListenerHandle
import tyrian.runtime.SDLRuntime

object SDLWatcher:

  extension (w: Watcher.type)

    /** Creates a watcher that emits a message whenever the SDL runtime produces an `SDLMsg`
      */
    def fromSDLMsg(id: String)(extract: SDLMsg => Option[GlobalMsg]): Watcher =
      Watcher.fromSub {
        val _id = "tyrian-sdl-msg-watcher:" + id
        Sub.make[IO, SDLMsg, GlobalMsg, SDLEventListenerHandle](_id) { cb =>
          IO.delay {
            SDLRuntime.current.get.addSDLEventListener(SDLEventListenerHandle(_id))(a => cb(Right(a)))
          }
        } { handle =>
          IO.delay(SDLRuntime.current.get.removeSDLEventListener(handle))
        }(extract)
      }
