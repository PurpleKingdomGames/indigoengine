package roguelikestarterkit.ui.window

import indigo.*
import indigo.syntax.*
import roguelikestarterkit.terminal.MapTile
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalClones
import roguelikestarterkit.tiles.Tile
import roguelikestarterkit.ui.datatypes.Bounds
import roguelikestarterkit.ui.datatypes.Coords
import roguelikestarterkit.ui.datatypes.Dimensions
import roguelikestarterkit.ui.datatypes.UiContext

final case class WindowViewModel[ReferenceData](
    id: WindowId,
    modelHashCode: Int,
    terminal: RogueTerminalEmulator,
    terminalClones: TerminalClones,
    contentRectangle: Bounds,
    dragData: Option[DragData],
    resizeData: Option[DragData],
    mouseIsOver: Boolean,
    magnification: Int
):

  def update[A](
      context: UiContext[ReferenceData],
      model: WindowModel[A, ReferenceData],
      event: GlobalEvent
  ): Outcome[WindowViewModel[ReferenceData]] =
    Window.updateViewModel(context, model, this)(event)

  def resize[A](model: WindowModel[A, ReferenceData]): WindowViewModel[ReferenceData] =
    this.copy(terminal = WindowViewModel.makeWindowTerminal(model, terminal))

object WindowViewModel:

  def initial[ReferenceData](id: WindowId, magnification: Int): WindowViewModel[ReferenceData] =
    WindowViewModel(
      id,
      0,
      RogueTerminalEmulator(Size.zero),
      TerminalClones.empty,
      Bounds.zero,
      None,
      None,
      false,
      magnification
    )

  def makeWindowTerminal[A, ReferenceData](
      model: WindowModel[A, ReferenceData],
      current: RogueTerminalEmulator
  ): RogueTerminalEmulator =
    val validSize =
      model.bounds.dimensions.max(if model.title.isDefined then Dimensions(3) else Dimensions(2))

    val tiles: Batch[(Point, MapTile)] =
      val grey  = RGBA.White.mix(RGBA.Black, if model.hasFocus then 0.4 else 0.8)
      val title = model.title.getOrElse("").take(model.bounds.dimensions.width - 2).toCharArray()

      (0 to validSize.height).toBatch.flatMap { _y =>
        (0 to validSize.width).toBatch.map { _x =>
          val maxX   = validSize.width - 1
          val maxY   = validSize.height - 1
          val coords = Point(_x, _y)

          coords match
            // When there is a title
            case Point(0, 1) if model.title.isDefined =>
              // Title bar left
              coords -> MapTile(Tile.`│`, RGBA.White, RGBA.Black)

            case Point(x, 1) if model.title.isDefined && x == maxX =>
              // Title bar right
              coords -> MapTile(Tile.`│`, RGBA.White, RGBA.Black)

            case Point(x, 1) if model.title.isDefined =>
              // Title text, x starts at 2
              val idx = x - 1
              val tile =
                if idx >= 0 && idx < title.length then
                  val c = title(idx)
                  Tile.charCodes.get(if c == '\\' then "\\" else c.toString) match
                    case None       => Tile.SPACE
                    case Some(char) => Tile(char)
                else Tile.SPACE

              coords -> MapTile(tile, RGBA.White, RGBA.Black)

            case Point(0, 2) if model.title.isDefined =>
              // Title bar line left
              val tile = if maxY > 2 then Tile.`├` else Tile.`└`
              coords -> MapTile(Tile.`│`, RGBA.White, RGBA.Black)

            case Point(x, 2) if model.title.isDefined && x == maxX =>
              // Title bar line right
              val tile = if maxY > 2 then Tile.`┤` else Tile.`┘`
              coords -> MapTile(tile, RGBA.White, RGBA.Black)

            case Point(x, 2) if model.title.isDefined =>
              // Title bar line
              coords -> MapTile(Tile.`─`, RGBA.White, RGBA.Black)

            // Normal window frame

            case Point(0, 0) =>
              // top left
              coords -> MapTile(Tile.`┌`, RGBA.White, RGBA.Black)

            case Point(x, 0) if model.closeable && x == maxX =>
              // top right closable
              coords -> MapTile(Tile.`x`, RGBA.Black, RGBA.White)

            case Point(x, 0) if x == maxX =>
              // top right
              coords -> MapTile(Tile.`┐`, RGBA.White, RGBA.Black)

            case Point(x, 0) =>
              // top
              coords -> MapTile(Tile.`─`, RGBA.White, RGBA.Black)

            case Point(0, y) if y == maxY =>
              // bottom left
              coords -> MapTile(Tile.`└`, RGBA.White, RGBA.Black)

            case Point(x, y) if model.resizable && x == maxX && y == maxY =>
              // bottom right with resize
              coords -> MapTile(Tile.`▼`, RGBA.White, RGBA.Black)

            case Point(x, y) if x == maxX && y == maxY =>
              // bottom right
              coords -> MapTile(Tile.`┘`, RGBA.White, RGBA.Black)

            case Point(x, y) if y == maxY =>
              // bottom
              coords -> MapTile(Tile.`─`, RGBA.White, RGBA.Black)

            case Point(0, y) =>
              // Middle left
              coords -> MapTile(Tile.`│`, RGBA.White, RGBA.Black)

            case Point(x, y) if x == maxX =>
              // Middle right
              coords -> MapTile(Tile.`│`, RGBA.White, RGBA.Black)

            case Point(x, y) =>
              // Window background
              coords -> MapTile(Tile.`░`, grey, RGBA.Black)

        }
      }

    RogueTerminalEmulator(validSize.unsafeToSize)
      .put(tiles)
