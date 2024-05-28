package demo

import indigo.*
import roguelikestarterkit.*

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
          ComponentList { (count: Int) =>
            Batch(Label[Int]("How many windows: ", Label.Theme(charSheet))) ++
              Batch.fill(count)(Label("x", Label.Theme(charSheet)))
          }
            .withLayout(ComponentLayout.Horizontal())
        )
        .add(
          Input(20, Input.Theme(charSheet))
        )
        .add(
          TextArea("abc.\nde,f\n0123456!", TextArea.Theme(charSheet))
        )
    )
      .withTitle("More component examples")
      .moveTo(2, 2)
      .resizeTo(25, 25)
      .isDraggable
      .isResizable
      .isCloseable
