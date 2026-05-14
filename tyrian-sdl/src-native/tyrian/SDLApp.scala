package tyrian

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global
import indigoengine.shared.collections.Batch
import tyrian.GlobalMsg
import tyrian.Watcher
import tyrian.classic.Terminal
import tyrian.extensions.SDLExtension
import tyrian.extensions.SDLExtensionRegister
import tyrian.platform.Cmd
import tyrian.platform.Sub
import tyrian.runtime.SDLRuntime
import tyrian.runtime.TyrianSDLRuntime

import java.util.concurrent.ConcurrentLinkedQueue
import scala.annotation.nowarn

/** SDLApp is the equivalent of Tyrian's standard App, modified specifically for SDL based applications where the window
  * and graphics context must live on the main thread.
  */
@nowarn // TODO: Remove
trait SDLApp[Model]:

  // TODO: SDLApp doesn't have an onFrame yet, i.e. something that can render in the SDL graphics Context.

  // TODO: Wrap this up in something...
  def title: String
  def width: Int
  def height: Int

  /** Build the initial model and any startup commands. */
  def init(args: Array[String]): Result[Model]

  /** Fold a message into the model and emit follow-up commands. Runs on the main thread. */
  def update(model: Model): GlobalMsg => Result[Model]

  /** TODO */
  def view(model: Model): TerminalFragment

  /** Long-running message sources (timers, sockets, SDL events, etc.). Subscribed once at startup based on the initial
    * model.
    */
  def watchers(model: Model): Batch[Watcher]

  /** Extensions own per-frame rendering via [[SDLExtension.onFrame]], invoked on the main thread by the runtime. */
  def extensions(args: Array[String], model: Model): Set[SDLExtension]

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

  private val extensionsRegister: SDLExtensionRegister =
    new SDLExtensionRegister()

  // TODO: This seems backwards. I think the SDLApp should be run on top of the SDLRuntime.
  final def main(args: Array[String]): Unit =
    val runtime = SDLRuntime.create(title, width, height)
    SDLRuntime.current.set(runtime)

    val msgQueue = new ConcurrentLinkedQueue[SDLMsg]()

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

      extensionsRegister.onFrame(ctx, runningTime)
    }

    releaseDispatcher.unsafeRunSync()
    runtime.ctx.destroy()
