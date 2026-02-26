package sandbox

import cats.effect.IO
import tyrian.classic.NativeView
import tyrian.classic.TyrianIOApp
import tyrian.platform.Cmd
import tyrian.platform.Sub

import scala.concurrent.duration.*

object SandboxNative extends TyrianIOApp[Msg, Model]:

  def init(args: Array[String]): (Model, Cmd[IO, Msg]) =
    val cmd = Cmd.SideEffect[IO](println("Starting my command line app!"))

    Model(System.currentTimeMillis()) -> cmd

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case _ =>
      val cmd = Cmd.None
      val m   = model.copy(timestamp = System.currentTimeMillis())

      (m, cmd)

  def view(model: Model): NativeView[Msg] =
    NativeView.Print(s"...tick (${model.timestamp.toString()})")

  def subscriptions(model: Model): Sub[IO, Msg] =
    val d: FiniteDuration = 1.second
    val s: fs2.Stream[IO, Msg] = fs2.Stream.awakeEvery[IO](d).map { t =>
      Msg.Tick(t)
    }
    Sub.fromStream("tick", s)

final case class Model(timestamp: Long)

enum Msg:
  case NoOp
  case Tick(t: FiniteDuration)
