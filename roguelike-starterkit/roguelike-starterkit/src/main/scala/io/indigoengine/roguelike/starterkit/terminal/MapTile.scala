package io.indigoengine.roguelike.starterkit.terminal

import indigo.ShaderPrimitive.*
import indigo.*
import indigo.shared.datatypes.RGBA
import io.indigoengine.roguelike.starterkit.Tile

final case class MapTile(char: Tile, foreground: RGBA, background: RGBA):
  def withChar(newChar: Tile): MapTile =
    this.copy(char = newChar)

  def withForegroundColor(newColor: RGBA): MapTile =
    this.copy(foreground = newColor)

  def withBackgroundColor(newColor: RGBA): MapTile =
    this.copy(background = newColor)

object MapTile:
  def apply(char: Tile): MapTile =
    MapTile(char, RGBA.White, RGBA.Zero)

  def apply(char: Tile, foreground: RGBA): MapTile =
    MapTile(char, foreground, RGBA.Zero)