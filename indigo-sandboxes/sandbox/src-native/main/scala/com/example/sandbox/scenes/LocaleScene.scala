package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import indigo.*
import indigo.scenes.*

/** The native platform has no renderer, asset loading or pointer / keyboard input yet, so the locale strings cannot be
  * drawn, nor a window opened by clicking them. Until those land, this scene logs the locales the service reports. See
  * the JS sandbox for the scene this is standing in for.
  */
object LocaleScene extends Scene[SandboxGameModel]:

  type SceneModel = SandboxGameModel

  val name: SceneName =
    SceneName("LocaleScene")

  val modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext,
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    case FrameTick if !model.loggedLocales =>
      val current =
        context.services.locale.current.map(_.toString).getOrElse("Unknown locale")

      val preferred =
        context.services.locale.preferred.map(_.toString).toList.mkString(", ")

      IndigoLogger.info("Current locale: " + current)
      IndigoLogger.info("Preferred locales: " + preferred)

      Outcome(model.copy(loggedLocales = true))

    case _ =>
      Outcome(model)

  def present(
      context: SceneContext,
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
