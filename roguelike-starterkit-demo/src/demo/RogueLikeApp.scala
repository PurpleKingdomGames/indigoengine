package demo

import indigo.next.*
import org.scalajs.dom.document
import tyrian.*
import tyrian.Html.*
import tyrian.syntax.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object RogueLikeApp extends TyrianNext[AppModel]:

  def gameDivId: String = Constants.gameDivId

  def router: Location => GlobalMsg =
    Routing.none(AppMsg.NoOp)

  def init(flags: Map[String, String]): Result[AppModel] =
    Result(AppModel.init)

  def update(model: AppModel): GlobalMsg => Result[AppModel] =
    case GameMsg.MakeIndigoLog(msg) =>
      Result(model)
        .addGlobalMsgs(
          BridgeMsg.Send(MsgData.Log(msg))
        )

    case BridgeMsg.Receive(data) =>
      Result(model)
        .log("Tyrian got this from Indigo: " + data)

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
    Batch(
      Watcher.every(
        5.seconds,
        t => GameMsg.MakeIndigoLog(s"From Tyrian: ${t.toUTCString()}")
      )
    )

  def extensions(model: AppModel): Set[Extension] =
    Set(
      Indigo(
        ExtensionId("rogue game"),
        model.game,
        () => Option(document.getElementById(gameDivId)),
        AppMsg.Log("Game start success."),
        AppMsg.Log("Game start fail.")
      )
    )

enum AppMsg extends GlobalMsg:
  case NoOp
  case Log(msg: String)

enum GameMsg extends GlobalMsg:
  case MakeIndigoLog(msg: String)

final case class AppModel(game: RogueLikeGame)
object AppModel:
  val init: AppModel =
    AppModel(RogueLikeGame())
