package indigo.next

import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.unsafe.implicits.global
import indigo.next.IndigoNext
import indigoengine.shared.collections.Batch
import org.scalajs.dom.Element
import org.scalajs.dom.document
import org.scalajs.dom.window
import tyrian.Cmd
import tyrian.Html
import tyrian.Location
import tyrian.Sub
import tyrian.TyrianApp
import tyrian.bridge.TyrianIndigoBridge
import tyrian.next.Action
import tyrian.next.GlobalMsg
import tyrian.next.HtmlRoot
import tyrian.next.Result
import tyrian.next.Watcher

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*

trait IndigoApp[AppModel, Game <: IndigoNext[?, ?, ?], GameModel]:

  // TODO: Maybe we need a 'game container detect' function?
  def gameDivId: String

  def onBridge: String => Option[GlobalMsg]

  def startGame(bridge: TyrianIndigoBridge[IO, String, GameModel]): Game

  /** Specifies the number of queued tasks that can be consumed at any one time. Default is 1024 which is assumed to be
    * more than sufficient, however the value can be tweaked in your app by overriding this value.
    */
  def MaxConcurrentTasks: Int = 1024

  /** The `routing` function is typically implemented using the `Routing` helper. Used to decide how to manage what
    * happens when the user clicks a link. Links are split in the `Location` object into 'internal' and 'external'
    * types.
    */
  def router: Location => GlobalMsg

  /** Used to initialise your app. Accepts simple flags and produces the initial model state, along with any actions to
    * run at start up, in order to trigger other processes.
    */
  def init(flags: Map[String, String]): Result[AppModel]

  /** The update method allows you to modify the model based on incoming messages (events). As well as an updated model,
    * you can also produce actions to run.
    */
  def update(model: AppModel): GlobalMsg => Result[AppModel]

  /** Used to render your current model into an HTML view.
    */
  def view(model: AppModel): HtmlRoot

  /** Watchers are typically processes that run for a period of time and emit discrete events based on some world event,
    * e.g. a mouse moving might emit it's coordinates.
    */
  def watchers(model: AppModel): Batch[Watcher]

  /** Launch the app and attach it to an element with the given id. Can be called from Scala or JavaScript.
    */
  @JSExport
  def launch(containerId: String): Unit =
    runReadyOrError(containerId, Map[String, String]())

  /** Launch the app and attach it to the given element. Can be called from Scala or JavaScript.
    */
  @JSExport
  def launch(node: Element): Unit =
    ready(node, Map[String, String]())

  /** Launch the app and attach it to an element with the given id, with the supplied simple flags. Can be called from
    * Scala or JavaScript.
    */
  @JSExport
  def launch(containerId: String, flags: scala.scalajs.js.Dictionary[String]): Unit =
    runReadyOrError(containerId, flags.toMap)

  /** Launch the app and attach it to the given element, with the supplied simple flags. Can be called from Scala or
    * JavaScript.
    */
  @JSExport
  def launch(node: Element, flags: scala.scalajs.js.Dictionary[String]): Unit =
    ready(node, flags.toMap)

  /** Launch the app and attach it to an element with the given id, with the supplied simple flags. Can only be called
    * from Scala.
    */
  def launch(containerId: String, flags: Map[String, String]): Unit =
    runReadyOrError(containerId, flags)

  /** Launch the app and attach it to the given element, with the supplied simple flags. Can only be called from Scala.
    */
  def launch(node: Element, flags: Map[String, String]): Unit =
    ready(node, flags)

  val run: IO[Nothing] => Unit = _.unsafeRunAndForget()

  private def routeCurrentLocation(router: Location => GlobalMsg): Cmd[IO, GlobalMsg] =
    val task =
      Async[IO].delay {
        Location.fromJsLocation(window.location)
      }
    Cmd.Run(task, router)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def _init(flags: Map[String, String]): (ModelWrapper, Cmd[IO, GlobalMsg]) =
    val (m, cmd) = init(flags) match
      case Result.Next(state, actions) =>
        (state, Action.internal.Many(actions).toCmd |+| routeCurrentLocation(router))

      case e @ Result.Error(err, _) =>
        println(e.reportCrash)
        throw err

    (
      ModelWrapper(m, None, TyrianIndigoBridge[IO, String, GameModel]()), // TODO: String?
      cmd |+| routeCurrentLocation(router) |+| Cmd.Emit(IndigoBootMsg.Start)
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def _update(
      model: ModelWrapper
  ): GlobalMsg => (ModelWrapper, Cmd[IO, GlobalMsg]) =
    case IndigoBootMsg.Register(game) =>
      (model.copy(game = Some(game)), Cmd.None)

    case IndigoBootMsg.Start =>
      val task: IO[GlobalMsg] =
        IO.delay {
          if gameDivsExist(gameDivId) then
            println("Indigo container divs ready, launching games.")
            val game: Game =
              startGame(model.bridge)

            game.launch(gameDivId)

            IndigoBootMsg.Register(game)
          else
            println("Indigo container divs not ready, retrying...")
            IndigoBootMsg.Retry
        }

      (model, Cmd.Run(task))

    case IndigoBootMsg.Retry =>
      (model, Cmd.emitAfterDelay(IndigoBootMsg.Start, 0.5.seconds))

    case msg =>
      update(model.app)(msg) match
        case Result.Next(state, actions) =>
          model.copy(app = state) -> Action.internal.Many(actions).toCmd

        case e @ Result.Error(err, _) =>
          println(e.reportCrash)
          throw err

  private def _view(model: ModelWrapper): Html[GlobalMsg] =
    view(model.app).toHtml

  private def onUrlChange(router: Location => GlobalMsg): Watcher =
    def makeMsg = Option(router(Location.fromJsLocation(window.location)))
    Watcher.internal.Many(
      Watcher.fromEvent("DOMContentLoaded", window)(_ => makeMsg),
      Watcher.fromEvent("popstate", window)(_ => makeMsg)
    )

  private def _subscriptions(model: ModelWrapper): Sub[IO, GlobalMsg] =
    Watcher.internal
      .Many(
        onUrlChange(router) :: watchers(model.app)
      )
      .toSub |+| model.bridge.subscribe {
      onBridge
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def gameDivsExist(id: String): Boolean =
    document.getElementById(id) != null

  def ready(node: Element, flags: Map[String, String]): Unit =
    run(
      TyrianApp.start[IO, ModelWrapper, GlobalMsg](
        node,
        router,
        _init(flags),
        _update,
        _view,
        _subscriptions
      )
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def runReadyOrError(containerId: String, flags: Map[String, String]): Unit =
    Option(document.getElementById(containerId)) match
      case Some(e) =>
        ready(e, flags)

      case None =>
        throw new Exception(s"Missing Element! Could not find an element with id '$containerId' on the page.")

  enum IndigoBootMsg extends GlobalMsg:
    case Start, Retry
    case Register(game: Game)

  final case class ModelWrapper(
      app: AppModel,
      game: Option[Game],
      bridge: TyrianIndigoBridge[IO, String, GameModel]
  )
