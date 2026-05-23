package sandbox

import cats.effect.IO
import indigoengine.shared.collections.Batch
import tyrian.*
import tyrian.GlobalMsg
import tyrian.SDLApp
import tyrian.SDLMsg
import tyrian.SDLWatcher.*
import tyrian.extensions.Extension
import tyrian.platform.Cmd

object SandboxNativeSDL extends SDLApp[SandboxModel]:

  val title: String = "Tyrian SDL Sandbox"
  val width: Int    = 400
  val height: Int   = 400

  def init(args: Array[String]): Result[SandboxModel] =
    val cmd: Cmd[IO, GlobalMsg] =
      Cmd.SideEffect(IO.println("Tyrian SDL sandbox starting"))

    Result(SandboxModel(0L))
      .addCmds(cmd)

  def update(model: SandboxModel): GlobalMsg => Result[SandboxModel] =
    case Msg.Tick(t) =>
      val nextTicks = model.ticks + 1L
      val cmd: Cmd[IO, GlobalMsg] =
        if nextTicks % 60L == 0L then Cmd.SideEffect(IO.println(s"tick $nextTicks (t=${t.toMillis} ms)"))
        else Cmd.None

      Result(model.copy(ticks = nextTicks))
        .addCmds(cmd)

    case Msg.Shutdown =>
      Result(model)
        .addCmds(Cmd.SideEffect(IO.println("SDL quit received")))

    case Msg.NoOp =>
      Result(model)

  def view(model: SandboxModel): TerminalFragment =
    TerminalFragment.empty

  def watchers(model: SandboxModel): Batch[Watcher] =
    Batch(
      Watcher.fromSDLMsg("sdl-msg-handler") {
        case SDLMsg.Frame(t) => Some(Msg.Tick(t))
        case SDLMsg.Quit     => Some(Msg.Shutdown)
        case SDLMsg.Other(_) => Some(Msg.NoOp)
      }
    )

  def extensions(args: Array[String], model: SandboxModel): Set[Extension[SDLContext, TerminalFragment]] =
    Set(TestSDLExtension)

final case class SandboxModel(ticks: Long)

enum Msg extends GlobalMsg derives CanEqual:
  case Tick(runningTime: Seconds)
  case Shutdown
  case NoOp
