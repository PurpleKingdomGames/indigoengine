package indigo.next.scenes

import indigo.core.Outcome
import indigo.core.events.EventFilters
import indigo.core.events.GlobalEvent
import indigo.scenegraph.Layer
import indigo.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigoengine.shared.collections.Batch
import indigoengine.shared.lenses.Lens

/** Describes the functions that a valid scene must implement.
  */
trait Scene[StartUpData, GameModel] derives CanEqual:
  type SceneModel

  def name: SceneName
  def modelLens: Lens[GameModel, SceneModel]
  def eventFilters: EventFilters
  def subSystems: Set[SubSystem[GameModel]]

  def updateModel(context: SceneContext[StartUpData], model: SceneModel): GlobalEvent => Outcome[SceneModel]

  def present(
      context: SceneContext[StartUpData],
      model: SceneModel
  ): Outcome[SceneUpdateFragment]

object Scene {

  def updateModel[SD, GM](
      scene: Scene[SD, GM],
      context: SceneContext[SD],
      gameModel: GM
  ): GlobalEvent => Outcome[GM] =
    e =>
      scene
        .updateModel(context, scene.modelLens.get(gameModel))(e)
        .map(scene.modelLens.set(gameModel, _))

  def updateView[SD, GM](
      scene: Scene[SD, GM],
      context: SceneContext[SD],
      model: GM
  ): Outcome[SceneUpdateFragment] =
    scene.present(context, scene.modelLens.get(model))

  def empty[SD, GM]: Scene[SD, GM] =
    new Scene[SD, GM] {
      type SceneModel = Unit

      val sceneFragment =
        Outcome(SceneUpdateFragment(Batch.empty[Layer]))

      val modelOutcome = Outcome(())

      val name: SceneName =
        SceneName("empty-scene")

      val modelLens: Lens[GM, Unit] =
        Lens.unit

      val eventFilters: EventFilters =
        EventFilters.BlockAll

      val subSystems: Set[SubSystem[GM]] =
        Set()

      def updateModel(
          context: SceneContext[SD],
          model: Unit
      ): GlobalEvent => Outcome[Unit] =
        _ => modelOutcome

      def present(
          context: SceneContext[SD],
          model: Unit
      ): Outcome[SceneUpdateFragment] = sceneFragment
    }
}
