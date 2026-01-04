package demo

import indigo.next.Indigo
import org.scalajs.dom.document
import tyrian.*
import tyrian.Html.*
import tyrian.next.*
import tyrian.next.syntax.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object RogueLikeApp extends TyrianNext[AppModel]:

  def gameDivId: String = Constants.gameDivId.value

  def router: Location => GlobalMsg =
    Routing.none(AppMsg.NoOp)

  def init(flags: Map[String, String]): Result[AppModel] =
    Result(AppModel.init)

  def update(model: AppModel): GlobalMsg => Result[AppModel] =
    case GameMsg.MakeIndigoLog(msg) =>
      Result(model) // TODO
        // .addActions( 
        //   model.game.bridge.send(GameEvent.Log(msg))
        // )

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

  def extensions: Set[Extension] =
    Set(
      Indigo(
        ExtensionId("rogue game"),
        RogueLikeGame(),
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

final case class AppModel()
object AppModel:
  val init: AppModel =
    AppModel()
