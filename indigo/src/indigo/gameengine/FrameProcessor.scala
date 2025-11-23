package indigo.gameengine

import indigo.shared.Context
import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigoengine.shared.collections.Batch

trait FrameProcessor[StartUpData, Model, ViewModel]:
  def run(
      model: => Model,
      viewModel: => ViewModel,
      globalEvents: Batch[GlobalEvent],
      context: => Context[StartUpData]
  ): Outcome[(Model, ViewModel, SceneUpdateFragment)]
