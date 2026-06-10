package tyrian

import cats.effect.ExitCode
import cats.effect.IO
import tyrian.internal.ExitSignal

trait App[GraphicsContext, Model] extends internal.AppBase[GraphicsContext, Model]:

  private def teardownAll: Unit =
    extensionsRegister.teardown
    teardown

  def run(args: List[String]): IO[ExitCode] =
    appStart(args).attempt
      .flatMap {
        case Left(ExitSignal(code)) =>
          // The app shut itself down cleanly via Result.exit / Action.exit
          IO(teardownAll).as(code)

        case Left(e) =>
          IO(teardownAll).as(ExitCode.Error)

        case Right(n) =>
          // Unreachable: Here for completeness
          n
      }
      .onCancel(IO(teardownAll)) // cancelled, e.g. Ctrl+C / SIGTERM
