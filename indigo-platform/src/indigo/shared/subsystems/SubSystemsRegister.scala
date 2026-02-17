package indigo.shared.subsystems

import indigo.core.Outcome
import indigo.core.events.GlobalEvent
import indigo.core.utils.IndigoLogger
import indigo.scenegraph.SceneUpdateFragment
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.mutable

final class SubSystemsRegister[Model] {

  private[subsystems] val stateMap: mutable.KVP[Object] = mutable.KVP.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var registeredSubSystems: Batch[RegisteredSubSystem[Model]] = Batch()

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def register(newSubSystems: Batch[SubSystem[Model]]): Batch[GlobalEvent] =
    newSubSystems.map(initialiseSubSystem).sequence match {
      case oe @ Outcome.Error(e, _) =>
        IndigoLogger.error("Error during subsystem setup - Halting.")
        IndigoLogger.error("Crash report:")
        IndigoLogger.error(oe.reportCrash)
        throw e

      case Outcome.Result(toBeRegistered, events) =>
        registeredSubSystems = registeredSubSystems ++ toBeRegistered
        events
    }

  private def initialiseSubSystem(subSystem: SubSystem[Model]): Outcome[RegisteredSubSystem[Model]] = {
    val key = subSystem.id.toString
    val res = RegisteredSubSystem(key, subSystem)

    subSystem.initialModel.map { model =>
      stateMap.update(key, model.asInstanceOf[Object])

      res
    }
  }

  def update(
      context: SubSystemContext[Unit],
      gameModel: Model,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[SubSystemsRegister[Model]] = {
    def outcomeEvents: Outcome[Batch[GlobalEvent]] =
      Outcome
        .sequence(
          Batch(
            registeredSubSystems
              .map { rss =>
                val key       = rss.id
                val subSystem = rss.subSystem

                val filteredEvents: Batch[subSystem.EventType] =
                  globalEvents
                    .map(subSystem.eventFilter)
                    .collect { case Some(e) => e }

                val model: subSystem.SubSystemModel = stateMap.getUnsafe(key).asInstanceOf[subSystem.SubSystemModel]

                filteredEvents.foldLeft(Outcome(model)) { (acc, e) =>
                  acc.flatMap { m =>
                    subSystem.update(
                      context.copy(reference = subSystem.reference(gameModel)),
                      m
                    )(e)
                  }
                } match {
                  case Outcome.Error(e, _) =>
                    Outcome.raiseError(e)

                  case Outcome.Result(state, globalEvents) =>
                    stateMap.update(key, state.asInstanceOf[Object])
                    Outcome(globalEvents)
                }
              }
          ).flatten
        )
        .map(_.flatten)

    outcomeEvents.flatMap(l => Outcome(this, l))
  }

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def present(context: SubSystemContext[Unit], gameModel: Model): Outcome[SceneUpdateFragment] =
    registeredSubSystems
      .map { rss =>
        rss.subSystem.present(
          context.copy(reference = rss.subSystem.reference(gameModel)),
          stateMap.getUnsafe(rss.id).asInstanceOf[rss.subSystem.SubSystemModel]
        )
      }
      .foldLeft(Outcome(SceneUpdateFragment.empty))((acc, next) => Outcome.merge(acc, next)(_ |+| _))

  def size: Int =
    registeredSubSystems.length

}

final case class RegisteredSubSystem[Model](id: String, subSystem: SubSystem[Model]) derives CanEqual
