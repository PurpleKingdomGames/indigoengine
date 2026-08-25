package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxGameModel
import indigo.*
import indigo.scenes.*

object QuadScene extends Scene[SandboxGameModel]:

  type SceneModel = SandboxGameModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("quads")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext,
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def present(
      context: SceneContext,
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] = {

    val squareSize: Size =
      val signal = Signal.SmoothPulse.map(d => (100 * d).toInt).affectTime(0.25).at(context.frame.time.running)
      Size(
        signal,
        99 - signal
      )

    val progressBar =
      Group(
        // Shadow
        Quad(
          Rectangle(Size(200, 12)),
          Fill.Color(RGBA.Black.withAlpha(0.4))
        ).moveBy(6, 6),
        // Border
        Quad(
          Rectangle(Size(200, 12)),
          Fill.Color(RGBA.Black)
        ),
        // Bar
        Quad(
          Rectangle(Size(200 - 34, 12 - 4)),
          Fill.LinearGradient(Point(0, 0), RGBA.Red, Point(0, 8), RGBA.Yellow)
        ).moveBy(2, 2)
      ).moveBy(120, 120)

    Outcome(
      SceneUpdateFragment(
        Constants.LayerKeys.game -> Layer
          .Content(
            Quad(
              Rectangle(Point(60, 180), squareSize),
              Fill.Color(RGBA.Red),
              Corners(10, 5, 30, 15)
            )
              .withRef(squareSize.toPoint / 2),
            Quad(Rectangle(10, 10, 100, 100), Fill.Color(RGBA.Green.withAlpha(0.5))),
            Quad(Rectangle(120, 10, 100, 100), Fill.LinearGradient(Point.zero, RGBA.Magenta, Point(100), RGBA.Cyan))
              .withCorners(Corners(10, 5, 30, 15)),
            Quad(Rectangle(230, 10, 100, 100), Fill.RadialGradient(Point(40), RGBA.Cyan, Point(100), RGBA.DarkBlue))
              .withCorners(Corners(10)),
            Quad(Rectangle(330, 10, 100, 100), Fill.RadialGradient(Point(40), RGBA.White, Point(100), RGBA.Black))
              .withCorners(Corners(1))
              .rotateBy(Radians.fromDegrees(Degrees(45)))
          )
          .addNodes(progressBar)
      ).withMagnification(Magnification.x2)
    )
  }
