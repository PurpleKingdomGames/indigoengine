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

object shaders:

  val ui: Set[indigo.Shader] =
    Set(
      roguelikestarterkit.ui.shaders.LayerMask.shader
    )

  val material: Set[indigo.Shader] =
    Set(
      roguelikestarterkit.terminal.TerminalMaterial.standardShader,
      roguelikestarterkit.terminal.TerminalMaterial.standardShaderLit
    )

  val text: Set[indigo.Shader] =
    Set(
      roguelikestarterkit.terminal.TerminalText.standardShader
    )

  val all: Set[indigo.Shader] =
    ui ++ material ++ text

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

// UI General Datatypes

type UiContext[ReferenceData] = ui.datatypes.UiContext[ReferenceData]
val UiContext: ui.datatypes.UiContext.type = ui.datatypes.UiContext

type CharSheet = ui.datatypes.CharSheet
val CharSheet: ui.datatypes.CharSheet.type = ui.datatypes.CharSheet

type Coords = ui.datatypes.Coords
val Coords: ui.datatypes.Coords.type = ui.datatypes.Coords

type Dimensions = ui.datatypes.Dimensions
val Dimensions: ui.datatypes.Dimensions.type = ui.datatypes.Dimensions

type Bounds = ui.datatypes.Bounds
val Bounds: ui.datatypes.Bounds.type = ui.datatypes.Bounds

// UI Windows

val WindowManager: ui.window.WindowManager.type = ui.window.WindowManager

type WindowId = ui.window.WindowId
val WindowId: ui.window.WindowId.type = ui.window.WindowId

type WindowModel[A, ReferenceData] = ui.window.WindowModel[A, ReferenceData]
val WindowModel: ui.window.WindowModel.type = ui.window.WindowModel

type WindowEvent = ui.window.WindowEvent
val WindowEvent: ui.window.WindowEvent.type = ui.window.WindowEvent

type WindowContent[A, ReferenceData] = ui.window.WindowContent[A, ReferenceData]
val WindowContent: ui.window.WindowContent.type = ui.window.WindowContent

// UI Components

type Component[A, ReferenceData] = ui.component.Component[A, ReferenceData]

type ComponentGroup[ReferenceData] = ui.component.ComponentGroup[ReferenceData]
val ComponentGroup: ui.component.ComponentGroup.type = ui.component.ComponentGroup

type ComponentFragment = ui.component.ComponentFragment
val ComponentFragment: ui.component.ComponentFragment.type = ui.component.ComponentFragment

type ComponentLayout = ui.component.ComponentLayout
val ComponentLayout: ui.component.ComponentLayout.type = ui.component.ComponentLayout

type Overflow = ui.component.Overflow
val Overflow: ui.component.Overflow.type = ui.component.Overflow

type Padding = ui.component.Padding
val Padding: ui.component.Padding.type = ui.component.Padding

type BoundsType = ui.component.BoundsType
val BoundsType: ui.component.BoundsType.type = ui.component.BoundsType

// UI Built in components

type Button[ReferenceData] = ui.components.Button[ReferenceData]
val Button: ui.components.Button.type = ui.components.Button

type Label[ReferenceData] = ui.components.Label[ReferenceData]
val Label: ui.components.Label.type = ui.components.Label
