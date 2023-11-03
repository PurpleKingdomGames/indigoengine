package indigo.benchmarks

import indigo.*

import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*
import io.indigoengine.roguelike.starterkit.*
import io.indigoengine.roguelike.starterkit.terminal.MapTile

object TerminalEmulatorBenchmarks:

  val terminal4     = TerminalEmulator(Size(4))
  val terminal8     = TerminalEmulator(Size(8))
  val terminal16     = TerminalEmulator(Size(16))
  val terminal32     = TerminalEmulator(Size(32))
  val terminal64     = TerminalEmulator(Size(64))
  val mapTile      = MapTile(Tile.BLACK_HEART_SUIT)
  val fullTerminal = terminal64.fill(mapTile)
  val graphic      = Graphic(0, 0, TerminalText(AssetName("text"), RGBA.White))

  val suite = GuiSuite(
    Suite("TerminalEmulator Benchmarks")(
      Benchmark("put 4 - single") {
        terminal4.put(Point.zero, mapTile)
      },
      Benchmark("fill 4") {
        terminal4.fill(mapTile)
      },
      Benchmark("put 8 - single") {
        terminal8.put(Point.zero, mapTile)
      },
      Benchmark("fill 8") {
        terminal8.fill(mapTile)
      },
      Benchmark("put 16 - single") {
        terminal16.put(Point.zero, mapTile)
      },
      Benchmark("fill 16") {
        terminal16.fill(mapTile)
      },
      Benchmark("put 32 - single") {
        terminal32.put(Point.zero, mapTile)
      },
      Benchmark("fill 32") {
        terminal32.fill(mapTile)
      },
      Benchmark("put 64 - single") {
        terminal64.put(Point.zero, mapTile)
      },
      Benchmark("fill 64") {
        terminal64.fill(mapTile)
      },
      Benchmark("toCloneTiles - no material change") {
        fullTerminal.toCloneTiles(
          CloneId("test"),
          Point.zero,
          RoguelikeTiles.Size10x10.charCrops
        ) { case (fg, bg) =>
          graphic
        }
      },
      Benchmark("toCloneTiles - material change") {
        fullTerminal.toCloneTiles(
          CloneId("test"),
          Point.zero,
          RoguelikeTiles.Size10x10.charCrops
        ) { case (fg, bg) =>
          graphic.withMaterial(TerminalText(graphic.material.tileMap, fg, bg))
        }
      }
    )
  )
