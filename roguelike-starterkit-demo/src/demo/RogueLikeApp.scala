package demo

import indigo.*
import org.scalajs.dom.document
import tyrian.*
import tyrian.Html.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object RogueLikeApp extends App[AppModel]:

  def gameDivId: String = Constants.gameDivId

  def router: Location => GlobalMsg =
    Routing.none(AppMsg.NoOp)

  def init(flags: Map[String, String]): Result[AppModel] =
    Result(AppModel.init)

  def update(model: AppModel): GlobalMsg => Result[AppModel] =
    case AppMsg.NoOp =>
      Result(model)

    case AppMsg.Log(msg) =>
      Result(model).log(msg)

    case _ =>
      Result(model)

  def view(model: AppModel): HtmlRoot =
    HtmlRoot.div(
      HtmlFragment(
        div(id := gameDivId)().setKey(gameDivId)
      )
    )

  def watchers(model: AppModel): Batch[Watcher] =
    Batch()

  def extensions(flags: Map[String, String], model: AppModel): Set[Extension] =
    Set(
      Indigo(
        ExtensionId("rogue game"),
        flags,
        model.game,
        () => Option(document.getElementById(gameDivId)),
        AppMsg.Log("Game start success."),
        AppMsg.Log("Game start fail.")
      )
    )

enum AppMsg extends GlobalMsg:
  case NoOp
  case Log(msg: String)

final case class AppModel(game: RogueLikeGame)
object AppModel:
  val init: AppModel =
    AppModel(RogueLikeGame())
