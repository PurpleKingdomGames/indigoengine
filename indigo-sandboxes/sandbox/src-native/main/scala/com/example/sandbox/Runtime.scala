package com.example.sandbox

import indigo.BasicGameRuntime

object Runtime extends BasicGameRuntime(SandboxGame()):

  val title: String = "Indigo SDL Native Sandbox"
  val width: Int    = 800
  val height: Int   = 600
