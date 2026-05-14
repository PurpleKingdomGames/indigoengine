package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.kernel.Clock
import cats.effect.kernel.Ref
import cats.effect.std.AtomicCell
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.effect.syntax.all.*
import cats.syntax.all.*
import tyrian.Location
import tyrian.platform.Cmd
import tyrian.platform.Sub
import tyrian.platform.runtime.CmdHelper
import tyrian.platform.runtime.PresentView
import tyrian.platform.runtime.SubHelper

import scala.util.control.NonFatal

// TODO: Simplify, we know some of these types.
final class TyrianSDLRuntime[F[_], Model, Msg, View[Msg], ViewState](
    dispatcher: Dispatcher[F],
    _model: Ref[F, Model],
    _currentSubs: AtomicCell[F, List[(String, F[Unit])]],
    _msgQueue: Queue[F, Msg],
    _renderer: Ref[F, ViewState]
)(using F: Async[F], clock: Clock[F], present: PresentView[View, ViewState]):

  def start(initCmds: Cmd[F, Msg], initSubs: Model => Sub[F, Msg]): F[Unit] =
    val runCmd = runCommands(_msgQueue)
    val runSub = runSubscriptions(_currentSubs, _msgQueue, dispatcher)

    _model.get.flatMap: m =>
      runCmd(initCmds) *> runSub(initSubs(m))

  def tick(
      update: Model => Msg => (Model, Cmd[F, Msg]),
      view: Model => View[Msg],
      subscriptions: Model => Sub[F, Msg]
  ): F[Unit] =
    val router: Location => Option[Msg] = _ => None
    val runCmd                          = runCommands(_msgQueue)
    val runSub                          = runSubscriptions(_currentSubs, _msgQueue, dispatcher)
    val onMsg                           = postMsg(dispatcher, _msgQueue)

    // TODO: Magic number, make it a constant. Isn't there one already somewhere?
    val processQueued: F[Unit] =
      _msgQueue.tryTakeN(Some(256)).flatMap { msgs =>
        msgs.traverse_ { msg =>
          _model
            .modify { oldModel =>
              val (newModel, cmd) =
                update(oldModel)(msg)

              (newModel, (cmd, subscriptions(newModel)))
            }
            .flatMap { case (cmd, sub) => runCmd(cmd) *> runSub(sub) }
        }
      }

    processQueued *> present.draw(dispatcher, _renderer, _model, view, onMsg, router)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def runCommands[F[_], Msg](msgQueue: Queue[F, Msg])(cmd: Cmd[F, Msg])(using F: Async[F]): F[Unit] =
    CmdHelper.cmdToTaskList(cmd).foldMapM { task =>
      task
        .handleError {
          case NonFatal(e) =>
            println(e.getMessage)
            None

          case e =>
            throw e
        }
        .flatMap(_.traverse_(msgQueue.offer(_)))
        .start
        .void
    }

  def runSubscriptions[F[_], Msg](
      currentSubs: AtomicCell[F, List[(String, F[Unit])]],
      msgQueue: Queue[F, Msg],
      dispatcher: Dispatcher[F]
  )(sub: Sub[F, Msg])(using F: Async[F]): F[Unit] =
    currentSubs.evalUpdate { oldSubs =>
      val allSubs                 = SubHelper.flatten(sub)
      val (stillAlive, discarded) = SubHelper.aliveAndDead(allSubs, oldSubs)

      val newSubs = SubHelper
        .findNewSubs(allSubs, stillAlive.map(_._1), Nil)
        .traverse(
          SubHelper.runObserve(_) { result =>
            dispatcher.unsafeRunAndForget(
              result.toOption.flatten.foldMapM(msgQueue.offer(_).void)
            )
          }
        )

      discarded.foldMapM(_.start.void) *> newSubs.map(_ ++ stillAlive)
    }

  def postMsg[F[_], Msg](dispatcher: Dispatcher[F], msgQueue: Queue[F, Msg]): Msg => Unit =
    msg => dispatcher.unsafeRunAndForget(msgQueue.offer(msg))

object TyrianSDLRuntime:

  // TODO: Simplify, we know some of these types.
  def make[F[_], Model, Msg, View[Msg], ViewState](
      dispatcher: Dispatcher[F],
      initModel: Model,
      viewState: ViewState
  )(using
      F: Async[F],
      clock: Clock[F],
      present: PresentView[View, ViewState]
  ): F[TyrianSDLRuntime[F, Model, Msg, View, ViewState]] =
    for {
      model       <- F.ref(initModel)
      currentSubs <- AtomicCell[F].of(List.empty[(String, F[Unit])])
      msgQueue    <- Queue.unbounded[F, Msg]
      renderer    <- F.ref(viewState)
    } yield new TyrianSDLRuntime(dispatcher, model, currentSubs, msgQueue, renderer)
