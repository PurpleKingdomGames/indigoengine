package demo.windows

import demo.models.Log
import indigo.*
import indigo.syntax.*
import indigoextras.ui.*
import roguelikestarterkit.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.ui.*

object ComponentsWindow2:

  val windowId: WindowId = WindowId("ComponentsWindow2")

  def window(
      charSheet: CharSheet
  ): Window[ComponentGroup[Int], Int] =
    TerminalWindow(
      windowId,
      charSheet,
      TerminalWindowChrome[Int](windowId, charSheet)
        .withTitle("Components B")
        // .noClose
        // .noDrag
        // .noScroll
        // .noResize
        .build(content(charSheet))
    )
      .moveTo(2, 5)
      .resizeTo(25, 25)

  def content(charSheet: CharSheet): ComponentGroup[Int] =
    ComponentGroup[Int]()
      .withLayout(ComponentLayout.Vertical(Padding(0, 0, 1, 0)))
      .add(
        TerminalInput[Int]("Input 1", 20, TerminalInput.Theme(charSheet))
      )
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
        ComponentList[Int, Label[Int]](Dimensions(20, 11)) { (ctx: UIContext[Int]) =>
          Batch(
            ComponentId("window count") -> TerminalLabel[Int](
              "How many windows: ",
              TerminalLabel.Theme(charSheet)
            )
          ) ++
            (0 to ctx.reference).toBatch.map { i =>
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
          .add((_: UIContext[Int]) =>
            ComponentId("input_dynamic") -> TerminalInput[Int](
              "Input 2",
              20,
              TerminalInput.Theme(charSheet)
            )
          )
          .add((ctx: UIContext[Int]) =>
            (0 to ctx.reference).toBatch.map { i =>
              ComponentId("btn" + i) -> TerminalButton[Int](
                "Button " + i,
                TerminalButton.Theme(charSheet)
              ).onClick(
                Log("count: " + ctx.reference)
              )
            }
              :+ ComponentId("btnX") -> TerminalButton[Int](
                "test",
                TerminalButton.Theme(charSheet)
              ).onClick(Log("test"))
          )
          .add((ctx: UIContext[Int]) =>
            ComponentId("textarea") -> TerminalTextArea[Int](
              "abc.\nde,f\n0123456! " + ctx.reference,
              TerminalTextArea.Theme(charSheet)
            )
          )
          .withLayout(ComponentLayout.Vertical(Padding.zero))
      )
