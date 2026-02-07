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
  def viewUpdate: (Context[StartUpData], Model) => Outcome[SceneUpdateFragment]

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

  def processView(
      context: Context[StartUpData],
      model: Model
  ): Outcome[SceneUpdateFragment] =
    Outcome.merge(
      viewUpdate(context, model),
      subSystemsRegister.present(context.forSubSystems, model)
    )(_ |+| _)
