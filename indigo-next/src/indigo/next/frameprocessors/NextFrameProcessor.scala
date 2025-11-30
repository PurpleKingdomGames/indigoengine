package indigo.next.frameprocessors

import indigo.core.Outcome
import indigo.core.events.EventFilters
import indigo.core.events.GlobalEvent
import indigo.frameprocessors.StandardFrameProcessorFunctions
import indigo.gameengine.FrameProcessor
import indigo.next.scenes.SceneManager
import indigo.scenegraph.SceneUpdateFragment
import indigo.shared.Context
import indigo.shared.subsystems.SubSystemContext.*
import indigo.shared.subsystems.SubSystemsRegister
import indigoengine.shared.collections.Batch

final class NextFrameProcessor[StartUpData, Model](
    val subSystemsRegister: SubSystemsRegister[Model],
    val sceneManager: SceneManager[StartUpData, Model],
    val eventFilters: EventFilters,
    val modelUpdate: (Context[StartUpData], Model) => GlobalEvent => Outcome[Model],
    val _viewUpdate: (Context[StartUpData], Model) => Outcome[SceneUpdateFragment]
) extends FrameProcessor[StartUpData, Model, Unit]
    with StandardFrameProcessorFunctions[StartUpData, Model, Unit]:

  def viewModelUpdate: (Context[StartUpData], Model, Unit) => GlobalEvent => Outcome[Unit] =
    (_, _, _) => _ => Outcome(())

  def viewUpdate: (Context[StartUpData], Model, Unit) => Outcome[SceneUpdateFragment] =
    (ctx, m, _) => _viewUpdate(ctx, m)

  def run(
      model: => Model,
      viewModel: => Unit,
      globalEvents: Batch[GlobalEvent],
      context: => Context[StartUpData]
  ): Outcome[(Model, Unit, SceneUpdateFragment)] = {

    val processSceneViewModel: (Model, Unit) => Outcome[Unit] = (_, _) => Outcome(())

    val processSceneView: (Model, Unit) => Outcome[SceneUpdateFragment] = (m, vm) =>
      Outcome.merge(
        processView(context, m, vm),
        sceneManager.updateView(context, m)
      )(_ |+| _)

    Outcome.join(
      for {
        m   <- processModel(context, model, globalEvents)
        sm  <- processSceneModel(context, m, globalEvents)
        vm  <- processViewModel(context, sm, viewModel, globalEvents)
        svm <- processSceneViewModel(sm, vm)
        e   <- processSubSystems(context, m, globalEvents).eventsAsOutcome
        v   <- processSceneView(sm, svm)
      } yield Outcome((sm, svm, v), e)
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
