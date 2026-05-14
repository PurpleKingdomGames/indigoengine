package indigo.render.opengl

import indigo.core.assets.AssetType
import indigo.core.config.EngineConfig
import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.core.render.ScreenCaptureConfig
import indigo.render.Renderer
import indigo.render.pipeline.datatypes.ProcessedSceneData
import indigo.shaders.RawShaderCode
import indigoengine.sdl.facades.sdl.SDL.*
import indigoengine.shared.aliases.Seconds
import indigoengine.shared.collections.Batch

import scala.annotation.nowarn

@nowarn // TODO: Remove nowarn once the implementation is in place.
final class OpenGLRenderer(
    config: EngineConfig,
    ctx: SDL_GLContext
) extends Renderer:

  def screenWidth: Int =
    0

  def screenHeight: Int =
    0

  def orthographicProjectionMatrix: CheapMatrix4 =
    CheapMatrix4.identity

  def init(shaders: Set[RawShaderCode]): Unit =
    ()

  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit =
    ()

  def captureScreen(captureOptions: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]] =
    Batch(
      Left("No renderer available")
    )

  def dispose(): Unit =
    ()

  def resize(width: Int, height: Int): Unit =
    ()
