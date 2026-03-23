package indigo.render.opengl

import indigo.core.assets.AssetType
import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.render.EmitGlobalEvent
import indigo.render.Renderer
import indigo.render.RendererConfig
import indigo.render.ScreenCaptureConfig
import indigo.render.facades.sdl.SDL.*
import indigo.render.pipeline.datatypes.ProcessedSceneData
import indigo.shaders.RawShaderCode
import indigoengine.shared.aliases.Seconds
import indigoengine.shared.collections.Batch

import scala.annotation.nowarn
// import indigo.render.facades.sdl.SDLConstants.*
// import indigo.render.facades.gl.GL.*
// import indigo.render.facades.gl.GLConstants.*

@nowarn // TODO: Refine / Remove
final class OpenGLRenderer(
    config: RendererConfig,
    ctx: SDL_GLContext,
    globalEventStream: EmitGlobalEvent
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
