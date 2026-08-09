package indigo.render.webgl2

import indigo.core.render.Magnification

class FrameBufferFunctionsTests extends munit.FunSuite {

  test("decideBufferSizes - one entry per magnification, sized to the layer's game space") {

    val actual: List[(Int, Int)] =
      FrameBufferFunctions.decideBufferSizes(800, 600)

    assertEquals(actual.length, 16)

    assertEquals(
      actual.take(5),
      List(
        (800, 600),
        (400, 300),
        (267, 200),
        (200, 150),
        (160, 120)
      )
    )

    assertEquals(actual.last, (50, 38))
  }

  test("decideBufferSizes - rounds up so nothing is ever cropped") {
    val actual = FrameBufferFunctions.decideBufferSizes(801, 601)

    assertEquals(actual(1), (401, 301))
    assertEquals(actual(2), (267, 201))
  }

  test("decideBufferSizes - never produces a zero sized buffer") {
    val actual = FrameBufferFunctions.decideBufferSizes(10, 4)

    assert(actual.forall { case (w, h) => w >= 1 && h >= 1 })
    assertEquals(actual.last, (1, 1))
  }

  test("clampMagnification") {
    assertEquals(FrameBufferFunctions.clampMagnification(None, 15), 0)
    assertEquals(FrameBufferFunctions.clampMagnification(Some(0), 15), 0)
    assertEquals(FrameBufferFunctions.clampMagnification(Some(1), 15), 0)
    assertEquals(FrameBufferFunctions.clampMagnification(Some(2), 15), 1)
    assertEquals(FrameBufferFunctions.clampMagnification(Some(3), 15), 2)
    assertEquals(FrameBufferFunctions.clampMagnification(Some(16), 15), 15)
    assertEquals(FrameBufferFunctions.clampMagnification(Some(17), 15), 15)
  }

  test("effectiveMagnification") {
    assertEquals(FrameBufferFunctions.effectiveMagnification(None, 15), 1)
    assertEquals(FrameBufferFunctions.effectiveMagnification(Some(0), 15), 1)
    assertEquals(FrameBufferFunctions.effectiveMagnification(Some(1), 15), 1)
    assertEquals(FrameBufferFunctions.effectiveMagnification(Some(3), 15), 3)
    assertEquals(FrameBufferFunctions.effectiveMagnification(Some(16), 15), 16)
    assertEquals(FrameBufferFunctions.effectiveMagnification(Some(17), 15), 16)
  }

  test("mergeQuadSize - one texel covers exactly `magnification` screen pixels") {
    // The quad has to cover the whole screen, and can only overhang it by less than one magnified pixel, or the
    // scale is not the whole number the rest of the engine assumes it is.
    List((800, 600), (1920, 1080), (1366, 768)).foreach { case (screenWidth, screenHeight) =>
      val sizes = FrameBufferFunctions.decideBufferSizes(screenWidth, screenHeight)

      (1 to Magnification.Max.toInt).foreach { m =>
        val (bufferWidth, bufferHeight) = sizes(m - 1)
        val (quadWidth, quadHeight)     = FrameBufferFunctions.mergeQuadSize(bufferWidth, bufferHeight, m)

        assert(
          quadWidth >= screenWidth && quadWidth < screenWidth + m,
          s"width was $quadWidth at ${screenWidth}x$screenHeight magnification $m"
        )
        assert(
          quadHeight >= screenHeight && quadHeight < screenHeight + m,
          s"height was $quadHeight at ${screenWidth}x$screenHeight magnification $m"
        )
      }
    }
  }

}
