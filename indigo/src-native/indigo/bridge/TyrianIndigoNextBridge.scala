package indigo.bridge

import cats.effect.IO
import tyrian.classic.Cmd
import tyrian.classic.Sub

import scala.annotation.nowarn

final class TyrianIndigoNextBridge[Model]:

  // val eventTarget: EventTarget = new EventTarget()

  @nowarn("msg=unused")
  def send(value: BridgeMsg.Send): Cmd[IO, Nothing] =
    Cmd.SideEffect {
      // eventTarget.dispatchEvent(
      //   new BridgeToIndigo(
      //     BridgeEvent.Receive(value.data)
      //   )
      // )
      ()
    }

  def subscribe: Sub[IO, BridgeMsg.Receive] =
    // val acquire = (callback: Either[Throwable, BridgeToTyrian] => Unit) =>
    //   IO {
    //     val listener = Functions.fun((a: BridgeToTyrian) => callback(Right(a)))
    //     eventTarget.addEventListener(BridgeToTyrian.EventName, listener)
    //     listener
    //   }

    // val release = (listener: js.Function1[BridgeToTyrian, Unit]) =>
    //   IO(eventTarget.removeEventListener(BridgeToTyrian.EventName, listener))

    // Sub.Observe(
    //   BridgeToTyrian.EventName + this.hashCode,
    //   acquire,
    //   release,
    //   bridgeMsg => Some(bridgeMsg.value)
    // )
    Sub.None

  def subSystem: TyrianIndigoNextSubSystem[Model] =
    TyrianIndigoNextSubSystem(this)

/** Wraps our event in a Dom Event so that it can be send over the bridge from Tyrian to Indigo. */
final class BridgeToIndigo(val value: BridgeEvent.Receive) // extends DomEvent(BridgeToIndigo.EventName)
object BridgeToIndigo:
  val EventName: String = "[SendToIndigo]"

/** Wraps our event in a Dom Event so that it can be send over the bridge from Indigo to Tyrian. */
final class BridgeToTyrian(val value: BridgeMsg.Receive) // extends DomEvent(BridgeToTyrian.EventName)
object BridgeToTyrian:
  val EventName: String = "[SendToTyrian]"
