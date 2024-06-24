package demo

import indigo.*
import roguelikestarterkit.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.components.TerminalLabel
import roguelikestarterkit.ui.components.common.ComponentLayout
import roguelikestarterkit.ui.components.common.Overflow
import roguelikestarterkit.ui.components.common.Padding
import roguelikestarterkit.ui.components.group.ComponentGroup

object ComponentsWindow:

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  val windowId: WindowId = WindowId("ComponentsWindow")

  def window(
      charSheet: CharSheet
  ): WindowModel[ComponentGroup[Int], Int] =
    WindowModel(
      windowId,
      charSheet,
      ComponentGroup()
        .withLayout(ComponentLayout.Vertical(Padding(0, 0, 1)))
        .add(
          Button(
            "Hello!",
            Button.Theme(
              charSheet,
              RGBA.Silver -> RGBA.Black,
              RGBA.White  -> RGBA.Black,
              RGBA.Black  -> RGBA.White,
              hasBorder = true
            )
          )
        )
        .add(
          Button(
            "World!",
            Button.Theme(
              charSheet,
              RGBA.Silver -> RGBA.Black,
              RGBA.Green  -> RGBA.Black,
              RGBA.Black  -> RGBA.Yellow,
              hasBorder = false
            )
          ),
          Button("Default!", Button.Theme(charSheet))
        )
        .add {
          Button(
            (i: Int) => "Count" + (if i > 0 then s": $i" else ""),
            Button.Theme(charSheet).addBorder
          )
        }
        .add(
          Button[Int](Bounds(0, 0, 5, 2)) { case (coords, bounds, _) =>
            Outcome(
              ComponentFragment(
                Shape.Box(
                  bounds.toScreenSpace(charSheet.size).moveTo(coords.toScreenSpace(charSheet.size)),
                  Fill.LinearGradient(Point.zero, RGBA.Cyan, Point(50, 0), RGBA.Magenta)
                )
              )
            )
          }
        )
        .add(
          Label[Int]("Custom rendered label", (_, t) => Bounds(0, 0, t.length, 1)) {
            case (offset, label, dimensions) =>
              val size = dimensions.unsafeToSize

              val terminal =
                RogueTerminalEmulator(size)
                  .putLine(Point.zero, label, RGBA.Red, RGBA.Zero)
                  .toCloneTiles(
                    CloneId("label"),
                    offset.toScreenSpace(charSheet.size),
                    charSheet.charCrops
                  ) { case (fg, bg) =>
                    graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
                  }

              Outcome(ComponentFragment(terminal))
          }
        )
        .add(
          TerminalLabel("Terminal rendered label", TerminalLabel.Theme(charSheet, RGBA.Magenta, RGBA.Cyan)),
          TerminalLabel("Default theme", TerminalLabel.Theme(charSheet)),
          TerminalLabel((count: Int) => "Mouse over windows: " + count, TerminalLabel.Theme(charSheet))
        )
    )
      .withTitle("Components example")
      .moveTo(0, 3)
      .resizeTo(25, 25)
      .isDraggable
      .isResizable
      .isCloseable
