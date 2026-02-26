package tyrian.classic

import cats.Applicative
import cats.effect.Async
import cats.effect.Sync
import org.scalajs.dom
import org.scalajs.dom.EventTarget
import tyrian.classic.Cmd
import tyrian.classic.Sub
import util.Functions

import java.util.concurrent.TimeUnit
import scala.annotation.nowarn
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

object syntax:

  export tyrian.tags.syntax.*

  /** Make a side effect cmd from any `F[Unit]`
    */
  extension [F[_]](task: F[Unit])
    def toCmd: Cmd.SideEffect[F] =
      Cmd.SideEffect(task)

  /** Make a cmd from any `F[A]`
    */
  extension [F[_]: Applicative, A](task: F[A])
    def toCmd: Cmd.Run[F, A, A] =
      Cmd.Run[F, A](task)

  /** Make a sub from an `fs2.Stream`
    */
  extension [F[_]: Async, A](stream: fs2.Stream[F, A])
    def toSub(id: String): Sub[F, A] =
      Sub.make(id, stream)

  extension (s: Sub.type)
    /** A subscription that emits a msg once. Identical to timeout with a duration of 0. */
    def emit[F[_]: Sync, Msg](msg: Msg): Sub[F, Msg] =
      timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

    /** A subscription that produces a `msg` after a `duration`. */
    def timeout[F[_]: Sync, Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[F, Msg] =
      def task(callback: Either[Throwable, Msg] => Unit): F[Option[F[Unit]]] =
        val handle = dom.window.setTimeout(
          Functions.fun0(() => callback(Right(msg))),
          duration.toMillis.toDouble
        )
        Sync[F].delay {
          Option(Sync[F].delay(dom.window.clearTimeout(handle)))
        }

      Sub.Observe(id, Sync[F].pure(task))

    /** A subscription that produces a `msg` after a `duration`. */
    def timeout[F[_]: Sync, Msg](duration: FiniteDuration, msg: Msg): Sub[F, Msg] =
      timeout(duration, msg, "[tyrian-sub-timeout] " + duration.toString + msg.toString)

    /** A subscription that repeatedly produces a `msg` based on an `interval`. */
    def every[F[_]: Sync](interval: FiniteDuration, id: String): Sub[F, js.Date] =
      Sub.make[F, js.Date, Int](id) { callback =>
        Sync[F].delay {
          dom.window.setInterval(
            Functions.fun0(() => callback(Right(new js.Date()))),
            interval.toMillis.toDouble
          )
        }
      } { handle =>
        Sync[F].delay(dom.window.clearInterval(handle))
      }

    /** A subscription that repeatedly produces a `msg` based on an `interval`. */
    def every[F[_]: Sync](interval: FiniteDuration): Sub[F, js.Date] =
      every(interval, "[tyrian-sub-every] " + interval.toString)

    /** A subscription that emits a `msg` based on an a JavaScript event. */
    def fromEvent[F[_]: Sync, A, Msg](name: String, target: EventTarget)(extract: A => Option[Msg]): Sub[F, Msg] =
      Sub.make[F, A, Msg, js.Function1[A, Unit]](name + target.hashCode) { callback =>
        Sync[F].delay {
          val listener = Functions.fun { (a: A) =>
            callback(Right(a))
          }
          target.addEventListener(name, listener)
          listener
        }
      } { listener =>
        Sync[F].delay(target.removeEventListener(name, listener))
      }(extract)

    /** A subscription that emits a `msg` based on the running time in seconds whenever the browser renders an animation
      * frame.
      */
    @nowarn("msg=unused")
    def animationFrameTick[F[_]: Async, Msg](id: String)(toMsg: Double => Msg): Sub[F, Msg] =
      Sub.make(
        id,
        fs2.Stream.repeatEval {
          Async[F].async_[Msg] { cb =>
            dom.window.requestAnimationFrame { t =>
              cb(Right(toMsg(t / 1000)))
            }
            ()
          }
        }
      )
