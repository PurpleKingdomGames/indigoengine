package demo

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.*
import roguelikestarterkit.ui.components.TerminalButton
import roguelikestarterkit.ui.components.TerminalInput
import roguelikestarterkit.ui.components.TerminalLabel
import roguelikestarterkit.ui.components.TerminalTextArea
import roguelikestarterkit.ui.components.common.ComponentId
import roguelikestarterkit.ui.components.common.ComponentLayout
import roguelikestarterkit.ui.components.common.Overflow
import roguelikestarterkit.ui.components.common.Padding
import roguelikestarterkit.ui.components.group.ComponentGroup
import roguelikestarterkit.ui.components.list.ComponentList
import roguelikestarterkit.ui.window.TerminalWindow

object ComponentsWindow2:

  val windowId: WindowId = WindowId("ComponentsWindow2")

  def window(
      charSheet: CharSheet
  ): Window[ComponentGroup[Int], Int] =
    TerminalWindow(
      windowId,
      charSheet,
      ComponentGroup()
        .withLayout(ComponentLayout.Vertical(Padding(0, 0, 1, 0)))
        .add(
          ComponentGroup()
            .withLayout(ComponentLayout.Horizontal(Padding(0, 1, 0, 0)))
            .add(
              TerminalLabel("label 1", TerminalLabel.Theme(charSheet)),
              TerminalLabel("label 2", TerminalLabel.Theme(charSheet)),
              TerminalLabel("label 3", TerminalLabel.Theme(charSheet))
            )
        )
        .add(
          ComponentGroup()
            .withLayout(ComponentLayout.Horizontal(Padding(0, 1, 0, 0)))
            .add(
              Batch(
                "History"  -> Batch(),
                "Controls" -> Batch(),
                "Quit"     -> Batch()
              ).map { case (label, clickEvents) =>
                TerminalButton(
                  label,
                  TerminalButton.Theme(charSheet)
                ).onClick(clickEvents)
              }
            )
        )
        .add(
          ComponentList(Dimensions(20, 11)) { (count: Int) =>
            Batch(
              ComponentId("window count") -> TerminalLabel[Int](
                "How many windows: ",
                TerminalLabel.Theme(charSheet)
              )
            ) ++
              (0 to count).toBatch.map { i =>
                ComponentId("w" + i) -> TerminalLabel("x " + i, TerminalLabel.Theme(charSheet))
              }
          }
            .withBackground { bounds =>
              Layer(
                Shape.Box(
                  Rectangle(
                    bounds.coords.toScreenSpace(charSheet.size),
                    bounds.dimensions.toScreenSpace(charSheet.size)
                  ),
                  Fill.Color(RGBA.Magenta.withAlpha(0.5))
                )
              )
            }
            .add((_: Int) =>
              ComponentId("input_dynamic") -> TerminalInput(20, TerminalInput.Theme(charSheet))
            )
            .add((count: Int) =>
              (0 to count).toBatch.map { i =>
                ComponentId("btn" + i) -> TerminalButton[Int](
                  "Button " + i,
                  TerminalButton.Theme(charSheet)
                ).onClick(
                  Log("count: " + count)
                )
              }
                :+ ComponentId("btnX") -> TerminalButton[Int](
                  "test",
                  TerminalButton.Theme(charSheet)
                ).onClick(Log("test"))
            )
            .add((i: Int) =>
              ComponentId("textarea") -> TerminalTextArea[Int](
                "abc.\nde,f\n0123456! " + i,
                TerminalTextArea.Theme(charSheet)
              )
            )
            .withLayout(ComponentLayout.Vertical(Padding.zero))
        )
        .add(
          TerminalInput(20, TerminalInput.Theme(charSheet))
        )
    )
      .moveTo(2, 5)
      .resizeTo(25, 25)
