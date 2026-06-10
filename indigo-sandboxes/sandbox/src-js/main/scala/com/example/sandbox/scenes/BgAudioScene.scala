package com.example.sandbox.scenes

import com.example.sandbox.Fonts
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import indigo.*
import indigo.scenes.*

import scala.annotation.nowarn

@nowarn("msg=unused")
object BgAudioScene extends Scene[SandboxGameModel]:

  type SceneModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("bg audio")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext,
      model: Unit
  ): GlobalEvent => Outcome[Unit] =
    case _ => Outcome(model)

  val textMaterial =
    SandboxAssets.fontMaterial.toBitmap

  def present(
      context: SceneContext,
      model: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Layer(
          Batch(
            Text("Music should be playing", Fonts.fontKey, textMaterial)
              .moveTo(10, 10)
          )
        )
      )
        .withAudio(SandboxAssets.bgMusicSceneAudio)
    )
