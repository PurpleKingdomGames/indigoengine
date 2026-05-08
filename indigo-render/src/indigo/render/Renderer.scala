package indigo.render

import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.render.pipeline.datatypes.ProcessedSceneData
import indigo.shaders.RawShaderCode
import indigoengine.shared.datatypes.Seconds

trait Renderer:
  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: CheapMatrix4

  def init(shaders: Set[RawShaderCode]): Unit
  def resize(width: Int, height: Int): Unit
  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit

object Renderer:

  def blackHole =
    new Renderer {
      def screenWidth: Int                           = 0
      def screenHeight: Int                          = 0
      def orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity

      def init(shaders: Set[RawShaderCode]): Unit                              = ()
      def resize(width: Int, height: Int): Unit                                = ()
      def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = ()
    }
