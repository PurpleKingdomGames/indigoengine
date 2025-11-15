package roguelikestarterkit.ui.components

import roguelikestarterkit.tiles.Tile

final case class TerminalBorderTiles(
    fill: Tile,
    topLeft: Tile,
    topRight: Tile,
    bottomLeft: Tile,
    bottomRight: Tile,
    horizontal: Tile,
    vertical: Tile
):
  def withFill(value: Tile): TerminalBorderTiles =
    this.copy(fill = value)
  def withTopLeft(value: Tile): TerminalBorderTiles =
    this.copy(topLeft = value)
  def withTopRight(value: Tile): TerminalBorderTiles =
    this.copy(topRight = value)
  def withBottomLeft(value: Tile): TerminalBorderTiles =
    this.copy(bottomLeft = value)
  def withBottomRight(value: Tile): TerminalBorderTiles =
    this.copy(bottomRight = value)
  def withHorizontal(value: Tile): TerminalBorderTiles =
    this.copy(horizontal = value)
  def withVertical(value: Tile): TerminalBorderTiles =
    this.copy(vertical = value)

object TerminalBorderTiles:
  val default: TerminalBorderTiles =
    TerminalBorderTiles(Tile.` `, Tile.`┌`, Tile.`┐`, Tile.`└`, Tile.`┘`, Tile.`─`, Tile.`│`)
