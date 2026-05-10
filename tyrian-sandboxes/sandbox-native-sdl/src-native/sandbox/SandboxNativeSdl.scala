package sandbox

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import cats.effect.IO

import indigoengine.shared.collections.Batch

import tyrian.GlobalMsg
import tyrian.Watcher
import tyrian.platform.Cmd
import tyrian.sdl.SdlApp
import tyrian.sdl.SdlContext
import tyrian.sdl.SdlMsg
import tyrian.sdl.facades.gl.GL.*
import tyrian.sdl.facades.gl.GLConstants.*
import tyrian.syntax.*

final case class SandboxModel(program: UInt, vao: UInt, ticks: Long)

enum Msg extends GlobalMsg derives CanEqual:
  case Tick
  case NoOp

object SandboxNativeSdl extends SdlApp[SandboxModel]:

  override def title: String = "Tyrian SDL Sandbox"
  override def width: Int    = 400
  override def height: Int   = 400

  def init(args: Array[String]): (SandboxModel, Cmd[IO, GlobalMsg]) =
    val program = Shaders.createProgram(Shaders.vertSrc, Shaders.fragSrc)
    val vao     = makeVao()
    val cmd: Cmd[IO, GlobalMsg] =
      Cmd.SideEffect(IO.println("Tyrian SDL sandbox starting"))
    (SandboxModel(program, vao, 0L), cmd)

  def update(model: SandboxModel): GlobalMsg => (SandboxModel, Cmd[IO, GlobalMsg]) =
    case Msg.Tick =>
      val nextTicks = model.ticks + 1L
      val cmd: Cmd[IO, GlobalMsg] =
        Cmd.SideEffect(IO.println(s"tick $nextTicks"))
      (model.copy(ticks = nextTicks), cmd)

    case Msg.NoOp =>
      (model, Cmd.None)

    case SdlMsg.Quit =>
      (model, Cmd.SideEffect(IO.println("SDL quit received")))

    case SdlMsg.Other(_) =>
      (model, Cmd.None)

    case _ =>
      (model, Cmd.None)

  override def watchers(model: SandboxModel): Batch[Watcher] =
    Batch(Watcher.every(1.second, _ => Msg.Tick))

  def render(model: SandboxModel, ctx: SdlContext): Unit =
    val phase = ((model.ticks % 6L).toFloat) / 6.0f
    glClearColor(phase, 0.2f, 1.0f - phase, 1.0f)
    glClear(GL_COLOR_BUFFER_BIT)
    glUseProgram(model.program)
    glBindVertexArray(model.vao)
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

  private def makeVao(): UInt =
    val vaoPtr = stackalloc[UInt]()
    glGenVertexArrays(1, vaoPtr)
    val vaoId = !vaoPtr
    glBindVertexArray(vaoId)
    vaoId
