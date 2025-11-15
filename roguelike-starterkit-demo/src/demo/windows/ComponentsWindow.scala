package demo.windows

import demo.models.Log
import indigo.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.ui.*

object ComponentsWindow:

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  val windowId: WindowId = WindowId("ComponentsWindow")

  def window(
      charSheet: CharSheet
  ): Window[ComponentGroup[Int], Int] =
    TerminalWindow(
      windowId,
      charSheet,
      TerminalWindowChrome[Int](windowId, charSheet)
        .withTitle("Components A")
        .build(content(charSheet))
    )
      .moveTo(0, 3)
      .resizeTo(25, 25)

  def content(charSheet: CharSheet): ComponentGroup[Int] =
    ComponentGroup[Int]()
      .withLayout(ComponentLayout.Vertical(Padding(0, 0, 1, 0)))
      .add(
        TerminalSwitch[Int](
          TerminalSwitch.Theme(
            charSheet,
            TerminalTile(Tile.`0`, RGBA.Green, RGBA.Black),
            TerminalTile(Tile.X, RGBA.Red, RGBA.Black)
          )
        ).switchOn // On by default
          .onSwitch((_, switch) =>
            Batch(Log(s"Switch is now ${if switch.state.isOn then "on" else "off"}"))
          )
      )
      .add(
        TerminalButton(
          "Hello!",
          TerminalButton.Theme(
            charSheet,
            RGBA.Silver -> RGBA.Black,
            RGBA.White  -> RGBA.Black,
            RGBA.Black  -> RGBA.White,
            hasBorder = true
          )
        )
      )
      .add(
        TerminalButton(
          "World!",
          TerminalButton.Theme(
            charSheet,
            RGBA.Silver -> RGBA.Black,
            RGBA.Green  -> RGBA.Black,
            RGBA.Black  -> RGBA.Yellow,
            hasBorder = false
          )
        ),
        TerminalButton("Default!", TerminalButton.Theme(charSheet))
      )
      .add {
        TerminalButton(
          (ctx: UIContext[Int]) =>
            val i = ctx.reference
            "Count" + (if i > 0 then s": $i" else "")
          ,
          TerminalButton.Theme(charSheet).addBorder
        )
      }
      .add(
        Button[Int](Bounds(0, 0, 5, 2)) { case (ctx, btn) =>
          Outcome(
            Layer(
              Shape.Box(
                btn
                  .bounds(ctx)
                  .toScreenSpace(charSheet.size)
                  .moveTo(ctx.parent.coords.toScreenSpace(charSheet.size)),
                Fill.LinearGradient(Point.zero, RGBA.Cyan, Point(50, 0), RGBA.Magenta)
              )
            )
          )
        }
      )
      .add(
        Label[Int]("Custom rendered label", (_, t) => Bounds(0, 0, t.length, 1)) {
          case (ctx, label) =>
            val size = label.bounds(ctx).dimensions.unsafeToSize

            val terminal =
              RogueTerminalEmulator(size)
                .putLine(Point.zero, label.text(ctx), RGBA.Red, RGBA.Zero)
                .toCloneTiles(
                  CloneId("label"),
                  ctx.parent.coords.toScreenSpace(charSheet.size),
                  charSheet.charCrops
                ) { case (fg, bg) =>
                  graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
                }

            Outcome(Layer.Content(terminal))
        }
      )
      .add(
        TerminalLabel(
          "Terminal rendered label",
          TerminalLabel.Theme(charSheet, RGBA.Magenta, RGBA.Cyan)
        ),
        TerminalLabel("Default theme", TerminalLabel.Theme(charSheet)),
        TerminalLabel(
          (ctx: UIContext[Int]) => "Pointer over windows: " + ctx.reference,
          TerminalLabel.Theme(charSheet)
        )
      )
