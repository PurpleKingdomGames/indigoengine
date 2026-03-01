package tyrian.classic.internal

import cats.effect.kernel.Async
import tyrian.platform.Sub

import scala.concurrent.duration.FiniteDuration

object SubNativeOps:

  /** A subscription that emits a msg once. */
  def emit[F[_]: Async, Msg](msg: Msg, id: String): Sub[F, Msg] =
    Sub.fromStream(id, fs2.Stream.emit[F, Msg](msg))

  /** A subscription that produces a `msg` after a `duration`. */
  def timeout[F[_]: Async, Msg](duration: FiniteDuration, msg: Msg, id: String): Sub[F, Msg] =
    Sub.fromStream(id, fs2.Stream.awakeDelay(duration).map(_ => msg))

  /** A subscription that repeatedly produces a `msg` based on an `interval`. */
  def every[F[_]: Async](interval: FiniteDuration, id: String): Sub[F, FiniteDuration] =
    Sub.fromStream(id, fs2.Stream.awakeEvery(interval))
