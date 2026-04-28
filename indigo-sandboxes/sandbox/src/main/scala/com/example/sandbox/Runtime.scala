package com.example.sandbox

import indigo.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime:

  def game: Game[?, ?, ?] =
    SandboxGame()

  def settings: Indigo.Settings =
    Indigo.Settings.default
      .withFrameRatePolicy(FrameRatePolicy.Skip(FPS.`60`))
