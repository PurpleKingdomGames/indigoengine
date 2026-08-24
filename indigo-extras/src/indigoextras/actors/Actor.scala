package indigoextras.actors

import indigo.core.Outcome
import indigo.core.events.GlobalEvent

/** An Actor is a standalone entity that can update and present itself, and communicates with the world by reading
  * shared immutable data (`ReferenceData`) from the game model, and by receiving and emitting events.
  *
  * The Actor typeclass allows you to define an Actor for any type, so long as you can meaningfully provide an update
  * and a present function for it. The present function can produce any view data that can be used by the pool to
  * represent the actor, anything from a batch of scene nodes to clone data to a custom type you interpret later.
  */
trait Actor[ReferenceData, ActorType, ActorView]:

  /** Update this actor.
    */
  def update(context: ActorContext[ReferenceData, ActorType], actor: ActorType): GlobalEvent => Outcome[ActorType]

  /** Draw the actor.
    */
  def present(context: ActorContext[ReferenceData, ActorType], actor: ActorType): Outcome[ActorView]
