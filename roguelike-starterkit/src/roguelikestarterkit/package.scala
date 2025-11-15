package roguelikestarterkit

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.Layer
import indigo.shared.shader.ShaderProgram

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

  extension (c: Layer.Content.type)
    def fromTerminalClones(terminalClones: TerminalClones): Layer.Content =
      Layer.Content(terminalClones.clones).addCloneBlanks(terminalClones.blanks)

    def apply(terminalClones: TerminalClones): Layer.Content =
      fromTerminalClones(terminalClones)

end syntax

object shaders:

  val material: Set[ShaderProgram] =
    Set(
      roguelikestarterkit.terminal.TerminalMaterial.standardShader,
      roguelikestarterkit.terminal.TerminalMaterial.standardShaderLit
    )

  val text: Set[ShaderProgram] =
    Set(
      roguelikestarterkit.terminal.TerminalText.standardShader
    )

  val all: Set[ShaderProgram] =
    material ++ text

end shaders

// Terminal

type Terminal = terminal.Terminal
val Terminal: terminal.Terminal.type = terminal.Terminal

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

// Generated

val RoguelikeTiles: tiles.RoguelikeTiles.type = tiles.RoguelikeTiles

type Tile = tiles.Tile
val Tile: tiles.Tile.type = tiles.Tile
