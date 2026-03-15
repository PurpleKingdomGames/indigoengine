package indigo.render.pipeline.sceneprocessing.utils

import indigo.core.datatypes.Fill
import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.scenegraph.Camera
import indigo.scenegraph.Layer
import indigo.scenegraph.LayerEntry
import indigo.scenegraph.LayerKey
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
      LayerEntry(Layer.empty),
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

  lazy val compacted: Batch[(Option[LayerKey], Batch[Layer.Content])] =
    Batch(
      (None, Batch(Layer.Content.empty)),
      (Some(LayerKey("b")), Batch(Layer.Content.empty)),
      (
        Some(LayerKey("c")),
        Batch(
          Layer.Content(shape, shape)
        )
      ),
      (
        Some(LayerKey("d")),
        Batch(
          Layer.Content(shape).withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape, shape).withCamera(Camera.Fixed(Point(10))),
          Layer.Content(shape)
        )
      )
    )
