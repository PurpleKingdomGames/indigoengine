package io.indigoengine.roguelike.starterkit

// Terminal

type TerminalEmulator = terminal.TerminalEmulator
val TerminalEmulator: terminal.TerminalEmulator.type = terminal.TerminalEmulator

type RogueTerminalEmulator = terminal.RogueTerminalEmulator
val RogueTerminalEmulator: terminal.RogueTerminalEmulator.type =
  terminal.RogueTerminalEmulator

type TerminalEntity = terminal.TerminalEntity
val TerminalEntity: terminal.TerminalEntity.type = terminal.TerminalEntity

type MapTile = terminal.MapTile
val MapTile: terminal.MapTile.type = terminal.MapTile

type TerminalText = terminal.TerminalText
val TerminalText: terminal.TerminalText.type = terminal.TerminalText

type TerminalClones = terminal.TerminalClones
val TerminalClones: terminal.TerminalClones.type = terminal.TerminalClones

// Utils

val FOV: utils.FOV.type = utils.FOV

type PathFinder = utils.PathFinder
val PathFinder: utils.PathFinder.type = utils.PathFinder

val Coords: utils.Coords.type = utils.Coords

type GridSquare = utils.GridSquare
val GridSquare: utils.GridSquare.type = utils.GridSquare

// Generated

val RoguelikeTiles: tiles.RoguelikeTiles.type = tiles.RoguelikeTiles

type Tile = tiles.Tile
val Tile: tiles.Tile.type = tiles.Tile
