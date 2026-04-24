package com.example.perf

import indigo.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime:

  def game: Game[?, ?, ?] =
    PerfGame()

  def frameRatePolicy: FrameRatePolicy =
    FrameRatePolicy.Unlimited
