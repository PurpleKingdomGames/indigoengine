package tyrian.classic

import cats.Applicative
import cats.effect.Async
import org.scalajs.dom.EventTarget
import tyrian.classic.Cmd
import tyrian.classic.Sub
import tyrian.classic.internal.SubJsOps

import java.util.concurrent.TimeUnit
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
      Sub.fromStream(id, stream)

  extension (s: Sub.type)
    /** A subscription that emits a msg once. Identical to timeout with a duration of 0. */
    def emit[F[_]: Async, Msg](msg: Msg): Sub[F, Msg] =
      SubJsOps.timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

    /** A subscription that produces a `msg` after a `duration`. */
    def timeout[F[_]: Async, Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[F, Msg] =
      SubJsOps.timeout[F, Msg](duration, msg, id)

    /** A subscription that produces a `msg` after a `duration`. */
    def timeout[F[_]: Async, Msg](duration: FiniteDuration, msg: Msg): Sub[F, Msg] =
      timeout(duration, msg, "[tyrian-sub-timeout] " + duration.toString + msg.toString)

    /** A subscription that repeatedly produces a `msg` based on an `interval`. */
    def every[F[_]: Async](interval: FiniteDuration, id: String): Sub[F, js.Date] =
      SubJsOps.every[F](interval, id)

    /** A subscription that repeatedly produces a `msg` based on an `interval`. */
    def every[F[_]: Async](interval: FiniteDuration): Sub[F, js.Date] =
      every(interval, "[tyrian-sub-every] " + interval.toString)

    /** A subscription that emits a `msg` based on an a JavaScript event. */
    def fromEvent[F[_]: Async, A, Msg](name: String, target: EventTarget)(extract: A => Option[Msg]): Sub[F, Msg] =
      SubJsOps.fromEvent[F, A, Msg](name, target)(extract)

    /** A subscription that emits a `msg` based on the running time in seconds whenever the browser renders an animation
      * frame.
      */
    def animationFrameTick[F[_]: Async, Msg](id: String)(toMsg: Double => Msg): Sub[F, Msg] =
      SubJsOps.animationFrameTick[F, Msg](id)(toMsg)
