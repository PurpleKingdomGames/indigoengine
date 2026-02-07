package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.shaders.*
import indigo.*
import indigo.scenes.*

object NoiseScene extends Scene[SandboxStartupData, SandboxGameModel] {

  type SceneModel = SandboxGameModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("noise")

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
      SceneUpdateFragment(
        Layer(
          BlankEntity(100, 100, ShaderData(CellularNoiseShader.shader.id))
            .moveTo(0, 0),
          BlankEntity(100, 100, ShaderData(PerlinNoiseShader.shader.id))
            .moveTo(100, 0),
          BlankEntity(100, 100, ShaderData(GradientNoiseShader.shader.id))
            .moveTo(200, 0),
          BlankEntity(100, 100, ShaderData(SimplexNoiseShader.shader.id))
            .moveTo(0, 100),
          BlankEntity(100, 100, ShaderData(WhiteNoiseShader.shader.id))
            .moveTo(100, 100)
        ).withMagnification(1)
      )
    )

}
