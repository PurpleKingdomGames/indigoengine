package tyrian.internal

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import indigoengine.shared.collections.Batch
import tyrian.Action
import tyrian.GlobalMsg
import tyrian.Result
import tyrian.TerminalFragment
import tyrian.Watcher
import tyrian.classic.Terminal
import tyrian.classic.TyrianApp
import tyrian.extensions.Extension
import tyrian.extensions.ExtensionRegister
import tyrian.platform.Cmd
import tyrian.platform.Sub

trait AppBase[GraphicsContext, Model] extends IOApp:

  /** Used to initialise your app. Accepts simple flags and produces the initial model state, along with any actions to
    * run at start up, in order to trigger other processes.
    */
  def init(args: List[String]): Result[Model]

  /** The update method allows you to modify the model based on incoming messages (events). As well as an updated model,
    * you can also produce actions to run.
    */
  def update(model: Model): GlobalMsg => Result[Model]

  /** Used to render your current model into a view.
    */
  def view(model: Model): TerminalFragment

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
  def extensions(args: List[String], model: Model): Set[Extension[GraphicsContext, TerminalFragment]]

  /** Invoked when terminal apps exit. Provides an opportunity for sign-off messages to the user, or for clean up
    * side-effects to take place.
    *
    * Note: `teardown` may not be invoked if you run the native version through your build tool, but will be invoked if
    * you run the executable directly.
    */
  def teardown: Unit

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def _init(args: List[String]): (Model, Cmd[IO, GlobalMsg]) =
    init(args) match
      case Result.Next(state, actions) =>
        (state, Action.internal.Many(actions).toCmd)

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
          findExit(actions) match
            case Some(code) =>
              throw ExitSignal(code)

            case None =>
              state -> Action.internal.Many(actions).toCmd

        case e @ Result.Error(err, _) =>
          println(e.reportCrash)
          throw err

  private def findExit(actions: Batch[Action]): Option[ExitCode] =
    actions.collectFirst {
      case Action.Exit(code)          => Some(code)
      case Action.internal.Many(more) => findExit(more)
    }.flatten

  private def _subscriptions(model: Model): Sub[IO, GlobalMsg] =
    Watcher.internal.Many(watchers(model)).toSub

  private[tyrian] val extensionsRegister: ExtensionRegister[GraphicsContext, TerminalFragment] =
    new ExtensionRegister()

  def appStart(args: List[String]): IO[Nothing] =
    val (initModel, initCmds) =
      _init(args)

    val extensionsCmds =
      extensionsRegister.register(Batch.fromSet(extensions(args, initModel))).map(_.toCmd)

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

    def combinedView(model: Model): Terminal[GlobalMsg] =
      (view(model) |+| extensionsRegister.view).toTerminal

    def combinedSubscriptions(model: Model): Sub[IO, GlobalMsg] =
      _subscriptions(model) |+| Watcher.internal.Many(extensionsRegister.watchers).toSub

    TyrianApp.start[IO, Model, GlobalMsg](
      initModel -> (initCmds |+| Cmd.Batch(extensionsCmds.toList)),
      combinedUpdate,
      combinedView,
      combinedSubscriptions
    )

private[tyrian] final case class ExitSignal(code: ExitCode) extends RuntimeException
