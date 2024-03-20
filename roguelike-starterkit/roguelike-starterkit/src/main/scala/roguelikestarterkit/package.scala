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

type UiContext = ui.datatypes.UiContext
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

type WindowManagerModel[StartupData, A] = ui.window.WindowManagerModel[StartupData, A]
val WindowManagerModel: ui.window.WindowManagerModel.type = ui.window.WindowManagerModel

type WindowManagerViewModel[StartupData, A] = ui.window.WindowManagerViewModel[StartupData, A]
val WindowManagerViewModel: ui.window.WindowManagerViewModel.type = ui.window.WindowManagerViewModel

type WindowManagerEvent = ui.window.WindowManagerEvent
val WindowManagerEvent: ui.window.WindowManagerEvent.type = ui.window.WindowManagerEvent

type WindowId = ui.window.WindowId
val WindowId: ui.window.WindowId.type = ui.window.WindowId

type WindowModel[StartupData, CA, A] = ui.window.WindowModel[StartupData, CA, A]
val WindowModel: ui.window.WindowModel.type = ui.window.WindowModel

type WindowViewModel = ui.window.WindowViewModel
val WindowViewModel: ui.window.WindowViewModel.type = ui.window.WindowViewModel

type WindowEvent = ui.window.WindowEvent
val WindowEvent: ui.window.WindowEvent.type = ui.window.WindowEvent

// UI Components

type Component[A] = ui.component.Component[A]

type ComponentGroup = ui.component.ComponentGroup
val ComponentGroup: ui.component.ComponentGroup.type = ui.component.ComponentGroup

type ComponentFragment = ui.component.ComponentFragment
val ComponentFragment: ui.component.ComponentFragment.type = ui.component.ComponentFragment

type ComponentLayout = ui.component.ComponentLayout
val ComponentLayout: ui.component.ComponentLayout.type = ui.component.ComponentLayout

type Overflow = ui.component.Overflow
val Overflow: ui.component.Overflow.type = ui.component.Overflow

type Padding = ui.component.Padding
val Padding: ui.component.Padding.type = ui.component.Padding

// UI Built in components

type Button = ui.components.Button
val Button: ui.components.Button.type = ui.components.Button

type Label = ui.components.Label
val Label: ui.components.Label.type = ui.components.Label
