package com.example.sandbox

import com.example.sandbox.scenes.*
import indigo.*

final class SandboxGame extends Game[SandboxBootData, SandboxStartupData, SandboxGameModel]:

  val gameId: GameId = GameId("sandbox-native")

  def initialScene(bootData: SandboxBootData): Option[SceneName] =
    Some(ShapesScene.name)

  def scenes(bootData: SandboxBootData): NonEmptyBatch[Scene[SandboxStartupData, SandboxGameModel]] =
    ScenesList.scenes

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(args: Array[String]): Outcome[BootResult[SandboxBootData, SandboxGameModel]] =
    Outcome(
      BootResult(
        EngineConfig.default,
        SandboxBootData()
      )
    )

  def setup(
      bootData: SandboxBootData,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[SandboxStartupData]] =
    Outcome(Startup.Success(SandboxStartupData()))

  def initialModel(startupData: SandboxStartupData): Outcome[SandboxGameModel] =
    Outcome(SandboxModel.initialModel)

  def updateModel(
      context: Context,
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    case _ => Outcome(model)

  def present(
      context: Context,
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

final case class SandboxBootData()
final case class SandboxStartupData()

final case class Log(message: String) extends GlobalEvent
