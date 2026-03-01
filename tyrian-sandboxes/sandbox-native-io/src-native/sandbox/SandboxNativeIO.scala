package sandbox

import cats.effect.IO
import tyrian.classic.Terminal
import tyrian.classic.TyrianIOApp
import tyrian.platform.Cmd
import tyrian.platform.Sub

import scala.concurrent.duration.*

object SandboxNativeIO extends TyrianIOApp[Msg, Model]:

  def init(args: Array[String]): (Model, Cmd[IO, Msg]) =
    val cmd = Cmd.SideEffect[IO](println("Starting my command line app!"))

    Model(None) -> cmd

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Tick(t) =>
      val cmd = Cmd.None
      val m   = model.copy(timestamp = Some(t))

      (m, cmd)

    case Msg.NoOp =>
      model -> Cmd.None

  def view(model: Model): Terminal[Msg] =
    model.timestamp match
      case None =>
        Terminal.NoOp()

      case Some(t) =>
        Terminal.Print(s"...tick (${t.toString()})")

  def subscriptions(model: Model): Sub[IO, Msg] =
    val d: FiniteDuration = 1.second
    val s: fs2.Stream[IO, Msg] = fs2.Stream.awakeEvery[IO](d).map { t =>
      Msg.Tick(t)
    }
    Sub.fromStream("tick", s)

final case class Model(timestamp: Option[FiniteDuration])

enum Msg derives CanEqual:
  case NoOp
  case Tick(t: FiniteDuration)
