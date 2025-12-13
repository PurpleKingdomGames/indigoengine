package tyrian.bridge

import cats.effect.kernel.Async
import org.scalajs.dom.Event as DomEvent
import org.scalajs.dom.EventTarget
import tyrian.Cmd
import tyrian.Sub
import tyrian.bridge.TyrianSubSystem
import util.Functions

import scala.annotation.nowarn
import scala.scalajs.js

final class TyrianIndigoBridge[F[_]: Async, Event, Model]:

  val eventTarget: EventTarget = new EventTarget()

  def publish(value: Event): Cmd[F, Nothing] =
    publishToBridge(None, value)
  def publish(indigoGame: IndigoGameId, value: Event): Cmd[F, Nothing] =
    publishToBridge(Option(indigoGame), value)

  def subscribe[B](extract: Event => B)(using CanEqual[B, B]): Sub[F, B] =
    subscribeToBridge(None, e => Option(extract(e)))
  def subscribe[B](indigoGame: IndigoGameId)(extract: Event => B)(using CanEqual[B, B]): Sub[F, B] =
    subscribeToBridge(Option(indigoGame), e => Option(extract(e)))
  def subscribeOpt[B](extract: Event => Option[B])(using CanEqual[B, B]): Sub[F, B] =
    subscribeToBridge(None, extract)
  def subscribeOpt[B](indigoGame: IndigoGameId)(extract: Event => Option[B])(using CanEqual[B, B]): Sub[F, B] =
    subscribeToBridge(Option(indigoGame), extract)

  def subSystem: TyrianSubSystem[F, Event, Model] =
    TyrianSubSystem(this)
  def subSystem(indigoGame: IndigoGameId): TyrianSubSystem[F, Event, Model] =
    TyrianSubSystem(Option(indigoGame), this)

  @nowarn("msg=unused")
  private def publishToBridge(indigoGameId: Option[IndigoGameId], value: Event): Cmd[F, Nothing] =
    Cmd.SideEffect {
      eventTarget.dispatchEvent(TyrianIndigoBridge.BridgeToIndigo(indigoGameId, value))
      ()
    }

  private def subscribeToBridge[B](indigoGameId: Option[IndigoGameId], extract: Event => Option[B])(using
      CanEqual[B, B]
  ): Sub[F, B] =
    import TyrianIndigoBridge.BridgeToTyrian

    val eventExtract: BridgeToTyrian[Event] => Option[B] = e =>
      indigoGameId match
        case None                       => extract(e.value)
        case id if e.indigoGameId == id => extract(e.value)
        case _                          => None

    val acquire = (callback: Either[Throwable, BridgeToTyrian[Event]] => Unit) =>
      Async[F].delay {
        val listener = Functions.fun((a: BridgeToTyrian[Event]) => callback(Right(a)))
        eventTarget.addEventListener(BridgeToTyrian.EventName, listener)
        listener
      }

    val release = (listener: js.Function1[BridgeToTyrian[Event], Unit]) =>
      Async[F].delay(eventTarget.removeEventListener(BridgeToTyrian.EventName, listener))

    Sub.Observe(
      BridgeToTyrian.EventName + this.hashCode,
      acquire,
      release,
      eventExtract
    )

object TyrianIndigoBridge:

  def apply[F[_]: Async, Event, Model](): TyrianIndigoBridge[F, Event, Model] =
    new TyrianIndigoBridge[F, Event, Model]()

  final class BridgeToIndigo[Event](val indigoGameId: Option[IndigoGameId], val value: Event)
      extends DomEvent(BridgeToIndigo.EventName)
  object BridgeToIndigo:
    val EventName: String = "SendToIndigo"

    def unapply[Event](e: BridgeToIndigo[Event]): Option[(Option[IndigoGameId], Event)] =
      Some((e.indigoGameId, e.value))

  final class BridgeToTyrian[Event](val indigoGameId: Option[IndigoGameId], val value: Event)
      extends DomEvent(BridgeToTyrian.EventName)
  object BridgeToTyrian:
    val EventName: String = "SendToTyrian"

    def unapply[Event](e: BridgeToTyrian[Event]): Option[(Option[IndigoGameId], Event)] =
      Some((e.indigoGameId, e.value))
