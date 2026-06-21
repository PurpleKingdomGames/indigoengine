package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*

object NineSliceScene extends Scene[SandboxGameModel] {

  type SceneModel = SandboxGameModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("nine slice scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext,
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def fit(originalSize: Vector2, screenSize: Vector2): Vector2 =
    Vector2(Math.max(screenSize.x / originalSize.x, screenSize.y / originalSize.y))

  def boxSize(t: Seconds): Int = Signal.SmoothPulse.map(d => (d * 64) + 32).map(_.toInt).at(t)

  def present(
      context: SceneContext,
      model: SandboxGameModel
  ): Outcome[SceneUpdateFragment] = {
    val boxSizeValue: Int = boxSize(context.frame.time.running)

    val graphic =
      Graphic(64, 64, Material.Bitmap(SandboxAssets.cratesDiffuseName))

    def crate(gg: Graphic[Material.Bitmap])(position: Point): Batch[SceneNode] =
      Batch(
        gg.moveTo(position).withSize(Size(boxSizeValue)),
        Shape
          .Box(Rectangle(boxSizeValue, boxSizeValue), Fill.None, Stroke(1, RGBA.Green))
          .moveTo(position)
      )

    def crateStyles(gg: Graphic[Material.Bitmap], ps: Batch[Point], doNineSlice: Boolean): Batch[SceneNode] =
      val skip: Point => Batch[SceneNode] = _ => Batch.empty
      Batch(
        crate(gg),
        crate(gg.modifyMaterial(_.tile)),
        crate(gg.modifyMaterial(_.stretch)),
        if doNineSlice then crate(gg.modifyMaterial(_.nineSlice(Rectangle(5, 5, 24, 40))))
        else skip
      ).zip(ps)
        .flatMap: (c, p) =>
          c(p)

    Outcome(
      SceneUpdateFragment.empty
        .addLayers(
          Constants.LayerKeys.game ->
            Layer(
              Batch(
                Graphic(
                  boxSizeValue,
                  boxSizeValue,
                  Material.Bitmap(SandboxAssets.nineSlice).nineSlice(Rectangle(16, 16, 32, 32))
                ).moveTo(5, 5),
                // Shape
                //   .Box(Rectangle(boxSizeValue, boxSizeValue), Fill.None, Stroke(1, RGBA.Green))
                //   .moveTo(5, 5),
                Graphic(
                  boxSizeValue,
                  boxSizeValue,
                  Material.Bitmap(SandboxAssets.platform).nineSlice(Rectangle(8, 20, 112, 40))
                ).moveTo(100, 5),
                // Shape
                //   .Box(Rectangle(boxSizeValue, boxSizeValue), Fill.None, Stroke(1, RGBA.Green))
                //   .moveTo(100, 5),
                Graphic(
                  boxSizeValue,
                  boxSizeValue,
                  Material.Bitmap(SandboxAssets.window).nineSlice(Rectangle(3, 15, 121, 41))
                ).moveTo(5, 100)
                // Shape
                //   .Box(Rectangle(boxSizeValue, boxSizeValue), Fill.None, Stroke(1, RGBA.Green))
                //   .moveTo(5, 100),
              ) ++
                crateStyles(graphic, (0 until 4).toBatch.map(col => Point(col * 100, 200)), false) ++
                crateStyles(
                  graphic.withCrop(32, 0, 32, 48),
                  (0 until 4).toBatch.map(col => Point(col * 100, 300)),
                  true
                )
            ).withMagnificationForAll(2)
        )
    )
  }

}
