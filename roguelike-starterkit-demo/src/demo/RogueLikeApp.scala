package demo

import cats.effect.IO
import demo.models.GameModel
import indigo.next.IndigoApp
import tyrian.*
import tyrian.Html.*
import tyrian.next.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object RogueLikeApp extends IndigoApp[AppModel, RogueLikeGame, GameModel]:

  // TODO: Is this alright? Better discovery method?
  def gameDivId: String = Constants.gameDivId.value

  def onBridge: String => Option[GlobalMsg] =
    msg => Some(IndigoReceive(msg))

  // Instead of doing this, ditch model wrapper, make the user hold the reference, and do game.subscribe for a watcher instance?
  def startGame(bridge: TyrianIndigoBridge[String, GameModel]): RogueLikeGame =
    RogueLikeGame(bridge.subSystem(Constants.gameDivId))

  def router: Location => GlobalMsg =
    Routing.none(NoOp())

  def init(flags: Map[String, String]): Result[AppModel] =
    Result(AppModel.init)

  def update(model: AppModel): GlobalMsg => Result[AppModel] =
    case IndigoReceive(msg) =>
      Result(model)
        .addCmds(
          Logger.consoleLog[IO]("Tyrian received a message from indigo: " + msg)
        )

    case _ =>
      Result(model)

  // TODO: Implement view.
  def view(model: AppModel): HtmlRoot =
    HtmlRoot(
      elements => div(id := gameDivId)(elements.toList).setKey(gameDivId),
      HtmlFragment.empty
    )

  def watchers(model: AppModel): Batch[Watcher] =
    Batch.empty

final case class IndigoReceive(msg: String) extends GlobalMsg
final case class NoOp()                     extends GlobalMsg

final case class AppModel()
object AppModel:
  val init: AppModel =
    AppModel()
