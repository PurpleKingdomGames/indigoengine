package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import indigo.*
import indigo.scenes.*

object ViewportResizeScene extends Scene[SandboxStartupData, SandboxGameModel] {

  type SceneModel = SandboxGameModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("viewport resize scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayers(
          Layer(
            Graphic(
              context.frame.viewportSize,
              Material.Bitmap(SandboxAssets.nineSlice).nineSlice(Rectangle(16, 16, 32, 32))
            )
          )
        )
    )

}
