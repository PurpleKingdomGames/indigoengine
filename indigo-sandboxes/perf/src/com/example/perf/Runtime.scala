package com.example.perf

import indigo.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime:

  def game: Game[?, ?, ?] =
    PerfGame()

  def settings: Indigo.Settings =
    Indigo.Settings.default
      .withFrameRatePolicy(FrameRatePolicy.Unlimited)
      .allowContextMenu
