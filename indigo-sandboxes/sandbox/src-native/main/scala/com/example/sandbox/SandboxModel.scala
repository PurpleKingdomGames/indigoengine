package com.example.sandbox

import com.example.sandbox.scenes.ActorPhysicsSceneModel
import com.example.sandbox.scenes.ActorSceneModel
import com.example.sandbox.scenes.CaptureScreenScene
import com.example.sandbox.scenes.ComponentUIScene2
import com.example.sandbox.scenes.ConfettiModel
import com.example.sandbox.scenes.PathFindingModel
import com.example.sandbox.scenes.PerformerPhysicsSceneModel
import com.example.sandbox.scenes.PerformerSceneModel
import com.example.sandbox.scenes.SfxComponents
import indigo.*
import indigo.syntax.*
import indigoextras.mesh.*
import indigoextras.ui.*

final case class SandboxGameModel(
    dude: DudeModel,
    saveLoadPhase: SaveLoadPhases,
    data: Option[String],
    confetti: ConfettiModel,
    pathfinding: PathFindingModel,
    rotation: Radians,
    num: Int,
    sfxComponents: ComponentGroup[Unit],
    components: ComponentGroup[Int],
    scrollPane: ScrollPane[ComponentList[Int], Int],
    button: Button[Int],
    meshData: MeshData,
    actorScene: ActorSceneModel,
    actorPhysicsScene: ActorPhysicsSceneModel,
    performerSceneModel: PerformerSceneModel,
    performerPhysicsSceneModel: PerformerPhysicsSceneModel,
    viewModel: SandboxViewModel,
    captureScreenScene: CaptureScreenScene.Model
)

object SandboxModel:

  def initialModel(startupData: SandboxStartupData): SandboxGameModel =
    val dice          = Dice.fromSeed(1)
    val offset        = Point(75, 75)
    val points        = List.fill(10)(SandboxModelShared.randomPoint(dice, offset)).toBatch
    val superTriangle = Triangle.encompassing(points.map(_.toVertex), 10)
    val mesh          = Mesh.fromVertices(points.map(_.toVertex), superTriangle)

    SandboxGameModel(
      DudeModel(startupData.dude, DudeIdle),
      SaveLoadPhases.NotStarted,
      None,
      ConfettiModel.empty,
      PathFindingModel.empty,
      Radians.zero,
      0,
      SfxComponents.components,
      SandboxModelShared.components,
      ComponentUIScene2.CustomComponents.pane,
      SandboxModelShared.customButton,
      MeshData(
        points,
        superTriangle,
        mesh
      ),
      ActorSceneModel.initial,
      ActorPhysicsSceneModel.initial,
      PerformerSceneModel.initial,
      PerformerPhysicsSceneModel.initial,
      SandboxViewModel(
        Point.zero,
        true
      ),
      CaptureScreenScene.Model(None, None, Point.zero)
    )
