package sandbox

import tyrian.*
import tyrian.syntax.*

object SandboxTerminal extends App[Unit, Model]:

  def init(args: List[String]): Result[Model] =
    Result(Model(None))
      .addActions(Action.fireAndForget(println("Starting my command line app!")))

  def update(model: Model): GlobalMsg => Result[Model] =
    case Msg.Tick(t) =>
      Result(model.copy(elapsed = Some(t)))

    case Msg.Quit =>
      Result(model).exit

    case Msg.NoOp =>
      Result(model)

  def view(model: Model): ConsoleFragment =
    model.elapsed match
      case None =>
        ConsoleFragment.println("Waiting...")

      case Some(t) =>
        ConsoleFragment.println(s"...tick (${t.toString()})")

  def watchers(model: Model): Batch[Watcher] =
    Batch(
      Watcher.every(1.second, t => Msg.Tick(t)),
      Watcher.timeout(5.seconds, Msg.Quit, "quit")
    )

  def extensions(args: List[String], model: Model): Set[Extension[Unit, ConsoleFragment]] =
    Set()

  def prepare: Unit =
    println("Getting ready...")

  def teardown: Unit =
    println("Goodbye!")

final case class Model(elapsed: Option[Seconds])

enum Msg extends GlobalMsg:
  case NoOp
  case Tick(t: Seconds)
  case Quit
