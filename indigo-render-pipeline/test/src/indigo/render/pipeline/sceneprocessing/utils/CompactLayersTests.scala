package indigo.render.pipeline.sceneprocessing.utils

import indigo.core.datatypes.Fill
import indigo.core.datatypes.LayerKey
import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.core.render.Magnification
import indigo.scenegraph.Camera
import indigo.scenegraph.Layer
import indigo.scenegraph.LayerEntry
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

  lazy val shape: Shape.Box =
    Shape.Box(Rectangle(0, 0, 100, 100), Fill.Color(RGBA.Red))

  lazy val uncompacted: Batch[LayerEntry] =
    Batch(
      LayerEntry(LayerKey("a"), Layer.empty).withMagnification(Magnification.x2),
      LayerEntry(LayerKey("b"), Layer.empty),
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
          Layer.empty.withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape).withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape).withCamera(Camera.Fixed(Point(10))),
          Layer.Stack(
            Layer(shape).withCamera(Camera.Fixed(Point(10))),
            Layer(shape)
          )
        )
      )
    )

  lazy val compacted: Batch[(Batch[Layer.Content], Magnification)] =
    Batch(
      (Batch(Layer.Content.empty), Magnification.x2),
      (Batch(Layer.Content.empty), Magnification.default),
      (
        Batch(
          Layer.Content(shape, shape)
        ),
        Magnification.default
      ),
      (
        Batch(
          Layer.Content(shape).withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape, shape).withCamera(Camera.Fixed(Point(10))),
          Layer.Content(shape)
        ),
        Magnification.default
      )
    )

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
