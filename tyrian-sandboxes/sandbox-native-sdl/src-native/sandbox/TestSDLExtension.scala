package sandbox

import indigoengine.sdl.facades.gl.GL.*
import indigoengine.sdl.facades.gl.GLConstants.*
import tyrian.*
import tyrian.extensions.Extension

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

object TestSDLExtension extends Extension.Graphical[SDLContext]:

  type ExtensionModel = SandboxModel

  def id: ExtensionId =
    ExtensionId("SDL test extension")

  def init: Result[ExtensionModel] =
    val program = Shaders.createProgram(Shaders.vertSrc, Shaders.fragSrc)
    val vao     = makeVao()

    Result(SandboxModel(program, vao, 0L))

  def update(model: SandboxModel): GlobalMsg => Result[ExtensionModel] =
    case _ => Result(model)

  def view(model: SandboxModel): TerminalFragment =
    TerminalFragment.empty

  def draw(context: SDLContext, runningTime: Seconds, timeDelta: Seconds, model: SandboxModel): Unit =
    val phase = ((runningTime.toMillis.toLong / 1000L) % 6L).toFloat / 6.0f

    glClearColor(phase, 0.2f, 1.0f - phase, 1.0f)
    glClear(GL_COLOR_BUFFER_BIT)
    glUseProgram(model.program)
    glBindVertexArray(model.vao)
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

  def watchers(model: SandboxModel): Batch[Watcher] =
    Batch.empty

  private def makeVao(): UInt =
    val vaoPtr = stackalloc[UInt]()
    glGenVertexArrays(1, vaoPtr)
    val vaoId = !vaoPtr
    glBindVertexArray(vaoId)
    vaoId
