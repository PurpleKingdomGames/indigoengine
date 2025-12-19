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
import tyrian.next.GlobalMsg

final case class TyrianIndigoNextSubSystem[Model](
    bridge: TyrianIndigoNextBridge[Model],
    toGlobalMsg: GlobalEvent => Option[GlobalMsg]
) extends SubSystem[Model]:

  val id: SubSystemId =
    SubSystemId("[IndigoNextBridgeSubSystem] " + hashCode.toString)

  type EventType      = GlobalEvent
  type SubSystemModel = Unit
  type ReferenceData  = Unit

  private val eventQueue: mutable.Queue[GlobalEvent] =
    new mutable.Queue[GlobalEvent]()

  bridge.eventTarget.addEventListener[TyrianIndigoNextBridge.BridgeToIndigo](
    TyrianIndigoNextBridge.BridgeToIndigo.EventName,
    {
      case TyrianIndigoNextBridge.BridgeToIndigo(value) =>
        eventQueue.enqueue(value)

      case _ =>
        ()
    }
  )

  def eventFilter: GlobalEvent => Option[EventType] =
    case e         => Some(e)
    // case _         => None

  def reference(model: Model): ReferenceData =
    ()

  def initialModel: Outcome[Unit] =
    Outcome(())

  @nowarn("msg=unused")
  def update(context: SubSystemContext[ReferenceData], model: Unit): GlobalEvent => Outcome[Unit] =
    case FrameTick if eventQueue.size > 0 =>
      // println("Do we enqueue? " + eventQueue.size)
      Outcome(model, Batch.fromSeq(eventQueue.dequeueAll(_ => true)))

    case e =>
      toGlobalMsg(e) match
        case None =>
          Outcome(model)

        case Some(msg) =>
          bridge.eventTarget.dispatchEvent(TyrianIndigoNextBridge.BridgeToTyrian(msg))
          Outcome(model)

  def present(context: SubSystemContext[ReferenceData], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
