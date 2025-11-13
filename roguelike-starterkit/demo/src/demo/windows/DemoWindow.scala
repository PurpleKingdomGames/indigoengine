package demo.windows

import demo.Assets
import indigo.*
import indigoextras.ui.*

object DemoWindow:

  val windowId: WindowId = WindowId("demo window")

  def window: Window[ComponentGroup[Unit], Unit] =
    Window(
      windowId,
      Size(1),
      Dimensions(128, 64),
      content
    )
      .withBackground { ctx =>
        Outcome(
          Layer.Content(
            Graphic(
              ctx.bounds.width,
              ctx.bounds.height,
              Material
                .Bitmap(Assets.assets.window)
                .nineSlice(Rectangle(3, 15, 121, 41))
            ),
            Shape.Box(ctx.bounds.toScreenSpace(Size(1)), Fill.None, Stroke(1, RGBA.Green))
          )
        )
      }

  def content: ComponentGroup[Unit] =
    ComponentGroup()
      .anchor(
        HitArea(Bounds(0, 0, 16, 16))
          .onClick(
            WindowEvent.Close(windowId)
          )
          .withFill(RGBA.Green.withAlpha(0.5))
          .withStroke(Stroke(1, RGBA.Green.withAlpha(0.75))),
        Anchor.TopRight
      )
