package tyrian

import cats.effect.IO
import indigoengine.shared.collections.Batch
import tyrian.classic.internal.SubNativeOps
import tyrian.platform.Cmd
import tyrian.platform.Sub

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object syntax:

  export indigoengine.shared.syntax.*

  extension (cmd: Cmd[IO, GlobalMsg]) def toAction: Action = Action.fromCmd(cmd)

  extension (sub: Sub[IO, GlobalMsg]) def toWatcher: Watcher = Watcher.fromSub(sub)

  extension [A](values: Option[A]) def toResult(error: => Throwable): Result[A] = Result.fromOption(values, error)

  extension [A](b: Batch[Result[A]]) def sequence: Result[Batch[A]] = Result.sequenceBatch(b)
  extension [A](l: List[Result[A]]) def sequence: Result[List[A]]   = Result.sequenceList(l)

  extension (w: Watcher.type)

    /** Creates a watcher that emits a message immediately. */
    def emit(msg: GlobalMsg): Watcher =
      timeout(Millis.zero, msg, msg.toString)

    /** Creates a watcher that emits a message after a specified duration. */
    def timeout(duration: Millis, msg: GlobalMsg, id: String): Watcher =
      Watcher.fromSub(
        SubNativeOps.timeout[IO, GlobalMsg](FiniteDuration(duration.toLong, TimeUnit.MILLISECONDS), msg, id)
      )

    /** Creates a watcher that emits a message after a specified duration. */
    def timeout(duration: Seconds, msg: GlobalMsg, id: String): Watcher =
      Watcher.fromSub(
        SubNativeOps.timeout[IO, GlobalMsg](FiniteDuration(duration.toMillis.toLong, TimeUnit.MILLISECONDS), msg, id)
      )

    /** Creates a watcher that emits a message after a specified duration. */
    def timeout(duration: Millis, msg: GlobalMsg): Watcher =
      timeout(duration, msg, "[tyrian-watcher-timeout] " + duration.toString + msg.toString)

    /** Creates a watcher that emits a message after a specified duration. */
    def timeout(duration: Seconds, msg: GlobalMsg): Watcher =
      timeout(duration.toMillis, msg, "[tyrian-watcher-timeout] " + duration.toMillis.toString + msg.toString)

    /** Creates a watcher that repeatedly emits messages at regular intervals. */
    def every(interval: Millis, id: String, toMsg: FiniteDuration => GlobalMsg): Watcher =
      Watcher.fromSub(
        SubNativeOps.every[IO](FiniteDuration(interval.toLong, TimeUnit.MILLISECONDS), id).map(toMsg)
      )

    /** Creates a watcher that repeatedly emits messages at regular intervals. */
    def every(interval: Seconds, id: String, toMsg: FiniteDuration => GlobalMsg): Watcher =
      Watcher.fromSub(
        SubNativeOps.every[IO](FiniteDuration(interval.toMillis.toLong, TimeUnit.MILLISECONDS), id).map(toMsg)
      )

    /** Creates a watcher that repeatedly emits messages at regular intervals. */
    def every(interval: Millis, toMsg: FiniteDuration => GlobalMsg): Watcher =
      every(interval, "[tyrian-watcher-every] " + interval.toString, toMsg)

    /** Creates a watcher that repeatedly emits messages at regular intervals. */
    def every(interval: Seconds, toMsg: FiniteDuration => GlobalMsg): Watcher =
      every(interval.toMillis, "[tyrian-watcher-every] " + interval.toMillis.toString, toMsg)
