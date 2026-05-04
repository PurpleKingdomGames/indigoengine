package indigo

import indigo.*
import tyrian.*
import tyrian.ui.*

trait BasicGameRuntime extends App[Unit]:

  def game: Game[?, ?, ?]

  def settings: Indigo.Settings

  private val containerMarkerId = MarkerId("indigo-game-container")
  private given Theme           = Theme.None

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
    val surround: Batch[Elem[GlobalMsg]] => Html[GlobalMsg] =
      elems =>
        elems.headOption match
          case Some(elem) =>
            Container(
              HtmlElement.of(elem)
            )
              .withSize(
                Extent.CSS("100vw"),
                Extent.CSS("100vh")
              )
              .toHtml

          case None =>
            Container(
              TextBlock("No canvas element was built by the Indigo extension.")
            ).toHtml

    val fragment: HtmlFragment =
      HtmlFragment(
        Marker(containerMarkerId)
      )

    HtmlRoot(surround, fragment)

  def watchers(model: Unit): Batch[Watcher] =
    Batch.empty

enum AppMsg extends GlobalMsg:
  case NoOp
