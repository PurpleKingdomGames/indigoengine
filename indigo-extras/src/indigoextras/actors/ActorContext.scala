package indigoextras.actors

import indigo.*
import indigo.scenes.SceneContext

final case class ActorContext[ReferenceData, ActorType](
    find: (ActorType => Boolean) => Option[ActorType],
    reference: ReferenceData,
    frame: Context.Frame,
    services: Context.Services
):
  def toContext: Context =
    new Context(frame, services)

object ActorContext:

  def apply[ReferenceData, ActorType](
      find: (ActorType => Boolean) => Option[ActorType],
      reference: ReferenceData,
      ctx: Context
  ): ActorContext[ReferenceData, ActorType] =
    ActorContext(find, reference, ctx.frame, ctx.services)

  def apply[ReferenceData, ActorType](
      find: (ActorType => Boolean) => Option[ActorType],
      reference: ReferenceData,
      ctx: SceneContext
  ): ActorContext[ReferenceData, ActorType] =
    ActorContext(find, reference, ctx.frame, ctx.services)

  def apply[ReferenceData, ActorType](
      find: (ActorType => Boolean) => Option[ActorType],
      reference: ReferenceData,
      ctx: SubSystemContext[?]
  ): ActorContext[ReferenceData, ActorType] =
    ActorContext(find, reference, ctx.frame, ctx.services)
