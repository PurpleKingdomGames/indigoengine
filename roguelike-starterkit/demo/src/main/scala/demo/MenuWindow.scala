package demo

import indigo.*
import roguelikestarterkit.*
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.components.common.ComponentLayout
import roguelikestarterkit.ui.components.common.Overflow
import roguelikestarterkit.ui.components.common.Padding
import roguelikestarterkit.ui.components.group.ComponentGroup
import roguelikestarterkit.ui.components.list.ComponentList

object MenuWindow:

  val windowId: WindowId = WindowId("MenuWindow")

  def window(
      charSheet: CharSheet
  ): WindowModel[ComponentList[Int], Int] =
    WindowModel(
      windowId,
      charSheet,
      ComponentList(Dimensions(20, 3)) { (_: Int) =>
        Batch(
          Button[Int](
            "Window 1",
            Button.Theme(
              charSheet,
              RGBA.Silver -> RGBA.Black,
              RGBA.White  -> RGBA.Black,
              RGBA.Black  -> RGBA.White,
              hasBorder = false
            )
          ).onClick(Log("Window 1"), WindowEvent.Open(ComponentsWindow.windowId)),
          Button[Int](
            "Window 2",
            Button.Theme(
              charSheet,
              RGBA.Silver -> RGBA.Black,
              RGBA.Green  -> RGBA.Black,
              RGBA.Black  -> RGBA.Yellow,
              hasBorder = false
            )
          ).onClick(Log("Window 2"), WindowEvent.Open(ComponentsWindow2.windowId))
        )
      }
        .withLayout(ComponentLayout.Horizontal(Padding(0, 1, 0, 0)))
    )
      .moveTo(0, 0)
      .resizeTo(20, 3)
      .isStatic
