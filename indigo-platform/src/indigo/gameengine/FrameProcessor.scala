package indigo.gameengine

import indigo.core.Outcome
import indigo.core.events.GlobalEvent
import indigo.scenegraph.SceneUpdateFragment
import indigo.shared.Context
import indigoengine.shared.collections.Batch

trait FrameProcessor[StartUpData, Model]:
  def run(
      model: => Model,
      globalEvents: Batch[GlobalEvent],
      context: => Context[StartUpData]
  ): Outcome[(Model, SceneUpdateFragment)]
