package indigo.scenegraph

import indigo.core.datatypes.Size

class CornersTests extends munit.FunSuite {

  test("corner clamping") {
    val corners = Corners(5, 10, 15, 20)

    assertEquals(corners.clampTo(Size(50)), corners)
    assertEquals(corners.clampTo(Size(32)), Corners(5, 10, 15, 16))
    assertEquals(corners.clampTo(Size(20)), Corners(5, 10, 10, 10))
    assertEquals(corners.clampTo(Size(1)), Corners(0, 0, 0, 0))
  }

}
