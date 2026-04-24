package com.example.sandbox

import indigo.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime:

  def game: Game[?, ?, ?] =
    SandboxGame()

  def frameRatePolicy: FrameRatePolicy =
    FrameRatePolicy.Skip(FPS.`60`)
