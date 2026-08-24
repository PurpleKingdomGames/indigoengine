package indigoextras.actors

import indigo.*

class ActorPoolTests extends munit.FunSuite {

  test("ActorPool should be created with an empty list of actors") {
    val actorPool =
      ActorPool[Unit, String, SceneNode](_ => SceneUpdateFragment.empty)

    assertEquals(actorPool.toBatch.length, 0)
  }

  test("spawn") {
    val actorPool =
      ActorPool[Unit, String, SceneNode](_ => SceneUpdateFragment.empty)
    val newActors = Batch("Actor1", "Actor2", "Actor3")

    val updatedPool = actorPool.spawn(newActors)

    assertEquals(updatedPool.toBatch, newActors)
  }

  test("find") {
    val actorPool =
      ActorPool[Unit, String, SceneNode](_ => SceneUpdateFragment.empty)
        .spawn("Actor1", "Actor2", "Actor3")

    val foundActor = actorPool.find(_ == "Actor2")

    assertEquals(foundActor, Some("Actor2"))
  }

  test("filter") {
    val actorPool =
      ActorPool[Unit, String, SceneNode](_ => SceneUpdateFragment.empty).spawn("Actor1", "Actor2", "Actor3")

    val filteredActors = actorPool.filter(_ == "Actor2")

    assertEquals(filteredActors, Batch("Actor2"))
  }

  test("filterNot") {
    val actorPool =
      ActorPool[Unit, String, SceneNode](_ => SceneUpdateFragment.empty).spawn("Actor1", "Actor2", "Actor3")

    val filteredActors = actorPool.filterNot(_ == "Actor2")

    assertEquals(filteredActors, Batch("Actor1", "Actor3"))
  }

  test("kill") {
    val actorPool =
      ActorPool[Unit, String, SceneNode](_ => SceneUpdateFragment.empty).spawn("Actor1", "Actor2", "Actor3")

    val updatedPool = actorPool.kill(_ == "Actor2")

    assertEquals(updatedPool.toBatch, Batch("Actor1", "Actor3"))
  }

  test("update") {
    val actorPool =
      ActorPool[Unit, String, SceneNode](_ => SceneUpdateFragment.empty).spawn("Actor1", "Actor2", "Actor3")

    val ctx: Context =
      Context.initial

    val updatedPool = actorPool.update(ctx, ())(FrameTick)

    assertEquals(updatedPool.unsafeGet.toBatch, Batch("Actor1!", "Actor2!", "Actor3!"))
  }

  test("present") {
    val actorPool =
      ActorPool[Unit, String, SceneNode](nodes =>
        SceneUpdateFragment(
          LayerKey("test") -> Layer.Content(nodes)
        )
      ).spawn("Actor1", "Actor2", "Actor3")

    val ctx: Context =
      Context.initial

    val presented = actorPool.present(ctx, ())

    presented.unsafeGet.layers.head.layer match
      case Layer.Stack(_) =>
        fail("Uh oh.")

      case Layer.Content(nodes, _, _, _, _) =>
        assertEquals(nodes.length, 3)
  }

  given Actor[Unit, String, SceneNode] with
    def update(context: ActorContext[Unit, String], actor: String): GlobalEvent => Outcome[String] =
      _ => Outcome(actor + "!")

    def present(context: ActorContext[Unit, String], actor: String): Outcome[SceneNode] =
      Outcome(Text(actor, FontKey("test"), Material.Bitmap(AssetName("test"))))
}
