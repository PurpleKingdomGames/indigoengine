package demo.windows

import demo.Assets
import indigo.*
import roguelikestarterkit.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.ui.component.Component
import roguelikestarterkit.ui.components.ComponentGroup

object DemoWindow:

  val windowId: WindowId = WindowId("demo window")

  def window: Window[Unit, Unit] =
    Window(
      windowId,
      Size(1),
      Dimensions(128, 64),
      ()
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

  def content(charSheet: CharSheet): ComponentGroup[Unit] =
    ComponentGroup()
