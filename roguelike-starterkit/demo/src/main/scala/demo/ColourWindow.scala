package demo

import indigo.*
import roguelikestarterkit.*

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

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  val windowId: WindowId = WindowId("Color palette")

  def window(
      charSheet: CharSheet
  ): WindowModel[ColorPalette, Unit] =
    WindowModel(
      windowId,
      charSheet,
      ColorPalette(
        // ComponentGroup(Bounds(0, 0, 23, 23))
        //   .withLayout(ComponentLayout.Vertical())
        //   .inheritBounds
        //   // .add(
        //   //   ComponentGroup(Bounds(0, 0, 23, 10))
        //   //     .withLayout(ComponentLayout.Horizontal(Overflow.Wrap))
        //   //     .offsetSize(0, -4)
        //   //     .add(
        //   //       // Custom rendered buttons for the swatches
        //   //       outrunner16.colors.map { rgba =>
        //   //         Button(Bounds(0, 0, 3, 3))(presentSwatch(charSheet, rgba, None))
        //   //           // .onClick(<Emit some event...>)
        //   //           .presentOver(presentSwatch(charSheet, rgba, Option(RGBA.White)))
        //   //           .presentDown(presentSwatch(charSheet, rgba, Option(RGBA.Black)))
        //   //       }
        //   //     )
        //   // )
        //   .add(
        //     // Default button renderer
        //     Button(
        //       "Load palette",
        //       Button.Theme(
        //         charSheet,
        //         RGBA.Silver -> RGBA.Black,
        //         RGBA.White  -> RGBA.Black,
        //         RGBA.Black  -> RGBA.White,
        //         hasBorder = true
        //       )
        //     )
        //   )
      )
    )
      .withTitle("Colour Palette")
      .moveTo(0, 0)
      .resizeTo(25, 25)
      .isDraggable
      .isResizable
      .isCloseable

  def presentSwatch(
      charSheet: CharSheet,
      colour: RGBA,
      stroke: Option[RGBA]
  ): (Coords, Bounds, Unit) => Outcome[ComponentFragment] =
    (offset, bounds, _) =>
      Outcome(
        ComponentFragment(
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

final case class ColorPalette(/*componentGroup: ComponentGroup[Unit]*/)
object ColorPalette:

  given WindowContent[ColorPalette, Unit] with

    def updateModel(
        context: UiContext[Unit],
        model: ColorPalette
    ): GlobalEvent => Outcome[ColorPalette] =
      case e =>
        Outcome(model)
        // model.componentGroup.update(context)(e).map { c =>
        //   model.copy(componentGroup = c)
        // }

    def present(
        context: UiContext[Unit],
        model: ColorPalette
    ): Outcome[Layer] =
      // model.componentGroup.present(context).map(_.toLayer)
      Outcome(Layer.empty)

    def cascade(model: ColorPalette, newBounds: Bounds): ColorPalette =
      model
      // .copy(
      //   componentGroup = model.componentGroup.cascade(newBounds)
      // )

    def refresh(model: ColorPalette): ColorPalette =
      model
      // .copy(
      //   componentGroup = model.componentGroup.reflow
      // )
