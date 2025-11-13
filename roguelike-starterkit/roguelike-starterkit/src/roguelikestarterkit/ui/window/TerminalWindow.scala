package roguelikestarterkit.ui.window

import indigo.*
import indigo.syntax.*
import indigoextras.ui.*
import roguelikestarterkit.MapTile
import roguelikestarterkit.TerminalMaterial
import roguelikestarterkit.Tile
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.ui.datatypes.CharSheet

object TerminalWindow:

  def apply[A, ReferenceData](
      id: WindowId,
      charSheet: CharSheet,
      content: A
  )(using Component[A, ReferenceData]): Window[A, ReferenceData] =
    Window(id, charSheet.size, Dimensions(5), content)
      .withBackground(present(charSheet))

  private val graphic: Graphic[TerminalMaterial] =
    Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  def present[A, ReferenceData](charSheet: CharSheet)(
      context: WindowContext[ReferenceData]
  ): Outcome[Layer] =
    val validSize =
      context.bounds.dimensions.max(Dimensions(5))

    val tiles: Batch[(Point, MapTile)] =
      val grey = RGBA.White.mix(RGBA.Black, if context.hasFocus then 0.4 else 0.8)

      (0 to validSize.height).toBatch.flatMap { _y =>
        (0 to validSize.width).toBatch.map { _x =>
          val maxX   = validSize.width - 1
          val maxY   = validSize.height - 1
          val coords = Point(_x, _y)

          coords match
            case Point(0, 0) =>
              // top left
              coords -> MapTile(Tile.`┌`, RGBA.White, RGBA.Black)

            case Point(x, 0) if x == maxX =>
              // top right
              coords -> MapTile(Tile.`┐`, RGBA.White, RGBA.Black)

            case Point(x, 0) =>
              // top
              coords -> MapTile(Tile.`─`, RGBA.White, RGBA.Black)

            case Point(0, y) if y == maxY =>
              // bottom left
              coords -> MapTile(Tile.`└`, RGBA.White, RGBA.Black)

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

    val terminalClones =
      RogueTerminalEmulator(validSize.unsafeToSize)
        .put(tiles)
        .toCloneTiles(
          CloneId("terminal_window_tile_clone_id"),
          context.bounds.coords.toScreenSpace(charSheet.size),
          charSheet.charCrops
        ) { case (fg, bg) =>
          graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
        }

    Outcome(
      Layer
        .Content(terminalClones.clones)
        .addCloneBlanks(terminalClones.blanks)
    )
