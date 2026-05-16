package indigo.render

import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.render.pipeline.datatypes.ProcessedSceneData
import indigo.shaders.RawShaderCode
import indigoengine.shared.datatypes.Seconds

trait Renderer[Ctx]:
  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: CheapMatrix4

  def init(ctx: Ctx, shaders: Set[RawShaderCode]): Unit
  def resize(ctx: Ctx): Unit
  def drawScene(ctx: Ctx, sceneData: ProcessedSceneData, runningTime: Seconds): Unit
  def dispose(ctx: Ctx): Unit

object Renderer:

  def blackHole =
    new Renderer[Unit] {
      def screenWidth: Int                           = 0
      def screenHeight: Int                          = 0
      def orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity

      def init(ctx: Unit, shaders: Set[RawShaderCode]): Unit                              = ()
      def resize(ctx: Unit): Unit                                                         = ()
      def drawScene(ctx: Unit, sceneData: ProcessedSceneData, runningTime: Seconds): Unit = ()
      def dispose(ctx: Unit): Unit                                                        = ()
    }
