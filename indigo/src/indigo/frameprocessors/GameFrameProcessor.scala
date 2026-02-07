package indigo.frameprocessors

import indigo.core.Outcome
import indigo.core.events.EventFilters
import indigo.core.events.GlobalEvent
import indigo.frameprocessors.StandardFrameProcessorFunctions
import indigo.gameengine.FrameProcessor
import indigo.scenegraph.SceneUpdateFragment
import indigo.scenes.SceneManager
import indigo.shared.Context
import indigo.shared.subsystems.SubSystemContext.*
import indigo.shared.subsystems.SubSystemsRegister
import indigoengine.shared.collections.Batch

final class GameFrameProcessor[StartUpData, Model](
    val subSystemsRegister: SubSystemsRegister[Model],
    val sceneManager: SceneManager[StartUpData, Model],
    val eventFilters: EventFilters,
    val modelUpdate: (Context[StartUpData], Model) => GlobalEvent => Outcome[Model],
    val _viewUpdate: (Context[StartUpData], Model) => Outcome[SceneUpdateFragment]
) extends FrameProcessor[StartUpData, Model]
    with StandardFrameProcessorFunctions[StartUpData, Model, Unit]:

  def viewUpdate: (Context[StartUpData], Model) => Outcome[SceneUpdateFragment] =
    (ctx, m) => _viewUpdate(ctx, m)

  def run(
      model: => Model,
      globalEvents: Batch[GlobalEvent],
      context: => Context[StartUpData]
  ): Outcome[(Model, SceneUpdateFragment)] = {

    val processSceneView: Model => Outcome[SceneUpdateFragment] = m =>
      Outcome.merge(
        processView(context, m),
        sceneManager.updateView(context, m)
      )(_ |+| _)

    Outcome.join(
      for {
        m  <- processModel(context, model, globalEvents)
        sm <- processSceneModel(context, m, globalEvents)
        e  <- processSubSystems(context, m, globalEvents).eventsAsOutcome
        v  <- processSceneView(sm)
      } yield Outcome((sm, v), e)
    )
  }

  def processSceneModel(
      context: Context[StartUpData],
      model: Model,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[Model] =
    globalEvents
      .map(sceneManager.eventFilters.modelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMap { next =>
          sceneManager.updateModel(context, next)(e)
        }
      }

  def processSubSystems(
      context: Context[StartUpData],
      model: Model,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[Unit] =
    Outcome.merge(
      subSystemsRegister.update(context.forSubSystems, model, globalEvents.toJSArray),
      sceneManager.updateSubSystems(context.forSubSystems, model, globalEvents)
    )((_, _) => ())
