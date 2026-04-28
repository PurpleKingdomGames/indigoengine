package com.example.shader

import indigo.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime:

  def game: Game[?, ?, ?] =
    ShaderGame()

  def settings: Indigo.Settings =
    Indigo.Settings.default
      .withFrameRatePolicy(FrameRatePolicy.Unlimited)
