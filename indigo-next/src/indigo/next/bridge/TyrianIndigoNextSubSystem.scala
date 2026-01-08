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

final case class TyrianIndigoNextSubSystem[Model, Data](
    bridge: TyrianIndigoNextBridge[Model, Data]
) extends SubSystem[Model]:

  val id: SubSystemId =
    SubSystemId("[IndigoNextBridgeSubSystem] " + hashCode.toString)

  type EventType      = GlobalEvent
  type SubSystemModel = Unit
  type ReferenceData  = Unit

  private val eventQueue: mutable.Queue[GlobalEvent] =
    new mutable.Queue[GlobalEvent]()

  bridge.eventTarget.addEventListener[bridge.BridgeToIndigo](
    bridge.BridgeToIndigo.EventName,
    { case e: bridge.BridgeToIndigo =>
      eventQueue.enqueue(e.value)
    }
  )

  def eventFilter: GlobalEvent => Option[EventType] =
    case e: BridgeEvent.Send[_] => Some(e)
    case _                      => None

  def reference(model: Model): ReferenceData =
    ()

  def initialModel: Outcome[Unit] =
    Outcome(())

  @nowarn("msg=unused")
  def update(context: SubSystemContext[ReferenceData], model: Unit): GlobalEvent => Outcome[Unit] =
    case FrameTick if eventQueue.size > 0 =>
      Outcome(model, Batch.fromSeq(eventQueue.dequeueAll(_ => true)))

    case e: BridgeEvent.Send[Data] @unchecked =>
      bridge.eventTarget.dispatchEvent(bridge.BridgeToTyrian(BridgeMsg.Receive(e.data)))
      Outcome(model)

    case e =>
      Outcome(model)

  def present(context: SubSystemContext[ReferenceData], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
