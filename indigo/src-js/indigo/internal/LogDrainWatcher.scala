package indigo.internal

import cats.effect.IO
import indigo.Game
import indigo.internal.models.Msg
import org.scalajs.dom
import tyrian.GlobalMsg
import tyrian.Watcher
import tyrian.platform.Sub

import scala.scalajs.js

object LogDrainWatcher:

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  final private class StopFlag(var stopped: Boolean)

  def apply(game: Game[?, ?, ?]): Watcher =
    Watcher.fromSub {
      Sub.make[IO, GlobalMsg, GlobalMsg, StopFlag](s"indigo-log-drain-${game.gameId.asString}") { cb =>
        IO.delay {
          val flag = new StopFlag(false)

          lazy val tick: js.Function1[Double, Unit] = (_: Double) =>
            if !flag.stopped then {
              game.system.logs.collect().foreach { case (level, message) =>
                cb(Right(Msg.Log(level, message)))
              }
              val _ = dom.window.requestAnimationFrame(tick)
              ()
            }

          val _ = dom.window.requestAnimationFrame(tick)
          flag
        }
      } { flag =>
        IO.delay { flag.stopped = true }
      }(Some.apply)
    }
