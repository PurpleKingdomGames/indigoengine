package demo

import indigo.*
import roguelikestarterkit.*
import roguelikestarterkit.ui.component.ComponentFragment
import roguelikestarterkit.ui.components.TerminalLabel
import roguelikestarterkit.ui.components.common.ComponentLayout
import roguelikestarterkit.ui.components.common.Overflow
import roguelikestarterkit.ui.components.common.Padding
import roguelikestarterkit.ui.components.group.ComponentGroup
import roguelikestarterkit.ui.components.list.ComponentList

object ComponentsWindow2:

  val windowId: WindowId = WindowId("ComponentsWindow2")

  def window(
      charSheet: CharSheet
  ): WindowModel[ComponentGroup[Int], Int] =
    WindowModel(
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
                Button(
                  label,
                  Button.Theme(charSheet)
                ).onClick(clickEvents)
              }
            )
        )
        .add(
          ComponentList(Dimensions(20, 8)) { (count: Int) =>
            Batch(TerminalLabel[Int]("How many windows: ", TerminalLabel.Theme(charSheet))) ++
              Batch.fill(count)(TerminalLabel("x", TerminalLabel.Theme(charSheet)))
          }
            .add((count: Int) =>
              Batch.fill(count)(
                Button[Int]("Button", Button.Theme(charSheet)).onClick(Log("count: " + count))
              )
                :+ Button[Int]("test", Button.Theme(charSheet)).onClick(Log("test"))
            )
            .add((i: Int) => TextArea[Int]("abc.\nde,f\n0123456! " + i, TextArea.Theme(charSheet)))
            .withLayout(ComponentLayout.Vertical(Padding.zero))
        )
        .add(
          Input(20, Input.Theme(charSheet))
        )
    )
      .withTitle("More component examples")
      .moveTo(2, 5)
      .resizeTo(25, 25)
      .isDraggable
      .isResizable
      .isCloseable
