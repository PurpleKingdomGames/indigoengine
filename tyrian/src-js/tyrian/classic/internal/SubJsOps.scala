package tyrian.classic.internal

import cats.effect.kernel.Async
import org.scalajs.dom
import org.scalajs.dom.EventTarget
import tyrian.platform.Sub

import java.util.concurrent.TimeUnit
import scala.annotation.nowarn
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

object SubJsOps:

  /** A subscription that emits a msg once. Identical to timeout with a duration of 0. */
  def emit[F[_]: Async, Msg](msg: Msg): Sub[F, Msg] =
    timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

  /** A subscription that produces a `msg` after a `duration`. */
  def timeout[F[_]: Async, Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[F, Msg] =
    def task(callback: Either[Throwable, Msg] => Unit): F[Option[F[Unit]]] =
      val handle = dom.window.setTimeout(
        Functions.fun0(() => callback(Right(msg))),
        duration.toMillis.toDouble
      )
      Async[F].delay {
        Option(Async[F].delay(dom.window.clearTimeout(handle)))
      }

    Sub.Observe(id, Async[F].pure(task))

  /** A subscription that repeatedly produces a `msg` based on an `interval`. */
  def every[F[_]: Async](interval: FiniteDuration, id: String): Sub[F, js.Date] =
    Sub.make[F, js.Date, Int](id) { callback =>
      Async[F].delay {
        dom.window.setInterval(
          Functions.fun0(() => callback(Right(new js.Date()))),
          interval.toMillis.toDouble
        )
      }
    } { handle =>
      Async[F].delay(dom.window.clearInterval(handle))
    }

  /** A subscription that emits a `msg` based on an a JavaScript event. */
  def fromEvent[F[_]: Async, A, Msg](name: String, target: EventTarget)(extract: A => Option[Msg]): Sub[F, Msg] =
    Sub.make[F, A, Msg, js.Function1[A, Unit]](name + target.hashCode) { callback =>
      Async[F].delay {
        val listener = Functions.fun { (a: A) =>
          callback(Right(a))
        }
        target.addEventListener(name, listener)
        listener
      }
    } { listener =>
      Async[F].delay(target.removeEventListener(name, listener))
    }(extract)

  /** A subscription that emits a `msg` based on the running time in seconds whenever the browser renders an animation
    * frame.
    */
  @nowarn("msg=unused")
  def animationFrameTick[F[_]: Async, Msg](id: String)(toMsg: Double => Msg): Sub[F, Msg] =
    Sub.fromStream(
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
