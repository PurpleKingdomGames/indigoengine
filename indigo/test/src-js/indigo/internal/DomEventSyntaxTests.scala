package indigo.internal

import indigo.core.events.MouseButton.*
import indigoengine.shared.collections.Batch

class DomEventSyntaxTests extends munit.FunSuite {

  test("calculate which buttons have been pressed based on a dom.MouseEvent.buttons Int") {

    assertEquals(DomEventSyntax.buttonsFromInt(0), Batch())
    assertEquals(DomEventSyntax.buttonsFromInt(1), Batch(LeftMouseButton))
    assertEquals(DomEventSyntax.buttonsFromInt(2), Batch(MiddleMouseButton))
    assertEquals(DomEventSyntax.buttonsFromInt(3), Batch(LeftMouseButton, MiddleMouseButton))
    assertEquals(DomEventSyntax.buttonsFromInt(4), Batch(RightMouseButton))
    assertEquals(DomEventSyntax.buttonsFromInt(5), Batch(LeftMouseButton, RightMouseButton))
    assertEquals(DomEventSyntax.buttonsFromInt(6), Batch(MiddleMouseButton, RightMouseButton))
    assertEquals(DomEventSyntax.buttonsFromInt(7), Batch(LeftMouseButton, MiddleMouseButton, RightMouseButton))
    assertEquals(DomEventSyntax.buttonsFromInt(8), Batch(BrowserBackButton))
    assertEquals(DomEventSyntax.buttonsFromInt(9), Batch(LeftMouseButton, BrowserBackButton))
    assertEquals(DomEventSyntax.buttonsFromInt(10), Batch(MiddleMouseButton, BrowserBackButton))
    assertEquals(DomEventSyntax.buttonsFromInt(11), Batch(LeftMouseButton, MiddleMouseButton, BrowserBackButton))
    assertEquals(DomEventSyntax.buttonsFromInt(12), Batch(RightMouseButton, BrowserBackButton))
    assertEquals(DomEventSyntax.buttonsFromInt(13), Batch(LeftMouseButton, RightMouseButton, BrowserBackButton))
    assertEquals(DomEventSyntax.buttonsFromInt(14), Batch(MiddleMouseButton, RightMouseButton, BrowserBackButton))
    assertEquals(
      DomEventSyntax.buttonsFromInt(15),
      Batch(LeftMouseButton, MiddleMouseButton, RightMouseButton, BrowserBackButton)
    )
    assertEquals(DomEventSyntax.buttonsFromInt(16), Batch(BrowserForwardButton))
    assertEquals(DomEventSyntax.buttonsFromInt(17), Batch(LeftMouseButton, BrowserForwardButton))
    assertEquals(DomEventSyntax.buttonsFromInt(18), Batch(MiddleMouseButton, BrowserForwardButton))
    assertEquals(DomEventSyntax.buttonsFromInt(19), Batch(LeftMouseButton, MiddleMouseButton, BrowserForwardButton))
    assertEquals(DomEventSyntax.buttonsFromInt(20), Batch(RightMouseButton, BrowserForwardButton))
    assertEquals(DomEventSyntax.buttonsFromInt(21), Batch(LeftMouseButton, RightMouseButton, BrowserForwardButton))
    assertEquals(DomEventSyntax.buttonsFromInt(22), Batch(MiddleMouseButton, RightMouseButton, BrowserForwardButton))
    assertEquals(
      DomEventSyntax.buttonsFromInt(23),
      Batch(LeftMouseButton, MiddleMouseButton, RightMouseButton, BrowserForwardButton)
    )
    assertEquals(DomEventSyntax.buttonsFromInt(24), Batch(BrowserBackButton, BrowserForwardButton))
    assertEquals(DomEventSyntax.buttonsFromInt(25), Batch(LeftMouseButton, BrowserBackButton, BrowserForwardButton))
    assertEquals(DomEventSyntax.buttonsFromInt(26), Batch(MiddleMouseButton, BrowserBackButton, BrowserForwardButton))
    assertEquals(
      DomEventSyntax.buttonsFromInt(27),
      Batch(LeftMouseButton, MiddleMouseButton, BrowserBackButton, BrowserForwardButton)
    )
    assertEquals(DomEventSyntax.buttonsFromInt(28), Batch(RightMouseButton, BrowserBackButton, BrowserForwardButton))
    assertEquals(
      DomEventSyntax.buttonsFromInt(29),
      Batch(LeftMouseButton, RightMouseButton, BrowserBackButton, BrowserForwardButton)
    )
    assertEquals(
      DomEventSyntax.buttonsFromInt(30),
      Batch(MiddleMouseButton, RightMouseButton, BrowserBackButton, BrowserForwardButton)
    )
    assertEquals(
      DomEventSyntax.buttonsFromInt(31),
      Batch(LeftMouseButton, MiddleMouseButton, RightMouseButton, BrowserBackButton, BrowserForwardButton)
    )
  }

}
