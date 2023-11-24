package roguelikestarterkit

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle

object syntax:

  extension (r: Rectangle)
    def toPoints: Batch[Point] =
      Batch.fromIndexedSeq(
        (0 until r.height).flatMap { y =>
          (0 until r.width).map { x =>
            Point(r.x + x, r.y + y)
          }
        }
      )

end syntax

// Terminal

type TerminalEmulator = terminal.TerminalEmulator
val TerminalEmulator: terminal.TerminalEmulator.type = terminal.TerminalEmulator

type RogueTerminalEmulator = terminal.RogueTerminalEmulator
val RogueTerminalEmulator: terminal.RogueTerminalEmulator.type = terminal.RogueTerminalEmulator

type MapTile = terminal.MapTile
val MapTile: terminal.MapTile.type = terminal.MapTile

type TerminalText = terminal.TerminalText
val TerminalText: terminal.TerminalText.type = terminal.TerminalText

type TerminalMaterial = terminal.TerminalMaterial
val TerminalMaterial: terminal.TerminalMaterial.type = terminal.TerminalMaterial

type TerminalClones = terminal.TerminalClones
val TerminalClones: terminal.TerminalClones.type = terminal.TerminalClones

// Utils

val FOV: utils.FOV.type = utils.FOV

type PathFinder = utils.PathFinder
val PathFinder: utils.PathFinder.type = utils.PathFinder

type GridSquare = utils.GridSquare
val GridSquare: utils.GridSquare.type = utils.GridSquare

// Generated

val RoguelikeTiles: tiles.RoguelikeTiles.type = tiles.RoguelikeTiles

type Tile = tiles.Tile
val Tile: tiles.Tile.type = tiles.Tile
