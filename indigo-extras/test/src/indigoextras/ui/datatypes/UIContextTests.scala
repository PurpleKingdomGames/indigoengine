package indigoextras.ui.datatypes

import indigo.*

class UIContextTests extends munit.FunSuite {

  test("UIContext should correctly calculate the pointer coords") {
    val uiContext =
      UIContext(Context.initial, Magnification.x1)
        .withPointerCoords(Coords(10, 20))

    assertEquals(uiContext.pointerCoords, Coords(10, 20))
    assertEquals(uiContext.withSnapGrid(Size(2)).pointerCoords, Coords(5, 10))
    assertEquals(uiContext.withSnapGrid(Size(10)).pointerCoords, Coords(1, 2))
  }

  test("pointerCoords divides screen-space pointer by magnification") {
    // Pointer positions are screen-space pixels, so higher magnification means
    // the pointer maps to a smaller grid coord - it must divide, not multiply.
    val atX1 =
      UIContext(Context.initial, Magnification.x1)
        .withSnapGrid(Size(2))
        .withPointerCoords(Coords(20, 40))
    val atX2 =
      UIContext(Context.initial, Magnification.x2)
        .withSnapGrid(Size(2))
        .withPointerCoords(Coords(20, 40))

    assertEquals(atX1.pointerCoords, Coords(10, 20))
    assertEquals(atX2.pointerCoords, Coords(5, 10))
  }

  test("pointerCoords is the inverse of toScreenSpace") {
    // A component drawn at grid coord `c` occupies screen pixels
    // `c.toScreenSpace(snapGrid, magnification)`. A pointer over that top-left
    // pixel must hit-test back to `c`.
    val snapGrid      = Size(8)
    val magnification = Magnification.x2
    val coord         = Coords(3, 5)

    val screenPos = coord.toScreenSpace(snapGrid, magnification)

    val ctx =
      UIContext(Context.initial, magnification)
        .withSnapGrid(snapGrid)
        .withPointerCoords(Coords(screenPos))

    assertEquals(ctx.pointerCoords, coord)
  }

}
