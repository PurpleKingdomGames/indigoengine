package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import indigo.*

object ScenesList:

  val scenes: NonEmptyBatch[Scene[SandboxGameModel]] =
    NonEmptyBatch(
      OriginalScene,
      ShapesScene,
      LightsScene,
      RefractionScene,
      LegacyEffectsScene,
      BoundsScene,
      CameraScene,
      TextureTileScene,
      ConfettiScene,
      MutantsScene,
      CratesScene,
      ClipScene,
      TextScene,
      BoxesScene,
      ManyEventHandlers,
      TimelineScene,
      UltravioletScene,
      PointersScene,
      BoundingCircleScene,
      LineReflectionScene,
      CameraWithCloneTilesScene,
      PathFindingScene,
      CaptureScreenScene,
      NineSliceScene,
      SfxScene,
      ComponentUIScene,
      ComponentUIScene2,
      WindowsScene,
      MeshScene,
      WaypointScene,
      ActorPoolScene,
      ActorPoolPhysicsScene,
      PerformerScene,
      PerformerPhysicsScene,
      ViewportResizeScene,
      MultiPointScene,
      BgAudioScene,
      MagnificationScene,
      LocaleScene
    )
