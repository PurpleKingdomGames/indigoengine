package example

import indigo.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object Runtime extends BasicGameRuntime:

  def game: Game[?, ?, ?] =
    IndigoPhysics()

  def frameRatePolicy: FrameRatePolicy =
    FrameRatePolicy.Unlimited
