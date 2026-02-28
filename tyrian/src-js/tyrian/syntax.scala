package tyrian

import cats.effect.IO
import indigoengine.shared.collections.Batch
import org.scalajs.dom.EventTarget
import tyrian.classic.internal.SubJsOps
import tyrian.platform.Cmd
import tyrian.platform.Sub

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

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
        SubJsOps.timeout[IO, GlobalMsg](FiniteDuration(duration.toLong, TimeUnit.MILLISECONDS), msg, id)
      )

    /** Creates a watcher that emits a message after a specified duration. */
    def timeout(duration: Seconds, msg: GlobalMsg, id: String): Watcher =
      Watcher.fromSub(
        SubJsOps.timeout[IO, GlobalMsg](FiniteDuration(duration.toMillis.toLong, TimeUnit.MILLISECONDS), msg, id)
      )

    /** Creates a watcher that emits a message after a specified duration. */
    def timeout(duration: Millis, msg: GlobalMsg): Watcher =
      timeout(duration, msg, "[tyrian-watcher-timout] " + duration.toString + msg.toString)

    /** Creates a watcher that emits a message after a specified duration. */
    def timeout(duration: Seconds, msg: GlobalMsg): Watcher =
      timeout(duration.toMillis, msg, "[tyrian-watcher-timout] " + duration.toMillis.toString + msg.toString)

    /** Creates a watcher that repeatedly emits messages at regular intervals. */
    def every(interval: Millis, id: String, toMsg: js.Date => GlobalMsg): Watcher =
      Watcher.fromSub(
        SubJsOps.every[IO](FiniteDuration(interval.toLong, TimeUnit.MILLISECONDS), id).map(toMsg)
      )

    /** Creates a watcher that repeatedly emits messages at regular intervals. */
    def every(interval: Seconds, id: String, toMsg: js.Date => GlobalMsg): Watcher =
      Watcher.fromSub(
        SubJsOps.every[IO](FiniteDuration(interval.toMillis.toLong, TimeUnit.MILLISECONDS), id).map(toMsg)
      )

    /** Creates a watcher that repeatedly emits messages at regular intervals. */
    def every(interval: Millis, toMsg: js.Date => GlobalMsg): Watcher =
      every(interval, "[tyrian-watcher-every] " + interval.toString, toMsg)

    /** Creates a watcher that repeatedly emits messages at regular intervals. */
    def every(interval: Seconds, toMsg: js.Date => GlobalMsg): Watcher =
      every(interval.toMillis, "[tyrian-watcher-every] " + interval.toMillis.toString, toMsg)

    /** Creates a watcher that listens for JavaScript events and emits messages based on them. */
    def fromEvent[A](name: String, target: EventTarget)(extract: A => Option[GlobalMsg]): Watcher =
      Watcher.fromSub(
        SubJsOps.fromEvent[IO, A, GlobalMsg](name, target)(extract)
      )

    /** Creates a watcher that emits messages on each animation frame with elapsed time in seconds. */
    def animationFrameTick(id: String)(toMsg: Double => GlobalMsg): Watcher =
      Watcher.fromSub(
        SubJsOps.animationFrameTick[IO, GlobalMsg](id)(toMsg)
      )
