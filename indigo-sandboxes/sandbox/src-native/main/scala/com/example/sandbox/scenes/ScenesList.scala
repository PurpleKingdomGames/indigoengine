package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import indigo.*

object ScenesList:

  val scenes: NonEmptyBatch[Scene[SandboxGameModel]] =
    NonEmptyBatch(
      ShapesScene
    )
