package demo.windows

import indigo.*
import roguelikestarterkit.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.ui.component.Component
import roguelikestarterkit.ui.components.ComponentGroup
import roguelikestarterkit.ui.components.TerminalButton
import roguelikestarterkit.ui.components.datatypes.ComponentLayout
import roguelikestarterkit.ui.components.datatypes.Overflow
import roguelikestarterkit.ui.components.datatypes.Padding
import roguelikestarterkit.ui.window.TerminalWindow
import roguelikestarterkit.ui.window.TerminalWindowChrome

object ColourWindow:

  final case class ColorPaletteReference(name: String, count: Int, colors: Batch[RGBA])

  val outrunner16 = ColorPaletteReference(
    "outrunner-16",
    16,
    Batch(
      RGBA.fromHexString("4d004c"),
      RGBA.fromHexString("8f0076"),
      RGBA.fromHexString("c70083"),
      RGBA.fromHexString("f50078"),
      RGBA.fromHexString("ff4764"),
      RGBA.fromHexString("ff9393"),
      RGBA.fromHexString("ffd5cc"),
      RGBA.fromHexString("fff3f0"),
      RGBA.fromHexString("000221"),
      RGBA.fromHexString("000769"),
      RGBA.fromHexString("00228f"),
      RGBA.fromHexString("0050c7"),
      RGBA.fromHexString("008bf5"),
      RGBA.fromHexString("00bbff"),
      RGBA.fromHexString("47edff"),
      RGBA.fromHexString("93fff8")
    )
  )

  val windowId: WindowId = WindowId("Color palette")

  def window(
      charSheet: CharSheet
  ): Window[ColorPalette, Unit] =
    TerminalWindow(
      windowId,
      charSheet,
      ColorPalette(
        TerminalWindowChrome[Unit](
          windowId,
          charSheet
        )
          .withTitle("Colour palette")
          .build(content(charSheet))
      )
    )
      .moveTo(5, 5)
      .resizeTo(20, 20)

  def content(charSheet: CharSheet): ComponentGroup[Unit] =
    ComponentGroup()
      .withLayout(ComponentLayout.Vertical(Padding.zero.withBottom(1)))
      .add(
        ComponentGroup()
          .withLayout(ComponentLayout.Horizontal(Overflow.Wrap))
          .add(
            // Custom rendered buttons for the swatches
            outrunner16.colors.map { rgba =>
              Button(Bounds(0, 0, 3, 3))(presentSwatch(charSheet, rgba, None))
                // .onClick(<Emit some event...>)
                .presentOver(presentSwatch(charSheet, rgba, Option(RGBA.White)))
                .presentDown(presentSwatch(charSheet, rgba, Option(RGBA.Black)))
            }
          )
      )
      .add(
        // Default button renderer
        TerminalButton(
          "Load palette",
          TerminalButton.Theme(
            charSheet,
            RGBA.Silver -> RGBA.Black,
            RGBA.White  -> RGBA.Black,
            RGBA.Black  -> RGBA.White,
            hasBorder = true
          )
        )
      )
      .withBackground { bounds =>
        Layer(
          Shape.Box(
            bounds.toScreenSpace(charSheet.size),
            Fill.Color(RGBA.Cyan.withAlpha(0.5))
          )
        )
      }

  def presentSwatch(
      charSheet: CharSheet,
      colour: RGBA,
      stroke: Option[RGBA]
  ): (Coords, Bounds, Unit) => Outcome[Layer] =
    (offset, bounds, _) =>
      Outcome(
        Layer(
          stroke match
            case None =>
              Shape.Box(
                Rectangle(
                  offset.toScreenSpace(charSheet.size),
                  bounds.dimensions.toScreenSpace(charSheet.size)
                ),
                Fill.Color(colour)
              )

            case Some(strokeColor) =>
              Shape.Box(
                Rectangle(
                  offset.toScreenSpace(charSheet.size),
                  bounds.dimensions.toScreenSpace(charSheet.size)
                ),
                Fill.Color(colour),
                Stroke(2, strokeColor)
              )
        )
      )

final case class ColorPalette(componentGroup: ComponentGroup[Unit])
object ColorPalette:

  given Component[ColorPalette, Unit] with

    def bounds(reference: Unit, model: ColorPalette): Bounds =
      Bounds(model.componentGroup.dimensions)

    def updateModel(
        context: UIContext[Unit],
        model: ColorPalette
    ): GlobalEvent => Outcome[ColorPalette] =
      case e =>
        model.componentGroup.update(context)(e).map { c =>
          model.copy(componentGroup = c)
        }

    def present(
        context: UIContext[Unit],
        model: ColorPalette
    ): Outcome[Layer] =
      model.componentGroup.present(context)

    def refresh(reference: Unit, model: ColorPalette, contentDimensions: Dimensions): ColorPalette =
      model.copy(
        componentGroup = model.componentGroup.refresh(reference, contentDimensions)
      )
