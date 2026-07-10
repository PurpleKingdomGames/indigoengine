package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import indigo.*
import indigo.scenes.*

object MagnificationScene extends Scene[SandboxGameModel] {

  type SceneModel = SandboxGameModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("magnification scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext,
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def present(
      context: SceneContext,
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] = {
    val graphic =
      Graphic(64, 64, Material.Bitmap(SandboxAssets.cratesDiffuseName))

    Outcome(
      SceneUpdateFragment.empty
        .addLayers(
          LayerEntry(LayerKey("a"), Layer.Content(graphic.moveTo(10, 10)), Magnification.x1),
          LayerEntry(
            LayerKey("b"),
            Layer.Content(graphic.moveTo(10 + 60, 10)),
            Magnification.x2
          ),
          LayerEntry(
            LayerKey("c"),
            Layer.Stack(
              Layer.Stack(
                Layer.Content(graphic.moveTo(10, 10 + 60))
              )
            ),
            Magnification.x2
          ),
          LayerEntry(
            LayerKey("d"),
            Layer.Content(graphic.moveTo(10 + 30, 10 + 30)),
            // Layer.Content(graphic),
            Magnification.x4
          )
        )
    )
  }

}
