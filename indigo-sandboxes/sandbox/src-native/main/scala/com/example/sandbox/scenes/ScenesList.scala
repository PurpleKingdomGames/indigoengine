package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import indigo.*

object ScenesList:

  val scenes: NonEmptyBatch[Scene[SandboxStartupData, SandboxGameModel]] =
    NonEmptyBatch(
      ShapesScene
    )
