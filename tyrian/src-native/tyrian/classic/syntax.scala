package tyrian.classic

import cats.Applicative
import cats.effect.Async
import tyrian.classic.internal.SubNativeOps
import tyrian.platform.Cmd
import tyrian.platform.Sub

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object syntax:

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
      SubNativeOps.timeout(FiniteDuration(0, TimeUnit.MILLISECONDS), msg, msg.toString)

    /** A subscription that produces a `msg` after a `duration`. */
    def timeout[F[_]: Async, Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[F, Msg] =
      SubNativeOps.timeout[F, Msg](duration, msg, id)

    /** A subscription that produces a `msg` after a `duration`. */
    def timeout[F[_]: Async, Msg](duration: FiniteDuration, msg: Msg): Sub[F, Msg] =
      timeout(duration, msg, "[tyrian-sub-timeout] " + duration.toString + msg.toString)

    /** A subscription that repeatedly produces a `msg` based on an `interval`. */
    def every[F[_]: Async](interval: FiniteDuration, id: String): Sub[F, FiniteDuration] =
      SubNativeOps.every[F](interval, id)

    /** A subscription that repeatedly produces a `msg` based on an `interval`. */
    def every[F[_]: Async](interval: FiniteDuration): Sub[F, FiniteDuration] =
      every(interval, "[tyrian-sub-every] " + interval.toString)
