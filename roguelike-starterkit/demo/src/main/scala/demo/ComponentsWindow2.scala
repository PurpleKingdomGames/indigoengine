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
          ComponentList(Dimensions(20, 8)) { (count: Int) =>
            Batch(Label[Int]("How many windows: ", Label.Theme(charSheet))) ++
              Batch.fill(count)(Label("x", Label.Theme(charSheet)))
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
