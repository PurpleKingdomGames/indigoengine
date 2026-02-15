package indigoextras.actors

import indigo.GlobalEvent
import indigo.Outcome
import indigo.scenegraph.SceneNode
import indigoengine.shared.collections.Batch

/** An Actor is a standalone entity that can update and present itself, and communicates with the world by reading
  * shared immutable data (`ReferenceData`) from the game model, and by receiving and emitting events.
  *
  * The Actor typeclass allows you to define an Actor for any type, so long as you can meaningfully provide an update
  * and a present function for it.
  */
trait Actor[ReferenceData, ActorType]:

  /** Update this actor.
    */
  def update(context: ActorContext[ReferenceData, ActorType], actor: ActorType): GlobalEvent => Outcome[ActorType]

  /** Draw the actor.
    */
  def present(context: ActorContext[ReferenceData, ActorType], actor: ActorType): Outcome[Batch[SceneNode]]
