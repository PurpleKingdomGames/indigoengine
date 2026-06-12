package tyrian.classic

import cats.effect.kernel.Async
import cats.effect.kernel.Clock
import cats.effect.kernel.Ref
import cats.effect.std.Dispatcher
import indigoengine.shared.collections.Batch
import tyrian.Location
import tyrian.platform.runtime.PresentView

/** Console is a JVM/Native view for the 'classic' Tyrian set up that produces an append only output, printing to stdout
  * and stderr.
  */
enum Console[Msg] derives CanEqual:
  case NoOp()
  case Stdout(message: String)
  case Stderr(message: String)
  case Combine(t1: Console[Msg], t2: Console[Msg])

object Console:

  def stdout[Msg](message: String): Console.Stdout[Msg] =
    Console.Stdout(message)
  def stderr[Msg](message: String): Console.Stderr[Msg] =
    Console.Stderr(message)
  def println[Msg](message: String): Console.Stdout[Msg] =
    Console.Stdout(message)
  def errorln[Msg](message: String): Console.Stderr[Msg] =
    Console.Stderr(message)

  extension [Msg](c: Console[Msg])
    def |+|(other: Console[Msg]): Console[Msg] =
      Console.Combine(c, other)

    /** The map operation on the Console type is really a no-op, because the Console does not have interactive elements,
      * but it's here for consistency.
      */
    def map[N](f: Msg => N): Console[N] =
      c match
        case NoOp() =>
          NoOp()

        case Stdout(message) =>
          Stdout(message)

        case Stderr(message) =>
          Stderr(message)

        case Combine(t1, t2) =>
          Combine(t1.map(f), t2.map(f))

    def stdout(message: String): Console[Msg] =
      Console.Combine(c, Console.Stdout(message))
    def stderr(message: String): Console[Msg] =
      Console.Combine(c, Console.Stderr(message))
    def println(message: String): Console[Msg] =
      Console.Combine(c, Console.Stdout(message))
    def errorln(message: String): Console[Msg] =
      Console.Combine(c, Console.Stderr(message))

    def draw[F[_]](using F: Async[F]): Batch[F[Unit]] =
      def rec(remaining: Batch[Console[?]], acc: Batch[F[Unit]]): Batch[F[Unit]] =
        if remaining.isEmpty then acc
        else
          val h = remaining.head
          val t = remaining.tail

          h match
            case Console.NoOp() =>
              rec(t, acc)

            case Console.Stdout(msg) =>
              rec(t, acc :+ F.delay(System.out.println(msg)))

            case Console.Stderr(msg) =>
              rec(t, acc :+ F.delay(System.err.println(msg)))

            case Console.Combine(t1, t2) =>
              rec(Batch(t1, t2) ++ t, acc)

      rec(Batch(c), Batch.empty)

  given PresentView[Console, Unit] with
    def draw[F[_], Model, Msg](
        dispatcher: Dispatcher[F],
        viewState: Ref[F, Unit],
        model: Ref[F, Model],
        view: Model => Console[Msg],
        onMsg: Msg => Unit,
        router: Location => Option[Msg]
    )(using F: Async[F], clock: Clock[F]): F[Unit] =
      F.flatMap(model.get): m =>
        view(m).draw.foldLeft(F.unit)((acc, next) => F.flatMap(acc)(_ => next))
