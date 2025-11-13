package roguelikestarterkit.ui.components

import indigo.*
import indigoextras.ui.*
import indigoextras.ui.syntax.*
import roguelikestarterkit.syntax.*
import roguelikestarterkit.terminal.RogueTerminalEmulator
import roguelikestarterkit.terminal.TerminalMaterial
import roguelikestarterkit.tiles.Tile
import roguelikestarterkit.ui.*

object TerminalSwitch:

  private val graphic = Graphic(0, 0, TerminalMaterial(AssetName(""), RGBA.White, RGBA.Black))

  private def present[ReferenceData](
      tile: TerminalTile,
      charSheet: CharSheet
  ): (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer] =
    (context, switch) =>
      val bounds = switch.bounds(context)
      val terminal =
        RogueTerminalEmulator(Size(1))
          .put(Point.zero, tile.tile, tile.colors.foreground, tile.colors.background)
          .toCloneTiles(
            CloneId(s"switch_${charSheet.assetName.toString}"),
            bounds.coords
              .toScreenSpace(charSheet.size)
              .moveBy(context.parent.coords.toScreenSpace(charSheet.size)),
            charSheet.charCrops
          ) { case (fg, bg) =>
            graphic.withMaterial(TerminalMaterial(charSheet.assetName, fg, bg))
          }

      Outcome(Layer.Content(terminal))

  def apply[ReferenceData](theme: Theme): Switch[ReferenceData] =
    Switch[ReferenceData](Bounds(Dimensions(1)))(
      present(theme.on, theme.charSheet),
      present(theme.off, theme.charSheet)
    )

  final case class Theme(
      charSheet: CharSheet,
      on: TerminalTile,
      off: TerminalTile
  )
  object Theme:
    def apply(charSheet: CharSheet, on: Tile, off: Tile): Theme =
      Theme(
        charSheet,
        TerminalTile(on, TerminalTileColors(RGBA.White, RGBA.Black)),
        TerminalTile(off, TerminalTileColors(RGBA.Black, RGBA.White))
      )
