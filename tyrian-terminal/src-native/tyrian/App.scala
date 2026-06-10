package tyrian

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.kernel.Outcome
import cats.effect.kernel.Resource
import cats.syntax.all.*
import tyrian.internal.ExitSignal

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.duration.*
import scala.scalanative.libc.signal.SIGINT
import scala.scalanative.libc.signal.SIGTERM
import scala.scalanative.libc.signal.signal
import scala.scalanative.unsafe.CFuncPtr1
import scala.scalanative.unsafe.CInt

trait App[GraphicsContext, Model] extends internal.AppBase[GraphicsContext, Model]:

  private def teardownAll: Unit =
    extensionsRegister.teardown
    teardown

  def run(args: List[String]): IO[ExitCode] =
    App.onSignals
      .use { awaitSignal =>
        appStart(args).start
          .flatMap { fiber =>
            (awaitSignal *> fiber.cancel).background.surround {
              fiber.join.flatMap {
                case Outcome.Canceled() =>
                  // cancelled, e.g. Ctrl+C / SIGTERM
                  IO(teardownAll).as(ExitCode.Success)

                case Outcome.Errored(ExitSignal(code)) =>
                  // The app shut itself down cleanly via Result.exit / Action.exit
                  IO(teardownAll).as(code)

                case Outcome.Errored(e) =>
                  IO(teardownAll).as(ExitCode.Error)

                case Outcome.Succeeded(fa) =>
                  // Unreachable: Here for completeness
                  fa
              }
            }
          }
      }

object App:

  private val interrupted: AtomicBoolean =
    new AtomicBoolean(false)

  private val handler: CFuncPtr1[CInt, Unit] =
    (_: CInt) => interrupted.set(true)

  private def onSignals: Resource[IO, IO[Unit]] =
    Resource
      .make(
        IO {
          val prevInt  = signal(SIGINT, handler)
          val prevTerm = signal(SIGTERM, handler)

          (prevInt, prevTerm)
        }
      ) { case (prevInt, prevTerm) =>
        IO(signal(SIGINT, prevInt)).void *>
          IO(signal(SIGTERM, prevTerm)).void
      }
      .as {
        def loop: IO[Unit] =
          IO(interrupted.get())
            .ifM(
              IO.unit,
              IO.sleep(50.millis) *> loop
            )

        loop
      }
