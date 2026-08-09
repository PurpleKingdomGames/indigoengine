package indigo.render.pipeline.sceneprocessing.utils

import indigo.core.datatypes.Fill
import indigo.core.datatypes.LayerKey
import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.core.render.Magnification
import indigo.scenegraph.AmbientLight
import indigo.scenegraph.Blending
import indigo.scenegraph.Camera
import indigo.scenegraph.Layer
import indigo.scenegraph.LayerEntry
import indigo.scenegraph.SceneUpdateFragment
import indigo.scenegraph.Shape
import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.RGBA

class CompactLayersTests extends munit.FunSuite:

  test("Layer compacting") {
    val layers: Batch[LayerEntry] =
      uncompacted

    val actual =
      CompactLayers.compactLayers(layers)

    val expected =
      compacted

    assertEquals(clue(actual), clue(expected))
  }

  test("magnification is applied once per LayerEntry, not compounded by nesting") {
    val entry: LayerEntry =
      LayerEntry(
        LayerKey("deeply-nested"),
        Layer.Stack(
          Layer.Content(shape),
          Layer.Stack(
            Layer.Content(shape),
            Layer.Stack(
              Layer.Content(shape)
            )
          )
        )
      ).withMagnification(Magnification.x2)

    val actual =
      CompactLayers.compactLayers(Batch(entry))

    // One group, carrying the entry's single x2 magnification - not x2 * x2 * x2.
    assertEquals(actual.length, 1)

    val (contents, magnification) = actual.head
    assertEquals(magnification, Magnification.x2)
    // All nested content layers share the same camera/blend/lights, so they compact to one.
    assertEquals(contents, Batch(Layer.Content(shape, shape, shape)))
  }

  test("hidden entries are removed, allowing their neighbours to merge") {
    val entries: Batch[LayerEntry] =
      Batch(
        LayerEntry(LayerKey("a"), Layer.Content(shape)),
        LayerEntry(LayerKey("b"), Layer.Content(shape)).hide,
        LayerEntry(LayerKey("c"), Layer.Content(shape))
      )

    val actual =
      CompactLayers.compactLayers(entries)

    val expected =
      Batch(
        (Batch(Layer.Content(shape, shape)), Magnification.x1)
      )

    assertEquals(clue(actual), clue(expected))
  }

  test("no-op empty layers are removed, allowing groups to merge across them") {
    val entries: Batch[LayerEntry] =
      Batch(
        LayerEntry(LayerKey("a"), Layer.Content(shape)).withMagnification(Magnification.x2),
        LayerEntry(LayerKey("b"), Layer.Content.empty),
        LayerEntry(LayerKey("c"), Layer.Content(shape)).withMagnification(Magnification.x2)
      )

    val actual =
      CompactLayers.compactLayers(entries)

    val expected =
      Batch(
        (Batch(Layer.Content(shape, shape)), Magnification.x2)
      )

    assertEquals(clue(actual), clue(expected))
  }

  test("empty layers with blending are kept, they can still affect the output") {
    val lighting: Layer.Content =
      Layer.Content.empty.withBlending(Blending.Lighting(RGBA.Black))

    val entries: Batch[LayerEntry] =
      Batch(
        LayerEntry(LayerKey("game"), Layer.Content(shape)),
        LayerEntry(LayerKey("lighting"), lighting)
      )

    val actual =
      CompactLayers.compactLayers(entries)

    val expected =
      Batch(
        (Batch(Layer.Content(shape), lighting), Magnification.x1)
      )

    assertEquals(clue(actual), clue(expected))
  }

  test("compactByMagnification") {

    // This would be the results of 'unstacking'
    val unstacked =
      Batch(
        (Batch(Layer.Content.empty), Magnification.x2),
        (Batch(Layer.Content.empty), Magnification.x1),
        (Batch(Layer.Content.empty), Magnification.x1),
        (Batch(Layer.Content.empty), Magnification.x1),
        (Batch(Layer.Content(shape, shape)), Magnification.x1),
        (
          Batch(
            Layer.Content(shape).withCamera(Camera.Fixed(Point.zero)),
            Layer.Content(shape, shape).withCamera(Camera.Fixed(Point(10))),
            Layer.Content(shape)
          ),
          Magnification.x3
        ),
        (Batch(Layer.Content.empty), Magnification.x3),
        (Batch(Layer.Content.empty), Magnification.x2),
        (Batch(Layer.Content.empty), Magnification.x2),
        (Batch(Layer.Content(shape)), Magnification.x2)
      )

    // This process does not remove layers, just flattens the groups down when they share the same magnification.
    val actual =
      CompactLayers.compactByMagnification(unstacked)

    val expected =
      Batch(
        (Batch(Layer.Content.empty), Magnification.x2),
        (
          Batch(
            Layer.Content.empty,
            Layer.Content.empty,
            Layer.Content.empty,
            Layer.Content(shape, shape)
          ),
          Magnification.x1
        ),
        (
          Batch(
            Layer.Content(shape).withCamera(Camera.Fixed(Point.zero)),
            Layer.Content(shape, shape).withCamera(Camera.Fixed(Point(10))),
            Layer.Content(shape),
            Layer.Content.empty
          ),
          Magnification.x3
        ),
        (Batch(Layer.Content.empty, Layer.Content.empty, Layer.Content(shape)), Magnification.x2)
      )

    assertEquals(actual, expected)
  }

  test("the empty-placeholder-with-camera pattern survives through to the render pipeline") {
    val key    = LayerKey("game")
    val camera = Camera.LookAt(Point(100, 200))

    val scene =
      SceneUpdateFragment.empty.addLayers(key -> Layer.Content.empty.withCamera(camera)) |+|
        SceneUpdateFragment.empty
          .addLayers(key -> Layer.Content(shape))
          .withMagnification(Magnification.x2)

    val actualLayers =
      scene.layers

    val expectedLayers =
      Batch(
        LayerEntry(
          key,
          Layer.Content(shape).withCamera(camera)
        ).withMagnification(Magnification.x2)
      )

    assertEquals(clue(actualLayers), clue(expectedLayers))

    val actual =
      CompactLayers.compactLayers(scene.layers)

    val expected =
      Batch((Batch(Layer.Content(shape).withCamera(camera)), Magnification.x2))

    assertEquals(clue(actual), clue(expected))
  }

  test("the empty-placeholder-with-camera pattern survives through to the render pipeline (inverse)") {
    val key    = LayerKey("game")
    val camera = Camera.LookAt(Point(100, 200))

    val scene =
      SceneUpdateFragment.empty
        .addLayers(key -> Layer.Content(shape))
        .withMagnification(Magnification.x2) |+|
        SceneUpdateFragment.empty.addLayers(key -> Layer.Content.empty.withCamera(camera))

    val actualLayers =
      scene.layers

    val expectedLayers =
      Batch(
        LayerEntry(
          key,
          Layer.Content(shape).withCamera(camera)
        ).withMagnification(Magnification.x2)
      )

    assertEquals(clue(actualLayers), clue(expectedLayers))

    val actual =
      CompactLayers.compactLayers(scene.layers)

    val expected =
      Batch((Batch(Layer.Content(shape).withCamera(camera)), Magnification.x2))

    assertEquals(clue(actual), clue(expected))
  }

  test("layers that cannot affect the output can be dropped") {
    assert(CompactLayers.canBeDropped(Layer.Content.empty))
    assert(CompactLayers.canBeDropped(Layer.Content.empty.withCamera(Camera.Fixed(Point.zero))))
    assert(CompactLayers.canBeDropped(Layer.Content.empty.withLights(AmbientLight(RGBA.White))))
    assert(!CompactLayers.canBeDropped(Layer.Content(shape)))
    assert(!CompactLayers.canBeDropped(Layer.Content.empty.withBlending(Blending.Lighting(RGBA.Black))))
  }

  test("a camera-only layer does not survive as an empty composite") {
    val entries: Batch[LayerEntry] =
      Batch(
        LayerEntry(
          LayerKey("a"),
          Layer.Stack(
            Layer.Content.empty.withCamera(Camera.Fixed(Point.zero)),
            Layer.Content(shape).withCamera(Camera.Fixed(Point(10)))
          )
        )
      )

    val actual =
      CompactLayers.compactLayers(entries)

    val expected =
      Batch((Batch(Layer.Content(shape).withCamera(Camera.Fixed(Point(10)))), Magnification.x1))

    assertEquals(clue(actual), clue(expected))
  }

  lazy val shape: Shape.Box =
    Shape.Box(Rectangle(0, 0, 100, 100), Fill.Color(RGBA.Red))

  lazy val uncompacted: Batch[LayerEntry] =
    Batch(
      LayerEntry(LayerKey("z"), Layer.Content(shape)).withMagnification(Magnification.x3),
      LayerEntry(LayerKey("a"), Layer.Content.empty).withMagnification(Magnification.x2),
      LayerEntry(LayerKey("b"), Layer.Content.empty),
      LayerEntry(
        LayerKey("c"),
        Layer.Stack(
          Layer.Content(shape),
          Layer.Content(shape)
        )
      ),
      LayerEntry(
        LayerKey("d"),
        Layer.Stack(
          Layer.Content.empty.withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape).withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape).withCamera(Camera.Fixed(Point(10))),
          Layer.Stack(
            Layer.Content(shape).withCamera(Camera.Fixed(Point(10))),
            Layer.Content(shape)
          )
        )
      )
    )

  lazy val compacted: Batch[(Batch[Layer.Content], Magnification)] =
    Batch(
      (Batch(Layer.Content(shape)), Magnification.x3),
      (
        Batch(
          Layer.Content(shape, shape),
          Layer.Content(shape).withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape, shape).withCamera(Camera.Fixed(Point(10))),
          Layer.Content(shape)
        ),
        Magnification.x1
      )
    )
