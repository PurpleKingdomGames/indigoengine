package roguelikestarterkit.ui.components

import indigo.shared.datatypes.RGBA
import roguelikestarterkit.tiles.Tile

final case class TerminalTile(tile: Tile, colors: TerminalTileColors):
  def withTile(value: Tile): TerminalTile =
    this.copy(tile = value)
  def withColors(value: TerminalTileColors): TerminalTile =
    this.copy(colors = value)

object TerminalTile:
  def apply(tile: Tile, foreground: RGBA, background: RGBA): TerminalTile =
    TerminalTile(tile, TerminalTileColors(foreground, background))
