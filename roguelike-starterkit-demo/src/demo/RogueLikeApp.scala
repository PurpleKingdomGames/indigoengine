package demo

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
      .addGlobalMsgs(GameMsg.AttemptStart(30)) // TODO: Big number, might want a way to emit after delay.

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def update(model: AppModel): GlobalMsg => Result[AppModel] =
    case GameMsg.AttemptStart(remaining) if remaining <= 0 =>
      Result.raiseError(new Exception("Game div was not found."))

    case GameMsg.AttemptStart(remaining) =>
      Result(model)
        .addActions(
          Action.run {
            val elem = document.getElementById(gameDivId)

            // TODO: Maybe we delegate this to Indigo, and lauch does the null check reporting success or failure?
            if elem != null then
              model.game.launch(elem)
              GameMsg.Started
            else GameMsg.AttemptStart(remaining - 1)
          }
        )
        .log("Attempts remaining: " + remaining)

    case GameMsg.Started =>
      Result(model).log("Game started!")

    case GameMsg.MakeIndigoLog(msg) =>
      Result(model)
        .addActions(
          model.game.bridge.send(GameEvent.Log(msg))
        )

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
      model.game.bridge.watch,
      Watcher.every(
        5.seconds,
        t => GameMsg.MakeIndigoLog(s"From Tyrian: ${t.toUTCString()}")
      )
    )

  def extensions: Set[Extension] =
    Set() // TODO

enum AppMsg extends GlobalMsg:
  case NoOp
  case Log(msg: String)

enum GameMsg extends GlobalMsg:
  case AttemptStart(remaining: Int)
  case Started
  case MakeIndigoLog(msg: String)

final case class AppModel(game: RogueLikeGame)
object AppModel:
  val init: AppModel =
    AppModel(RogueLikeGame())
