package example

import indigo.*
import indigo.physics.*

trait PhysicsScene extends Scene[Unit, Model]:

  def world(dice: Dice): World[MyTag]

  type SceneModel = World[MyTag]

  val name: SceneName

  val modelLens: Lens[Model, World[MyTag]]

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[Model]] =
    Set()

  def updateModel(
      context: SceneContext[Unit],
      world: World[MyTag]
  ): GlobalEvent => Outcome[World[MyTag]] =
    case FrameTick =>
      world.update(context.frame.time.delta)

    case _ =>
      Outcome(world)

  def present(
      context: SceneContext[Unit],
      world: World[MyTag]
  ): Outcome[SceneUpdateFragment] =
    View.present(world)
