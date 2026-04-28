package indigo

import indigo.*
import tyrian.*

trait BasicGameRuntime extends App[Unit]:

  def game: Game[?, ?, ?]

  def settings: Indigo.Settings

  private val containerMarkerId = MarkerId("indigo-game-container")

  def extensions(flags: Map[String, String], model: Unit): Set[Extension] =
    Set(
      Indigo(
        ExtensionId("indigo game"),
        flags,
        game,
        containerMarkerId
      ).withSettings(settings)
    )

  def init(flags: Map[String, String]): Result[Unit] =
    Result(())

  def router: Location => GlobalMsg =
    Routing.none(AppMsg.NoOp)

  def update(model: Unit): GlobalMsg => Result[Unit] =
    case m: AppMsg =>
      handleAppMsg(model)(m)

    case _ =>
      Result(model)

  def handleAppMsg(model: Unit): AppMsg => Result[Unit] =
    case AppMsg.NoOp =>
      Result(model)

  def view(model: Unit): HtmlRoot =
    HtmlRoot.div(
      HtmlFragment(
        Marker(containerMarkerId)
      )
    )

  def watchers(model: Unit): Batch[Watcher] =
    Batch.empty

enum AppMsg extends GlobalMsg:
  case NoOp
