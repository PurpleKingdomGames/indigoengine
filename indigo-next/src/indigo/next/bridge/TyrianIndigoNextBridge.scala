package indigo.next.bridge

import cats.effect.IO
import org.scalajs.dom.Event as DomEvent
import org.scalajs.dom.EventTarget
import tyrian.Cmd
import tyrian.Sub
import tyrian.next.GlobalMsg
import util.Functions

import scala.annotation.nowarn
import scala.scalajs.js

final class TyrianIndigoNextBridge[Model, Data]:

  val eventTarget: EventTarget = new EventTarget()

  @nowarn("msg=unused")
  def send(value: BridgeMsg.Send[Data]): Cmd[IO, Nothing] =
    Cmd.SideEffect {
      eventTarget.dispatchEvent(
        BridgeToIndigo(
          BridgeEvent.Receive[Data](value.data)
        )
      )
      ()
    }

  def subscribe: Sub[IO, GlobalMsg] =
    // import TyrianIndigoNextBridge.BridgeToTyrian

    val acquire = (callback: Either[Throwable, BridgeToTyrian] => Unit) =>
      IO {
        val listener = Functions.fun((a: BridgeToTyrian) => callback(Right(a)))
        eventTarget.addEventListener(BridgeToTyrian.EventName, listener)
        listener
      }

    val release = (listener: js.Function1[BridgeToTyrian, Unit]) =>
      IO(eventTarget.removeEventListener(BridgeToTyrian.EventName, listener))

    Sub.Observe(
      BridgeToTyrian.EventName + this.hashCode,
      acquire,
      release,
      bridgeMsg => Some(bridgeMsg.value)
    )

  def subSystem: TyrianIndigoNextSubSystem[Model, Data] =
    TyrianIndigoNextSubSystem(this)

  /** Wraps our event in a Dom Event so that it can be send over the bridge from Tyrian to Indigo. */
  final class BridgeToIndigo(val value: BridgeEvent.Receive[Data]) extends DomEvent(BridgeToIndigo.EventName)
  object BridgeToIndigo:
    val EventName: String = "[SendToIndigo]"

    // def unapply[Data](e: BridgeToIndigo): Option[BridgeEvent.[Data]] =
    //   Some(e.value)

  /** Wraps our event in a Dom Event so that it can be send over the bridge from Indigo to Tyrian. */
  final class BridgeToTyrian(val value: BridgeMsg.Receive[Data]) extends DomEvent(BridgeToTyrian.EventName)
  object BridgeToTyrian:
    val EventName: String = "[SendToTyrian]"

    // def unapply[Data](e: BridgeToTyrian): Option[BridgeMsg[Data]] =
    //   Some(e.value)
