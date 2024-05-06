package demo

import indigo.*
import roguelikestarterkit.*

object ComponentsWindow:

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  val windowId: WindowId = WindowId("ComponentsWindow")

  def window(
      charSheet: CharSheet
  ): WindowModel[ComponentGroup, Unit] =
    WindowModel(
      windowId,
      charSheet,
      ComponentGroup(Bounds(0, 0, 23, 23))
        .withLayout(ComponentLayout.Vertical(Padding(0, 0, 1)))
        .inheritBounds
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
        .add(
          Button(Bounds(0, 0, 5, 2)) { case (coords, bounds) =>
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
          Label("Custom rendered label") { case (offset, label) =>
            val dimensions = Dimensions(label.length, 1)
            val size       = dimensions.unsafeToSize

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
          Label("Terminal rendered label", Label.Theme(charSheet, RGBA.Magenta, RGBA.Cyan)),
          Label("Default theme", Label.Theme(charSheet))
        )
    )
      .withTitle("Components example")
      .moveTo(0, 0)
      .resizeTo(25, 25)
      .isDraggable
      .isResizable
      .isCloseable
