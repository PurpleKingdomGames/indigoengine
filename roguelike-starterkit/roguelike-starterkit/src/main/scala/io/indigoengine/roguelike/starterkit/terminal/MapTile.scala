package io.indigoengine.roguelike.starterkit.terminal

import indigo.ShaderPrimitive.*
import indigo.*
import indigo.shared.datatypes.RGBA
import io.indigoengine.roguelike.starterkit.Tile

/** Represents the three basic properties of a tile: Character, foreground colour, and background
  * colour.
  */
final case class MapTile(char: Tile, foreground: RGBA, background: RGBA):
  def withChar(newChar: Tile): MapTile =
    this.copy(char = newChar)

  def withForegroundColor(newColor: RGBA): MapTile =
    this.copy(foreground = newColor)

  def withBackgroundColor(newColor: RGBA): MapTile =
    this.copy(background = newColor)

  def withColors(newForeground: RGBA, newBackground: RGBA): MapTile =
    this.copy(foreground = newForeground, background = newBackground)

object MapTile:
  def apply(char: Tile): MapTile =
    MapTile(char, RGBA.White, RGBA.Zero)

  def apply(char: Tile, foreground: RGBA): MapTile =
    MapTile(char, foreground, RGBA.Zero)
