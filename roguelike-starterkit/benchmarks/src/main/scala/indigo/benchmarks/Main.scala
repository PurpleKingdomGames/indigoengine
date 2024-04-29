package indigo.benchmarks

import japgolly.scalajs.benchmark.gui.BenchmarkGUI
import org.scalajs.dom.document

import scala.scalajs.js.annotation.JSExportTopLevel

object Main:

  @JSExportTopLevel("main")
  def main(): Unit =
    val body = document.getElementById("body")
    BenchmarkGUI.renderMenu(body)(
      TerminalEmulatorBenchmarks.suite,
      RogueTerminalEmulatorBenchmarks.suite
    )
