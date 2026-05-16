package indigo.render.opengl

import indigo.core.config.EngineConfig
import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.render.Renderer
import indigo.render.pipeline.datatypes.ProcessedSceneData
import indigo.shaders.RawShaderCode
import indigoengine.shared.aliases.Seconds

import scala.annotation.nowarn

@nowarn // TODO: Remove nowarn once the implementation is in place.
final class OpenGLRenderer(
    config: EngineConfig
) extends Renderer[ContextAndSize]:

  def screenWidth: Int =
    0

  def screenHeight: Int =
    0

  def orthographicProjectionMatrix: CheapMatrix4 =
    CheapMatrix4.identity

  def init(ctx: ContextAndSize, shaders: Set[RawShaderCode]): Unit =
    ()

  def drawScene(ctx: ContextAndSize, sceneData: ProcessedSceneData, runningTime: Seconds): Unit =
    ()

  def dispose(ctx: ContextAndSize): Unit =
    ()

  def resize(ctx: ContextAndSize): Unit =
    ()
