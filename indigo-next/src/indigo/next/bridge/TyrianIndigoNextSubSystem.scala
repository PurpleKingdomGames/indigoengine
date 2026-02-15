package indigo.next.bridge

import indigo.core.Outcome
import indigo.core.events.FrameTick
import indigo.core.events.GlobalEvent
import indigo.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemId
import indigoengine.shared.collections.Batch

import scala.annotation.nowarn
import scala.collection.mutable

final case class TyrianIndigoNextSubSystem[Model](
    bridge: TyrianIndigoNextBridge[Model]
) extends SubSystem[Model]:

  val id: SubSystemId =
    SubSystemId("[IndigoNextBridgeSubSystem] " + hashCode.toString)

  type EventType      = GlobalEvent
  type SubSystemModel = Unit
  type ReferenceData  = Unit

  private val eventQueue: mutable.Queue[GlobalEvent] =
    new mutable.Queue[GlobalEvent]()

  bridge.eventTarget.addEventListener[BridgeToIndigo](
    BridgeToIndigo.EventName,
    { case e: BridgeToIndigo =>
      eventQueue.enqueue(e.value)
    }
  )

  def eventFilter: GlobalEvent => Option[EventType] =
    case e: BridgeEvent.Send => Some(e)
    case FrameTick           => Some(FrameTick)
    case _                   => None

  def reference(model: Model): ReferenceData =
    ()

  def initialModel: Outcome[Unit] =
    Outcome(())

  @nowarn("msg=unused")
  def update(context: SubSystemContext[ReferenceData], model: Unit): GlobalEvent => Outcome[Unit] =
    case FrameTick if eventQueue.size > 0 =>
      val evts = eventQueue.dequeueAll(_ => true)
      Outcome(model, Batch.fromSeq(evts))

    case e: BridgeEvent.Send =>
      bridge.eventTarget.dispatchEvent(new BridgeToTyrian(BridgeMsg.Receive(e.data)))
      Outcome(model)

    case e =>
      Outcome(model)

  def present(context: SubSystemContext[ReferenceData], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
