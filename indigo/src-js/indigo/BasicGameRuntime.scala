package indigo

import indigo.*
import indigo.launchers.GameLauncher
import tyrian.*
import tyrian.Html.*

abstract class BasicGameRuntime(game: => Game[?, ?, ?] | GameLauncher[?, ?]) extends App[Unit]:

  val gameContainerId = "indigo-game-container"

  def extensions(flags: Map[String, String], model: Unit): Set[Extension] =
    Set(
      Indigo(
        ExtensionId("indigo game"),
        flags,
        game,
        gameContainerId
      )
    )

  def init(flags: Map[String, String]): Result[Unit] =
    Result(())

  def router: Location => GlobalMsg =
    Routing.none(AppMsg.NoOp)

  def update(model: Unit): GlobalMsg => Result[Unit] =
    _ => Result(model)

  def view(model: Unit): HtmlRoot =
    HtmlRoot.div(
      HtmlFragment(
        div(id := gameContainerId)().setKey(gameContainerId)
      )
    )

  def watchers(model: Unit): Batch[Watcher] =
    Batch.empty

enum AppMsg extends GlobalMsg:
  case NoOp
