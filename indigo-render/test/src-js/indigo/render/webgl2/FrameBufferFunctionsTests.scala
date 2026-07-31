package indigo.render.webgl2

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

}
