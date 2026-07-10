package indigo.scenegraph

import indigo.core.datatypes.LayerKey
import indigo.core.render.Magnification
import indigo.scenegraph.materials.BlendMaterial
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.RGBA

class SceneUpdateFragmentTests extends munit.FunSuite {

  test("Able to add a batch of layers from a constructor") {

    val actual =
      SceneUpdateFragment(Batch(LayerKey("key A") -> Layer.empty, LayerKey("key B") -> Layer.empty))

    val expected =
      SceneUpdateFragment.empty.addLayers(
        Batch(LayerEntry(LayerKey("key A"), Layer.empty), LayerEntry(LayerKey("key B"), Layer.empty))
      )

    assertEquals(actual, expected)
  }

  test("Able to add an optional layer from a constructor (Some)") {

    val actual =
      SceneUpdateFragment(Option(LayerKey("key A") -> Layer.empty))

    val expected =
      SceneUpdateFragment.empty.addLayers(Batch(LayerEntry(LayerKey("key A"), Layer.empty)))

    assertEquals(actual, expected)

  }

  test("Able to add an optional layer from a constructor (None)") {

    val actual =
      SceneUpdateFragment(Option.empty[LayerEntry])

    val expected =
      SceneUpdateFragment.empty

    assertEquals(actual, expected)

  }

  test("Adding a layer with an existing key merges magnification down (none, none)") {

    val scene =
      SceneUpdateFragment.empty.addLayer(LayerEntry(LayerKey("key A"), Layer.empty))

    val actual =
      scene.addLayer(LayerEntry(LayerKey("key A"), Layer.empty))

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.head.config.magnification.map(_.toInt), None)

  }

  test("Adding a layer with an existing key merges magnification down (some, some)") {

    val scene =
      SceneUpdateFragment.empty.addLayer(LayerEntry(LayerKey("key A"), Layer.empty).withMagnification(Magnification.x2))

    val actual =
      scene.addLayer(LayerEntry(LayerKey("key A"), Layer.empty).withMagnification(Magnification.x1))

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.head.config.magnification.map(_.toInt), Some(2))

  }

  test("Adding a layer with an existing key merges magnification down (none, some)") {

    val scene =
      SceneUpdateFragment.empty.addLayer(LayerEntry(LayerKey("key A"), Layer.empty))

    val actual =
      scene.addLayer(LayerEntry(LayerKey("key A"), Layer.empty).withMagnification(Magnification.x1))

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.head.config.magnification.map(_.toInt), Some(1))

  }

  test("Adding a layer with an existing key merges magnification down (some, none)") {

    val scene =
      SceneUpdateFragment.empty.addLayer(LayerEntry(LayerKey("key A"), Layer.empty).withMagnification(Magnification.x2))

    val actual =
      scene.addLayer(LayerEntry(LayerKey("key A"), Layer.empty))

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.head.config.magnification.map(_.toInt), Some(2))

  }

  test("Replace layers using withLayers") {

    val scene =
      SceneUpdateFragment.empty.addLayers(LayerKey("key A") -> Layer.empty)

    val actual =
      scene.withLayers(LayerKey("key B") -> Layer.empty)

    assert(actual.layers.length == 1)

    actual.layers.head match
      case LayerEntry(key, _, _) =>
        assertEquals(key, LayerKey("key B"))

  }

  test("SUF append preseves layer keys") {

    val sceneA: SceneUpdateFragment =
      SceneUpdateFragment.empty.addLayer(LayerEntry(LayerKey("key A"), Layer.empty).withMagnification(Magnification.x2))

    val sceneB: SceneUpdateFragment =
      SceneUpdateFragment.empty.addLayer(LayerEntry(LayerKey("key A"), Layer.empty).withMagnification(Magnification.x3))

    val actual: SceneUpdateFragment =
      sceneA |+| sceneB

    assert(actual.layers.length == 1)
    assertEquals(actual.layers.head.config.magnification.map(_.toInt), Some(2))

  }

  test("Can add a blend material with no Blending instance in place") {
    val scene =
      SceneUpdateFragment.empty.withBlendMaterial(BlendMaterial.Lighting(RGBA.Red))

    scene.blendMaterial match
      case Some(BlendMaterial.Lighting(color)) =>
        assertEquals(color, RGBA.Red)

      case _ =>
        fail("match failed")
  }

  test("Can modify blending with no Blending instance in place") {
    val scene =
      SceneUpdateFragment.empty.modifyBlendMaterial(_ => BlendMaterial.Lighting(RGBA.Red))

    scene.blendMaterial match
      case Some(BlendMaterial.Lighting(color)) =>
        assertEquals(color, RGBA.Red)

      case _ =>
        fail("match failed")
  }

  test("Modify layers") {
    val scene =
      SceneUpdateFragment.empty
        .addLayer(LayerEntry(LayerKey("key A"), Layer.empty).withMagnification(Magnification.x1))
        .addLayer(LayerEntry(LayerKey("key B"), Layer.empty).withMagnification(Magnification.x1))

    val actual =
      scene.modifyLayers { case le @ LayerEntry(key, _, _) =>
        le.withKey(LayerKey(key.show + "?")).withMagnification(Magnification.x2)
      }

    assert(actual.layers.length == 2)

    assertEquals(
      actual.layers.map(_.key).toList,
      List(LayerKey("key A?"), LayerKey("key B?"))
    )
    assertEquals(
      actual.layers.map(_.config.magnification.map(_.toInt)).toList,
      List(Some(2), Some(2))
    )
  }

  test("Setting the magnification for all layers") {
    val scene =
      SceneUpdateFragment.empty
        .addLayer(LayerEntry(LayerKey("key A"), Layer.empty).withMagnification(Magnification.x1))
        .addLayer(LayerEntry(LayerKey("key B"), Layer.empty).withMagnification(Magnification.x1))

    val actual =
      scene.withMagnification(Magnification.x2)

    assertEquals(
      actual.layers.map(_.config.magnification.map(_.toInt)).toList,
      List(Some(2), Some(2))
    )
  }

  test("Merging SUF's with layer stacks") {

    val sceneA =
      SceneUpdateFragment.empty
        .addLayers(
          LayerKey("a") -> Layer.Content.empty,
          LayerKey("b") -> Layer.Stack(
            Layer.Content.empty,
            Layer.Stack(
              Layer.Content.empty
            ),
            Layer.Content.empty
          ),
          LayerKey("c") -> Layer.Content.empty
        )

    val sceneB =
      SceneUpdateFragment.empty
        .addLayers(
          LayerKey("a") -> Layer.Content.empty,
          LayerKey("b") -> Layer.Content.empty,
          LayerKey("c") -> Layer.Stack(
            Layer.Content.empty
          )
        )

    val actual =
      sceneA |+| sceneB

    val expected =
      SceneUpdateFragment.empty
        .addLayers(
          LayerKey("a") -> Layer.Content.empty,
          LayerKey("b") -> Layer.Stack(
            Layer.Content.empty,
            Layer.Stack(
              Layer.Content.empty
            ),
            Layer.Content.empty,
            Layer.Content.empty
          ),
          LayerKey("c") -> Layer.Stack(
            Layer.Content.empty,
            Layer.Content.empty
          )
        )

    assertEquals(actual, expected)

    val actualBatch =
      actual.layers.flatMap(_.toBatch)

    val expectedBatch =
      Batch(
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty,
        Layer.Content.empty
      )

    assertEquals(actualBatch.length, expectedBatch.length)
    assertEquals(actualBatch, expectedBatch)

  }

}
