package indigoextras.actors

final case class ActorInstance[ReferenceData, ActorType, ActorView](
    instance: ActorType,
    actor: Actor[ReferenceData, ActorType, ActorView]
)
