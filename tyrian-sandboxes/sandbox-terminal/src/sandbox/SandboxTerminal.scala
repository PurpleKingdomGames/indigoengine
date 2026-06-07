package sandbox

import tyrian.*
import tyrian.syntax.*

object SandboxTerminal extends App[Unit, Model]:

  def init(args: Array[String]): Result[Model] =
    Result(Model(None))
      .addActions(Action.fireAndForget(println("Starting my command line app!")))

  def update(model: Model): GlobalMsg => Result[Model] =
    case Msg.Tick(t) =>
      Result(model.copy(elapsed = Some(t)))

    case Msg.NoOp =>
      Result(model)

  def view(model: Model): TerminalFragment =
    model.elapsed match
      case None =>
        TerminalFragment.empty

      case Some(t) =>
        TerminalFragment(
          Batch(
            TerminalOps.Print(s"...tick (${t.toString()})")
          )
        )

  def watchers(model: Model): Batch[Watcher] =
    Batch(
      Watcher.every(1.second, t => Msg.Tick(t))
    )

  def extensions(args: Array[String], model: Model): Set[Extension[Unit, TerminalFragment]] =
    Set()

final case class Model(elapsed: Option[Seconds])

enum Msg extends GlobalMsg:
  case NoOp
  case Tick(t: Seconds)
