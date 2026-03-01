package tyrian.classic

import cats.effect.kernel.Async
import cats.effect.kernel.Clock
import cats.effect.kernel.Ref
import cats.effect.std.Dispatcher
import indigoengine.shared.collections.Batch
import tyrian.Location
import tyrian.platform.runtime.PresentView

enum Terminal[Msg] derives CanEqual:
  case NoOp()
  case Print(msg: String)
  case Combine(t1: Terminal[Msg], t2: Terminal[Msg])

object Terminal:

  extension [Msg](t: Terminal[Msg])
    def |+|(other: Terminal[Msg]): Terminal[Msg] =
      Terminal.Combine(t, other)

    def draw: String =
      def rec(remaining: Batch[Terminal[?]], acc: Batch[String]): String =
        if remaining.isEmpty then acc.mkString("\n")
        else
          val h = remaining.head
          val t = remaining.tail

          h match
            case Terminal.NoOp() =>
              rec(t, acc)

            case Terminal.Print(msg) =>
              rec(t, acc :+ msg)

            case Terminal.Combine(t1, t2) =>
              rec(t ++ Batch(t1, t2), acc)

      rec(Batch(t), Batch.empty)

  given PresentView[Terminal, Unit] with
    def draw[F[_], Model, Msg](
        dispatcher: Dispatcher[F],
        viewState: Ref[F, Unit],
        model: Ref[F, Model],
        view: Model => Terminal[Msg],
        onMsg: Msg => Unit,
        router: Location => Option[Msg]
    )(using F: Async[F], clock: Clock[F]): F[Unit] =
      F.map(model.get) { m =>
        println(view(m).draw)
      }
