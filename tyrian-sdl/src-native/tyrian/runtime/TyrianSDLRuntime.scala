package tyrian.runtime

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.std.AtomicCell
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.syntax.all.*
import tyrian.GlobalMsg
import tyrian.Location
import tyrian.classic.Console
import tyrian.platform.Cmd
import tyrian.platform.Sub
import tyrian.platform.runtime.CmdHelper
import tyrian.platform.runtime.PresentView
import tyrian.platform.runtime.SubHelper

import scala.util.control.NonFatal

final class TyrianSDLRuntime[Model](
    dispatcher: Dispatcher[IO],
    _model: Ref[IO, Model],
    _currentSubs: AtomicCell[IO, List[(String, IO[Unit])]],
    _msgQueue: Queue[IO, GlobalMsg],
    _viewState: Ref[IO, Unit]
)(using present: PresentView[Console, Unit]):

  def start(initCmds: Cmd[IO, GlobalMsg], initSubs: Model => Sub[IO, GlobalMsg]): IO[Unit] =
    val runCmd = runCommands(_msgQueue)
    val runSub = runSubscriptions(_currentSubs, _msgQueue, dispatcher)

    _model.get.flatMap: m =>
      runCmd(initCmds) *> runSub(initSubs(m))

  def tick(
      update: Model => GlobalMsg => (Model, Cmd[IO, GlobalMsg]),
      view: Model => Console[GlobalMsg],
      subscriptions: Model => Sub[IO, GlobalMsg]
  ): IO[Unit] =
    val router: Location => Option[GlobalMsg] = _ => None
    val runCmd                                = runCommands(_msgQueue)
    val runSub                                = runSubscriptions(_currentSubs, _msgQueue, dispatcher)
    val onMsg                                 = postMsg(dispatcher, _msgQueue)

    // TODO: Magic number, make it a constant. Isn't there one already somewhere?
    val processQueued: IO[Unit] =
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

    processQueued *> present.draw(dispatcher, _viewState, _model, view, onMsg, router)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def runCommands[GlobalMsg](msgQueue: Queue[IO, GlobalMsg])(cmd: Cmd[IO, GlobalMsg]): IO[Unit] =
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

  def runSubscriptions[GlobalMsg](
      currentSubs: AtomicCell[IO, List[(String, IO[Unit])]],
      msgQueue: Queue[IO, GlobalMsg],
      dispatcher: Dispatcher[IO]
  )(sub: Sub[IO, GlobalMsg]): IO[Unit] =
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

  def postMsg[GlobalMsg](dispatcher: Dispatcher[IO], msgQueue: Queue[IO, GlobalMsg]): GlobalMsg => Unit =
    msg => dispatcher.unsafeRunAndForget(msgQueue.offer(msg))

object TyrianSDLRuntime:

  def make[Model](
      dispatcher: Dispatcher[IO],
      initModel: Model
  )(using
      present: PresentView[Console, Unit]
  ): IO[TyrianSDLRuntime[Model]] =
    for {
      model       <- IO.ref(initModel)
      currentSubs <- AtomicCell[IO].of(List.empty[(String, IO[Unit])])
      msgQueue    <- Queue.unbounded[IO, GlobalMsg]
      viewState   <- IO.ref(()) // This is a hangover from the web version
    } yield new TyrianSDLRuntime(dispatcher, model, currentSubs, msgQueue, viewState)
