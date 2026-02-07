package tyrian

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import indigoengine.shared.collections.Batch
import org.scalajs.dom.Element
import org.scalajs.dom.document
import org.scalajs.dom.window
import tyrian.Html
import tyrian.Location
import tyrian.classic.Cmd
import tyrian.classic.Sub
import tyrian.classic.TyrianApp
import tyrian.extensions.Extension
import tyrian.extensions.ExtensionRegister

import scala.scalajs.js.annotation.*

trait App[Model]:

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
  def init(flags: Map[String, String]): Result[Model]

  /** The update method allows you to modify the model based on incoming messages (events). As well as an updated model,
    * you can also produce actions to run.
    */
  def update(model: Model): GlobalMsg => Result[Model]

  /** Used to render your current model into an HTML view.
    */
  def view(model: Model): HtmlRoot

  /** Watchers are typically processes that run for a period of time and emit discrete events based on some world event,
    * e.g. a mouse moving might emit it's coordinates.
    */
  def watchers(model: Model): Batch[Watcher]

  /** Extensions are mini-apps that are mechanically combined with your main application in the background. Extensions
    * are provided the initial model to assist them during start-up.
    *
    * @param model
    *   The initial app model. Only provided once.
    */
  def extensions(flags: Map[String, String], model: Model): Set[Extension]

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
      IO.delay {
        Location.fromJsLocation(window.location)
      }
    Cmd.Run(task, router)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def _init(flags: Map[String, String]): (Model, Cmd[IO, GlobalMsg]) =
    init(flags) match
      case Result.Next(state, actions) =>
        (state, Action.internal.Many(actions).toCmd |+| routeCurrentLocation(router))

      case e @ Result.Error(err, _) =>
        println(e.reportCrash)
        throw err

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  protected def _update(
      model: Model
  ): GlobalMsg => (Model, Cmd[IO, GlobalMsg]) =
    case msg =>
      update(model)(msg) match
        case Result.Next(state, actions) =>
          state -> Action.internal.Many(actions).toCmd

        case e @ Result.Error(err, _) =>
          println(e.reportCrash)
          throw err

  private def onUrlChange(router: Location => GlobalMsg): Watcher =
    def makeMsg = Option(router(Location.fromJsLocation(window.location)))
    Watcher.internal.Many(
      Watcher.fromEvent("DOMContentLoaded", window)(_ => makeMsg),
      Watcher.fromEvent("popstate", window)(_ => makeMsg)
    )

  private def _subscriptions(model: Model): Sub[IO, GlobalMsg] =
    Watcher.internal
      .Many(
        onUrlChange(router) :: watchers(model)
      )
      .toSub

  private val extensionsRegister: ExtensionRegister =
    new ExtensionRegister()

  def ready(node: Element, flags: Map[String, String]): Unit =

    val (initModel, initCmds) = _init(flags)

    val extensionsCmds = extensionsRegister.register(Batch.fromSet(extensions(flags, initModel))).map(_.toCmd)

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def combinedUpdate(
        model: Model
    ): GlobalMsg => (Model, Cmd[IO, GlobalMsg]) =
      msg =>
        val (m, as) = _update(model)(msg)
        val extCmds = extensionsRegister.update(msg).map { actions =>
          val cmds = actions.map(_.toCmd)
          if cmds.isEmpty then Cmd.None
          else
            val head = cmds.head
            cmds.tail.foldLeft(head)(_ |+| _)
        }

        extCmds match {
          case Result.Error(e, _) =>
            throw e

          case Result.Next(eCmds, _) =>
            m -> (as |+| eCmds)
        }

    def combinedView(model: Model): Html[GlobalMsg] =
      view(model).addHtmlFragments(extensionsRegister.view).toHtml

    def combinedSubscriptions(model: Model): Sub[IO, GlobalMsg] =
      _subscriptions(model) |+| Watcher.internal.Many(extensionsRegister.watchers).toSub

    run(
      TyrianApp.start[IO, Model, GlobalMsg](
        node,
        router,
        initModel -> (initCmds |+| Cmd.Batch(extensionsCmds.toList)),
        combinedUpdate,
        combinedView,
        combinedSubscriptions
      )
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def runReadyOrError(containerId: String, flags: Map[String, String]): Unit =
    Option(document.getElementById(containerId)) match
      case Some(e) =>
        ready(e, flags)

      case None =>
        throw new Exception(s"Missing Element! Could not find an element with id '$containerId' on the page.")
