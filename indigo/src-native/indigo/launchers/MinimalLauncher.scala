package indigo.launchers

import indigo.platform.gameengine.GameEngine

trait MinimalLauncher[StartUpData, Model]:

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  protected var game: GameEngine[StartUpData, Model] = null

  protected def ready(args: Array[String]): GameEngine[StartUpData, Model]

  def halt(): Unit =
    game.kill()
    ()

  def launch(args: Array[String]): Unit =
    game = ready(args)
    ()
