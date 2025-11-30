package indigo.frameprocessors

import indigo.core.Outcome
import indigo.core.events.EventFilters
import indigo.core.events.GlobalEvent
import indigo.scenegraph.SceneUpdateFragment
import indigo.shared.Context
import indigo.shared.subsystems.SubSystemContext.*
import indigo.shared.subsystems.SubSystemsRegister
import indigoengine.shared.collections.Batch

trait StandardFrameProcessorFunctions[StartUpData, Model, ViewModel]:
  def subSystemsRegister: SubSystemsRegister[Model]
  def eventFilters: EventFilters
  def modelUpdate: (Context[StartUpData], Model) => GlobalEvent => Outcome[Model]
  def viewModelUpdate: (Context[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel]
  def viewUpdate: (Context[StartUpData], Model, ViewModel) => Outcome[SceneUpdateFragment]

  def processModel(
      context: Context[StartUpData],
      model: Model,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[Model] =
    globalEvents
      .map(eventFilters.modelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMap { next =>
          modelUpdate(context, next)(e)
        }
      }

  def processViewModel(
      context: Context[StartUpData],
      model: Model,
      viewModel: ViewModel,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[ViewModel] =
    globalEvents
      .map(eventFilters.viewModelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(viewModel)) { (acc, e) =>
        acc.flatMap { next =>
          viewModelUpdate(context, model, next)(e)
        }
      }

  def processView(
      context: Context[StartUpData],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome.merge(
      viewUpdate(context, model, viewModel),
      subSystemsRegister.present(context.forSubSystems, model)
    )(_ |+| _)
