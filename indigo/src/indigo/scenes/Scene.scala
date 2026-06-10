package indigo.scenes

import indigo.core.Outcome
import indigo.core.events.EventFilters
import indigo.core.events.GlobalEvent
import indigo.scenegraph.Layer
import indigo.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigoengine.shared.collections.Batch
import indigoengine.shared.optics.Lens

/** Describes the functions that a valid scene must implement.
  */
trait Scene[GameModel] derives CanEqual:
  type SceneModel

  def name: SceneName
  def modelLens: Lens[GameModel, SceneModel]
  def eventFilters: EventFilters
  def subSystems: Set[SubSystem[GameModel]]

  def updateModel(context: SceneContext, model: SceneModel): GlobalEvent => Outcome[SceneModel]

  def present(
      context: SceneContext,
      model: SceneModel
  ): Outcome[SceneUpdateFragment]

object Scene {

  def updateModel[GameModel](
      scene: Scene[GameModel],
      context: SceneContext,
      gameModel: GameModel
  ): GlobalEvent => Outcome[GameModel] =
    e =>
      scene
        .updateModel(context, scene.modelLens.get(gameModel))(e)
        .map(scene.modelLens.set(gameModel, _))

  def updateView[GameModel](
      scene: Scene[GameModel],
      context: SceneContext,
      model: GameModel
  ): Outcome[SceneUpdateFragment] =
    scene.present(context, scene.modelLens.get(model))

  def empty[GameModel]: Scene[GameModel] =
    new Scene[GameModel] {
      type SceneModel = Unit

      val sceneFragment =
        Outcome(SceneUpdateFragment(Batch.empty[Layer]))

      val modelOutcome = Outcome(())

      val name: SceneName =
        SceneName("empty-scene")

      val modelLens: Lens[GameModel, Unit] =
        Lens.unit

      val eventFilters: EventFilters =
        EventFilters.BlockAll

      val subSystems: Set[SubSystem[GameModel]] =
        Set()

      def updateModel(
          context: SceneContext,
          model: Unit
      ): GlobalEvent => Outcome[Unit] =
        _ => modelOutcome

      def present(
          context: SceneContext,
          model: Unit
      ): Outcome[SceneUpdateFragment] = sceneFragment
    }
}
