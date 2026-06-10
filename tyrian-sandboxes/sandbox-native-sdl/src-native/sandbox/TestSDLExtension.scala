package sandbox

import indigoengine.sdl.facades.gl.GL.*
import indigoengine.sdl.facades.gl.GLConstants.*
import tyrian.*

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

object TestSDLExtension extends Extension.Graphical[SDLContext, TerminalFragment]:

  type ExtensionModel = ExtModel

  def id: ExtensionId =
    ExtensionId("SDL test extension")

  def init: Result[ExtensionModel] =
    val program = Zone { (z: Zone) ?=>
      Shaders.createProgram(Shaders.vertSrc, Shaders.fragSrc)
    }
    val vao = makeVao()

    Result(ExtModel(program, vao))

  def update(model: ExtModel): GlobalMsg => Result[ExtensionModel] =
    case _ => Result(model)

  def view(model: ExtModel): TerminalFragment =
    TerminalFragment.empty

  def draw(context: SDLContext, runningTime: Seconds, model: ExtModel): ExtModel =
    val phase = ((runningTime.toMillis.toLong / 1000L) % 6L).toFloat / 6.0f

    glClearColor(phase, 0.2f, 1.0f - phase, 1.0f)
    glClear(GL_COLOR_BUFFER_BIT)
    glUseProgram(model.program)
    glBindVertexArray(model.vao)
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

    model

  def provideContext(model: ExtModel): Option[SDLContext] =
    None

  def watchers(model: ExtModel): Batch[Watcher] =
    Batch.empty

  def teardown: Unit =
    ()

  private def makeVao(): UInt =
    val vaoPtr = stackalloc[UInt]()
    glGenVertexArrays(1, vaoPtr)
    val vaoId = !vaoPtr
    glBindVertexArray(vaoId)
    vaoId

  final case class ExtModel(program: UInt, vao: UInt)
