package tyrian

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global
import indigoengine.shared.collections.Batch
import tyrian.GlobalMsg
import tyrian.Watcher
import tyrian.classic.Terminal
import tyrian.extensions.Extension
import tyrian.extensions.ExtensionRegister
import tyrian.platform.Cmd
import tyrian.platform.Sub
import tyrian.runtime.SDLRuntime
import tyrian.runtime.TyrianSDLRuntime

/** SDLApp is the equivalent of Tyrian's standard App, modified specifically for SDL based applications where the window
  * and graphics context must live on the main thread.
  *
  * The SDL graphics context is not available to the main Tyrian app, which presents to the terminal as usual, instead
  * it is passed to the extensions `draw` operation.
  */
trait SDLApp[Model]:

  // TODO: Wrap this up in something...
  def title: String
  def width: Int
  def height: Int

  /** Used to initialise your app. Accepts simple flags and produces the initial model state, along with any actions to
    * run at start up, in order to trigger other processes.
    */
  def init(args: Array[String]): Result[Model]

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

  /** Extensions own per-frame rendering, invoked on the main thread by the runtime. */
  def extensions(args: Array[String], model: Model): Set[Extension[SDLContext, TerminalFragment]]

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def _init(args: Array[String]): (Model, Cmd[IO, GlobalMsg]) =
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
          state -> Action.internal.Many(actions).toCmd

        case e @ Result.Error(err, _) =>
          println(e.reportCrash)
          throw err

  private def _subscriptions(model: Model): Sub[IO, GlobalMsg] =
    Watcher.internal.Many(watchers(model)).toSub

  private val extensionsRegister: ExtensionRegister[SDLContext, TerminalFragment] =
    new ExtensionRegister()

  final def main(args: Array[String]): Unit =
    val runtime = SDLRuntime.create(title, width, height)
    SDLRuntime.current.set(runtime)

    val (dispatcher, releaseDispatcher) =
      Dispatcher.parallel[IO].allocated.unsafeRunSync()

    val (initModel, initCmds) = _init(args)

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

    val tyrianSDLRuntime =
      TyrianSDLRuntime
        .make[IO, Model, GlobalMsg, Terminal, Unit](
          dispatcher,
          initModel,
          ()
        )
        .unsafeRunSync()

    tyrianSDLRuntime
      .start(
        initCmds |+| Cmd.Batch(extensionsCmds.toList),
        combinedSubscriptions
      )
      .unsafeRunSync()

    runtime.run { (ctx, runningTime) =>
      tyrianSDLRuntime
        .tick(
          combinedUpdate,
          combinedView,
          combinedSubscriptions
        )
        .unsafeRunSync()

      extensionsRegister.draw(Option(ctx), runningTime)
    }

    releaseDispatcher.unsafeRunSync()
    runtime.ctx.destroy()
