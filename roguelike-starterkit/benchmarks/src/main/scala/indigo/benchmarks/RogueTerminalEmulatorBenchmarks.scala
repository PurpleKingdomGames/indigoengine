package indigo.benchmarks

import indigo.*
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*
import roguelikestarterkit.*
import roguelikestarterkit.terminal.MapTile

object RogueTerminalEmulatorBenchmarks:

  val terminal16     = RogueTerminalEmulator(Size(16))
  val terminal32     = RogueTerminalEmulator(Size(32))
  val terminal64     = RogueTerminalEmulator(Size(64))
  val mapTile        = MapTile(Tile.BLACK_HEART_SUIT)
  val fullTerminal16 = terminal16.fill(mapTile)
  val fullTerminal32 = terminal32.fill(mapTile)
  val fullTerminal64 = terminal64.fill(mapTile)
  val graphic        = Graphic(0, 0, TerminalText(AssetName("text"), RGBA.White))

  val suite = GuiSuite(
    Suite("RogueTerminalEmulator Benchmarks")(
      Benchmark("get 16 - single") {
        terminal16.get(Point.zero)
      },
      Benchmark("put 16 - single") {
        terminal16.put(Point.zero, mapTile)
      },
      Benchmark("put 16 - lines") {
        terminal16.putLine(Point.zero, "Hello, world!", RGBA.White, RGBA.Black)
      },
      Benchmark("fill 16") {
        terminal16.fill(mapTile)
      },
      Benchmark("inset 16") {
        terminal16.inset(fullTerminal16, Point.zero)
      },
      Benchmark("get 32 - single") {
        terminal32.get(Point.zero)
      },
      Benchmark("put 32 - single") {
        terminal32.put(Point.zero, mapTile)
      },
      Benchmark("put 32 - lines") {
        terminal32.putLine(Point.zero, "Hello, world!", RGBA.White, RGBA.Black)
      },
      Benchmark("fill 32") {
        terminal32.fill(mapTile)
      },
      Benchmark("inset 32") {
        terminal32.inset(fullTerminal32, Point.zero)
      },
      Benchmark("get 64 - single") {
        terminal64.get(Point.zero)
      },
      Benchmark("put 64 - single") {
        terminal64.put(Point.zero, mapTile)
      },
      Benchmark("put 64 - lines") {
        terminal64.putLine(Point.zero, "Hello, world!", RGBA.White, RGBA.Black)
      },
      Benchmark("fill 64") {
        terminal64.fill(mapTile)
      },
      Benchmark("inset 64") {
        terminal64.inset(fullTerminal64, Point.zero)
      },
      Benchmark("toCloneTiles 16 - no material change") {
        fullTerminal16.toCloneTiles(
          CloneId("test"),
          Point.zero,
          RoguelikeTiles.Size10x10.charCrops
        ) { case (fg, bg) =>
          graphic
        }
      },
      Benchmark("toCloneTiles 16 - material change") {
        fullTerminal16.toCloneTiles(
          CloneId("test"),
          Point.zero,
          RoguelikeTiles.Size10x10.charCrops
        ) { case (fg, bg) =>
          graphic.withMaterial(TerminalText(graphic.material.tileMap, fg, bg))
        }
      },
      Benchmark("toCloneTiles 32 - no material change") {
        fullTerminal32.toCloneTiles(
          CloneId("test"),
          Point.zero,
          RoguelikeTiles.Size10x10.charCrops
        ) { case (fg, bg) =>
          graphic
        }
      },
      Benchmark("toCloneTiles 32 - material change") {
        fullTerminal32.toCloneTiles(
          CloneId("test"),
          Point.zero,
          RoguelikeTiles.Size10x10.charCrops
        ) { case (fg, bg) =>
          graphic.withMaterial(TerminalText(graphic.material.tileMap, fg, bg))
        }
      },
      Benchmark("toCloneTiles 64 - no material change") {
        fullTerminal64.toCloneTiles(
          CloneId("test"),
          Point.zero,
          RoguelikeTiles.Size10x10.charCrops
        ) { case (fg, bg) =>
          graphic
        }
      },
      Benchmark("toCloneTiles 64 - material change") {
        fullTerminal64.toCloneTiles(
          CloneId("test"),
          Point.zero,
          RoguelikeTiles.Size10x10.charCrops
        ) { case (fg, bg) =>
          graphic.withMaterial(TerminalText(graphic.material.tileMap, fg, bg))
        }
      }
    )
  )
