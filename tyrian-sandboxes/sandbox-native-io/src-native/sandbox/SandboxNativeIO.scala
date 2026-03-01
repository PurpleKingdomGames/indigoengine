package sandbox

import cats.effect.IO
import tyrian.classic.Terminal
import tyrian.classic.TyrianIOApp
import tyrian.classic.cmds.Logger
import tyrian.classic.syntax.*
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

    case Msg.Log(msg) =>
      model -> Logger.info(msg)

    case Msg.NoOp =>
      model -> Cmd.None

  def view(model: Model): Terminal[Msg] =
    model.timestamp match
      case None =>
        Terminal.NoOp()

      case Some(t) =>
        Terminal.Print(s"...tick (${t.toString()})")

  def subscriptions(model: Model): Sub[IO, Msg] =
    val sub1 =
      val d: FiniteDuration = 1.second
      val s: fs2.Stream[IO, Msg] = fs2.Stream.awakeEvery[IO](d).map { t =>
        Msg.Tick(t)
      }
      Sub.fromStream("tick", s)

    val sub2a =
      Sub.emit[IO, Msg](Msg.Log("Immediately logged..."), "right-now")

    val sub2b =
      Sub.timeout[IO, Msg](3.seconds, Msg.Log("...logged 3 seconds later."))

    Sub.Combine(sub1, Sub.Combine(sub2a, sub2b))

final case class Model(timestamp: Option[FiniteDuration])

enum Msg derives CanEqual:
  case NoOp
  case Tick(t: FiniteDuration)
  case Log(msg: String)
