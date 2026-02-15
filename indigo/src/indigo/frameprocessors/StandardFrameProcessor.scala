package indigo.frameprocessors

import indigo.core.Outcome
import indigo.core.events.EventFilters
import indigo.core.events.GlobalEvent
import indigo.gameengine.FrameProcessor
import indigo.scenegraph.SceneUpdateFragment
import indigo.shared.Context
import indigo.shared.subsystems.SubSystemContext.*
import indigo.shared.subsystems.SubSystemsRegister
import indigoengine.shared.collections.Batch

final class StandardFrameProcessor[StartUpData, Model, ViewModel](
    val subSystemsRegister: SubSystemsRegister[Model],
    val eventFilters: EventFilters,
    val modelUpdate: (Context[StartUpData], Model) => GlobalEvent => Outcome[Model],
    val viewModelUpdate: (Context[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel],
    val viewUpdate: (Context[StartUpData], Model, ViewModel) => Outcome[SceneUpdateFragment]
) extends FrameProcessor[StartUpData, Model, ViewModel]
    with StandardFrameProcessorFunctions[StartUpData, Model, ViewModel]:

  def run(
      model: => Model,
      viewModel: => ViewModel,
      globalEvents: Batch[GlobalEvent],
      context: => Context[StartUpData]
  ): Outcome[(Model, ViewModel, SceneUpdateFragment)] =
    Outcome.join(
      for {
        m  <- processModel(context, model, globalEvents)
        vm <- processViewModel(context, m, viewModel, globalEvents)
        e  <- subSystemsRegister.update(context.forSubSystems, m, globalEvents).eventsAsOutcome
        v  <- processView(context, m, vm)
      } yield Outcome((m, vm, v), e)
    )
