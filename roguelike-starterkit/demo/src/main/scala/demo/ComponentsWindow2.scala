package demo

import indigo.*
import roguelikestarterkit.*
import roguelikestarterkit.ui.components.Input

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
              Label("label 1", Label.Theme(charSheet)),
              Label("label 2", Label.Theme(charSheet)),
              Label("label 3", Label.Theme(charSheet))
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
          Input(20, Input.Theme(charSheet))
        )
    )
      .withTitle("More component examples")
      .moveTo(2, 2)
      .resizeTo(25, 25)
      .isDraggable
      .isResizable
      .isCloseable
