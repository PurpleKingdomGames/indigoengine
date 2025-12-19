package indigo.next.bridge

import cats.effect.IO
import org.scalajs.dom.Event as DomEvent
import org.scalajs.dom.EventTarget
import tyrian.Cmd
import tyrian.Sub
import util.Functions

import scala.annotation.nowarn
import scala.scalajs.js
import indigo.core.events.GlobalEvent
import tyrian.next.GlobalMsg

final class TyrianIndigoNextBridge[Model]:

  val eventTarget: EventTarget = new EventTarget()

  def publish(value: GlobalEvent): Cmd[IO, Nothing] =
    publishToBridge(value)

  def subscribe: Sub[IO, GlobalMsg] =
    subscribeToBridge

  def subSystem(toGlobalMsg: GlobalEvent => Option[GlobalMsg]): TyrianIndigoNextSubSystem[Model] =
    TyrianIndigoNextSubSystem(this, toGlobalMsg)

  @nowarn("msg=unused")
  private def publishToBridge(value: GlobalEvent): Cmd[IO, Nothing] =
    Cmd.SideEffect {
      eventTarget.dispatchEvent(TyrianIndigoNextBridge.BridgeToIndigo(value))
      ()
    }

  private def subscribeToBridge: Sub[IO, GlobalMsg] =
    import TyrianIndigoNextBridge.BridgeToTyrian

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

object TyrianIndigoNextBridge:

  final class BridgeToIndigo(val value: GlobalEvent)
      extends DomEvent(BridgeToIndigo.EventName)
  object BridgeToIndigo:
    val EventName: String = "[SendToIndigo]"

    def unapply(e: BridgeToIndigo): Option[GlobalEvent] =
      Some(e.value)

  final class BridgeToTyrian(val value: GlobalMsg)
      extends DomEvent(BridgeToTyrian.EventName)
  object BridgeToTyrian:
    val EventName: String = "[SendToTyrian]"

    def unapply(e: BridgeToTyrian): Option[GlobalMsg] =
      Some(e.value)
