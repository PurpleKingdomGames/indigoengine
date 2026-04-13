package tyrian.classic

import cats.effect.kernel.Async
import org.scalajs.dom.window
import tyrian.classic.syntax.fromEvent

object Connectivity:

  /** A command to check the network status.
    */
  def checkStatus[F[_], Msg](toMsg: Connectivity.Status => Msg)(using F: Async[F]): Cmd[F, Msg] =
    val task =
      F.delay(
        window.navigator.onLine
      )

    val f: Boolean => Msg =
      ((on: Boolean) => if on then Connectivity.Status.Online else Connectivity.Status.Offline)
        .andThen(toMsg)

    Cmd.Run(task, f)

  /** Subscribes to online / offline network events. Please note that these are very browser implementation dependant,
    * and therefore may not behave as you'd expect.
    */
  def subscribe[F[_], Msg](onOnline: Msg, onOffline: Msg)(using F: Async[F]): Sub[F, Msg] =
    val onlineSub =
      Sub.fromEvent("online", window)(_ => Option(onOnline))
    val offlineSub =
      Sub.fromEvent("offline", window)(_ => Option(onOffline))

    Sub.combineAll(List(onlineSub, offlineSub))

  enum Status derives CanEqual:
    case Online, Offline

    def isOnline: Boolean =
      this match
        case Online  => true
        case Offline => false

    def isOffline: Boolean =
      !isOnline
