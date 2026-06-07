package tyrian

import cats.effect.ExitCode
import cats.effect.IO
import tyrian.internal.ExitSignal

trait App[GraphicsContext, Model] extends internal.AppBase[GraphicsContext, Model]:

  def run(args: List[String]): IO[ExitCode] =
    appStart(args).attempt
      .flatMap {
        case Left(ExitSignal(code)) =>
          // The app shut itself down cleanly via Result.exit / Action.exit
          IO(teardown).as(code)

        case Left(e) =>
          IO(teardown).as(ExitCode.Error)

        case Right(n) =>
          // Unreachable: Here for completeness
          n
      }
      .onCancel(IO(teardown)) // cancelled, e.g. Ctrl+C / SIGTERM
