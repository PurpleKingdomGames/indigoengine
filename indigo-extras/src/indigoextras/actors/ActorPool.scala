package indigoextras.actors

import indigo.SubSystemContext
import indigo.core.Outcome
import indigo.core.events.FrameTick
import indigo.core.events.GlobalEvent
import indigo.scenegraph.SceneUpdateFragment
import indigo.scenes.SceneContext
import indigo.shared.Context
import indigoengine.shared.collections.Batch

/** Holds, manages and presents a pool of Actors.
  */
final case class ActorPool[ReferenceData, ActorType, ActorView](
    actors: Batch[ActorInstance[ReferenceData, ActorType, ActorView]],
    toSceneUpdateFragment: Batch[ActorView] => SceneUpdateFragment
)(using Ordering[ActorType])(using actor: Actor[ReferenceData, ActorType, ActorView]):

  private val orderingInstance: Ordering[ActorInstance[ReferenceData, ActorType, ActorView]] =
    Ordering.by(a => a.instance)

  /** Update the actor pool, passing in the model and a standard context. */
  def update(
      context: Context,
      model: ReferenceData
  ): GlobalEvent => Outcome[ActorPool[ReferenceData, ActorType, ActorView]] =
    val nextPool: GlobalEvent => Outcome[Batch[ActorInstance[ReferenceData, ActorType, ActorView]]] =
      case FrameTick =>
        actors
          .map { ai =>
            val ctx = ActorContext(find, model, context)

            ai.actor.update(ctx, ai.instance)(FrameTick).map { updated =>
              ai.copy(instance = updated)
            }
          }
          .sequence
          .map { actorInstances =>
            actorInstances.sorted[ActorInstance[ReferenceData, ActorType, ActorView]](using orderingInstance)
          }

      case e =>
        actors.map { ai =>
          val ctx = ActorContext(find, model, context)

          ai.actor.update(ctx, ai.instance)(e).map { updated =>
            ai.copy(instance = updated)
          }
        }.sequence

    (e: GlobalEvent) => nextPool(e).map(n => this.copy(actors = n))

  /** Update the actor pool, passing in the model and a scene context. */
  def update(
      context: SceneContext,
      model: ReferenceData
  ): GlobalEvent => Outcome[ActorPool[ReferenceData, ActorType, ActorView]] =
    update(context.toContext, model)

  /** Update the actor pool, passing in the model and a subsystem context. */
  def update(
      context: SubSystemContext[?],
      model: ReferenceData
  ): GlobalEvent => Outcome[ActorPool[ReferenceData, ActorType, ActorView]] =
    update(context.toContext, model)

  def present(
      context: Context,
      model: ReferenceData
  ): Outcome[SceneUpdateFragment] =
    actors
      .map { ai =>
        val ctx = ActorContext(find, model, context)

        ai.actor.present(ctx, ai.instance)
      }
      .sequence
      .map(toSceneUpdateFragment)

  def present(
      context: SceneContext,
      model: ReferenceData
  ): Outcome[SceneUpdateFragment] =
    present(context.toContext, model)

  def present(
      context: SubSystemContext[?],
      model: ReferenceData
  ): Outcome[SceneUpdateFragment] =
    present(context.toContext, model)

  /** Finds the first actor in the pool that matches the predicate test. */
  def find(p: ActorType => Boolean): Option[ActorType] =
    actors.find(ai => p(ai.instance)).map(_.instance)

  /** Finds all actors in the pool that match the predicate test. */
  def filter(p: ActorType => Boolean): Batch[ActorType] =
    actors.filter(ai => p(ai.instance)).map(_.instance)

  /** Finds all actors in the pool that do not match the predicate test. */
  def filterNot(p: ActorType => Boolean): Batch[ActorType] =
    actors.filterNot(ai => p(ai.instance)).map(_.instance)

  /** Spawns a batch of new actor in the pool. */
  def spawn(
      newActors: Batch[ActorType]
  ): ActorPool[ReferenceData, ActorType, ActorView] =
    this.copy(
      actors = actors ++ newActors.map(a => ActorInstance(a, actor))
    )

  /** Spawns new actors in the pool. */
  def spawn(newActors: ActorType*): ActorPool[ReferenceData, ActorType, ActorView] =
    spawn(Batch.fromSeq(newActors))

  /** Kills any actors in the pool that match the predicate test. */
  def kill(p: ActorType => Boolean): ActorPool[ReferenceData, ActorType, ActorView] =
    this.copy(
      actors = actors.filterNot(ai => p(ai.instance))
    )

  def toBatch: Batch[ActorType] =
    actors.map(_.instance)

object ActorPool:

  def apply[ReferenceData, ActorType, ActorView](toSceneUpdateFragment: Batch[ActorView] => SceneUpdateFragment)(using
      Ordering[ActorType],
      Actor[ReferenceData, ActorType, ActorView]
  ): ActorPool[ReferenceData, ActorType, ActorView] =
    ActorPool(Batch.empty, toSceneUpdateFragment)
