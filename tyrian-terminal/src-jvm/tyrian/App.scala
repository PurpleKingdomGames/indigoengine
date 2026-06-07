package tyrian

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.kernel.Outcome

trait App[GraphicsContext, Model] extends internal.AppBase[GraphicsContext, Model]:

  def run(args: List[String]): IO[ExitCode] =
    appStart(args)
      .guaranteeCase {
        case Outcome.Canceled() =>
          // cancelled, e.g. Ctrl+C
          IO(teardown)

        case Outcome.Errored(e) =>
          IO(teardown) *>
            IO.println(s"App exited following an error:\n${e.getMessage}")

        case Outcome.Succeeded(_) =>
          // Exited because the app presumably shut itself down cleanly, nothing to do.
          IO(teardown)
      }
      .as(ExitCode.Success)
