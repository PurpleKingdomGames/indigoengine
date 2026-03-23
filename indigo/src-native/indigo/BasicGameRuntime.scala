package indigo

import indigo.*
import indigo.launchers.GameLauncher
import tyrian.*

abstract class BasicGameRuntime(game: => Game[?, ?, ?] | GameLauncher[?, ?]) extends App[Unit]:

  def extensions(args: Array[String], model: Unit): Set[Extension] =
    Set(
      Indigo(
        ExtensionId("indigo game"),
        args,
        game
      )
    )

  def init(args: Array[String]): Result[Unit] =
    Result(())

  def update(model: Unit): GlobalMsg => Result[Unit] =
    _ => Result(model)

  def view(model: Unit): TerminalFragment =
    TerminalFragment.empty

  def watchers(model: Unit): Batch[Watcher] =
    Batch.empty

enum AppMsg extends GlobalMsg:
  case NoOp
