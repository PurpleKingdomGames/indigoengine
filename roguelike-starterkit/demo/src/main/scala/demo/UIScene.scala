package demo

import indigo.*
import indigo.scenes.*
import roguelikestarterkit.*

object UIScene extends Scene[Size, Model, ViewModel]:

  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName =
    SceneName("UI scene")

  val modelLens: Lens[Model, Model] =
    Lens.keepLatest

  val viewModelLens: Lens[ViewModel, ViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: SceneContext[Size],
      model: Model
  ): GlobalEvent => Outcome[Model] =
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(RogueTerminalEmulatorScene.name))

    case e =>
      val updated =
        model.windowManager.update(
          UiContext(
            context.frameContext,
            Model.defaultCharSheet,
            CustomContext()
          ),
          e
        )

      updated.map(w => model.copy(windowManager = w))

  def updateViewModel(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    case e =>
      val updated = viewModel.windowManager.update(
        UiContext(
          context.frameContext,
          Model.defaultCharSheet,
          CustomContext()
        ),
        model.windowManager,
        e
      )

      updated.map(w => viewModel.copy(windowManager = w))

  def present(
      context: SceneContext[Size],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    WindowManager
      .present(
        UiContext(
          context.frameContext,
          Model.defaultCharSheet,
          CustomContext()
        ),
        RogueLikeGame.magnification,
        model.windowManager,
        viewModel.windowManager
      )

import indigo.*
import roguelikestarterkit.*

object ColourWindow {

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

  final case class ColorPalette(components: ComponentGroup)

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  def window(
      charSheet: CharSheet
  ): WindowModel[Size, CustomContext, ColorPalette] =
    WindowModel(
      WindowId("Color palette"),
      charSheet,
      ColorPalette(
        ComponentGroup(Bounds(0, 0, 23, 23))
          .withLayout(ComponentLayout.Vertical())
          .add(
            ComponentGroup(Bounds(0, 0, 23, 10))
              .withLayout(ComponentLayout.Horizontal(Overflow.Wrap))
              .add(
                outrunner16.colors.map { rgba =>
                  Button(Bounds(0, 0, 3, 3))(presentSwatch(charSheet, rgba, None))
                    // .onClick(<Emit some event...>)
                    .presentOver(presentSwatch(charSheet, rgba, Option(RGBA.White)))
                    .presentDown(presentSwatch(charSheet, rgba, Option(RGBA.Black)))
                }
              )
          )
          .add(
            Button(Bounds(0, 0, 14, 3))(
              presentButton(charSheet, "Load palette", RGBA.Silver, RGBA.Black)
            )
              // .onClick(<Emit some event...>)
              .presentOver(presentButton(charSheet, "Load palette", RGBA.White, RGBA.Black))
              .presentDown(presentButton(charSheet, "Load palette", RGBA.Black, RGBA.White))
          )
      )
    )
      .withTitle("Colour Palette")
      .moveTo(0, 0)
      .resizeTo(25, 25)
      .isDraggable
      .isResizable
      .isCloseable
      .updateModel(updateModel)
      .present(present)

  def updateModel(
      context: UiContext[Size, CustomContext],
      model: ColorPalette
  ): GlobalEvent => Outcome[ColorPalette] =
    case e =>
      model.components.update(context)(e).map { c =>
        model.copy(components = c)
      }

  def present(
      context: UiContext[Size, CustomContext],
      model: ColorPalette
  ): Outcome[SceneUpdateFragment] =
    model.components.present(context).map { c =>
      SceneUpdateFragment(c.nodes).addCloneBlanks(c.cloneBlanks)
    }

  def presentSwatch(
      charSheet: CharSheet,
      colour: RGBA,
      stroke: Option[RGBA]
  ): (Coords, Bounds) => Outcome[ComponentFragment] =
    (offset, bounds) =>
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

  def presentButton(
      charSheet: CharSheet,
      text: String,
      fgColor: RGBA,
      bgColor: RGBA
  ): (Coords, Bounds) => Outcome[ComponentFragment] =
    (offset, bounds) =>
      val hBar = Batch.fill(text.length)("─").mkString
      val size = bounds.dimensions.unsafeToSize

      val terminal =
        RogueTerminalEmulator(size)
          .put(Point(0, 0), Tile.`┌`, fgColor, bgColor)
          .put(Point(size.width - 1, 0), Tile.`┐`, fgColor, bgColor)
          .put(Point(0, size.height - 1), Tile.`└`, fgColor, bgColor)
          .put(Point(size.width - 1, size.height - 1), Tile.`┘`, fgColor, bgColor)
          .put(Point(0, 1), Tile.`│`, fgColor, bgColor)
          .put(Point(size.width - 1, 1), Tile.`│`, fgColor, bgColor)
          .putLine(Point(1, 0), hBar, fgColor, bgColor)
          .putLine(Point(1, 1), text, fgColor, bgColor)
          .putLine(Point(1, 2), hBar, fgColor, bgColor)
          .toCloneTiles(
            CloneId("button"),
            bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(offset.toScreenSpace(charSheet.size)),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      Outcome(
        ComponentFragment(
          terminal.clones
        ).addCloneBlanks(terminal.blanks)
      )
}

final case class ColorPalette(components: ComponentGroup)
