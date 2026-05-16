package indigo

import indigo.*
import tyrian.*
import tyrian.extensions.Extension

abstract class BasicGameRuntime(game: => Game[?, ?, ?]) extends SDLApp[Unit]:

  def extensions(args: Array[String], model: Unit): Set[Extension.Graphical[SDLContext]] =
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
