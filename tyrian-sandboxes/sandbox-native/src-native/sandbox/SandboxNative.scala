package sandbox

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import tyrian.NativeView
import tyrian.TyrianApp
import tyrian.platform.Cmd
import tyrian.platform.Sub

import scala.concurrent.duration.*

// TODO: So actually, we probably want IO and ZIO versions as usual.
object SandboxNative extends TyrianApp[IO, Msg, Model]:

  val run: IO[Nothing] => Unit =
    _.unsafeRunSync()

  @main
  def go(): Unit =
    launch(Map())

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
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
