package tyrian.next

import cats.effect.IO
import indigoengine.shared.collections.Batch
import tyrian.Cmd
import tyrian.Sub

object syntax:

  export indigoengine.shared.syntax.*

  extension (cmd: Cmd[IO, GlobalMsg]) def toAction: Action = Action.fromCmd(cmd)

  extension (sub: Sub[IO, GlobalMsg]) def toWatcher: Watcher = Watcher.fromSub(sub)

  extension [A](values: Option[A]) def toResult(error: => Throwable): Result[A] = Result.fromOption(values, error)

  extension [A](b: Batch[Result[A]]) def sequence: Result[Batch[A]] = Result.sequenceBatch(b)
  extension [A](l: List[Result[A]]) def sequence: Result[List[A]]   = Result.sequenceList(l)
