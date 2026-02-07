package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.shaders.*
import indigo.*
import indigo.scenes.*

object ShadersScene extends Scene[SandboxStartupData, SandboxGameModel] {

  type SceneModel = SandboxGameModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("custom shaders")

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
          BlankEntity(100, 100, ShaderData(BoxShader.shader.id))
            .moveTo(0, 0),
          BlankEntity(100, 100, ShaderData(CircleShader.shader.id))
            .moveTo(100, 0),
          BlankEntity(100, 100, ShaderData(HexagonShader.shader.id))
            .moveTo(200, 0),
          BlankEntity(100, 100, ShaderData(SegmentShader.shader.id))
            .moveTo(0, 100),
          BlankEntity(100, 100, ShaderData(StarShader.shader.id))
            .moveTo(100, 100),
          BlankEntity(100, 100, ShaderData(TriangleShader.shader.id))
            .moveTo(200, 100)
        ).withMagnification(1)
      )
    )

}
