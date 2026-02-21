package indigo.render

import indigo.core.assets.AssetType
import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.render.pipeline.ProcessedSceneData
import indigo.shaders.RawShaderCode
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Seconds

trait Renderer:
  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: CheapMatrix4

  def init(shaders: Set[RawShaderCode]): Unit
  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit

  /** Capture the screen as a number of images, each with the specified configuration
    *
    * @param captureConfig
    *   The configurations to use when capturing the screen
    * @return
    *   A batch containing either the captured images, or error messages
    */
  def captureScreen(captureConfig: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]]

object Renderer:

  def blackHole =
    new Renderer {
      def screenWidth: Int                           = 0
      def screenHeight: Int                          = 0
      def orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity

      def init(shaders: Set[RawShaderCode]): Unit                              = ()
      def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = ()
      def captureScreen(captureOptions: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]] = Batch(
        Left("No renderer available")
      )
    }
