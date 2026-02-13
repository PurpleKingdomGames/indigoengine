package indigo

import indigo.gameengine.GameEngine
import org.scalajs.dom.Element

import scala.scalajs.js.annotation.*

trait MinimalLauncher[StartUpData, Model, ViewModel]:

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  protected var game: GameEngine[StartUpData, Model, ViewModel] = null

  protected def ready(flags: Map[String, String]): Element => GameEngine[StartUpData, Model, ViewModel]

  @JSExport
  def halt(): Unit =
    game.kill()
    ()

  def launch(element: Element, flags: Map[String, String]): Unit =
    game = ready(flags)(element)
    ()
