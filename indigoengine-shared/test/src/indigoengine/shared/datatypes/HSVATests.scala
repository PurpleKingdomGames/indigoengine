package indigoengine.shared.datatypes

class HSVATests extends munit.FunSuite {

  test("HSVA color constants have correct values") {
    assertEquals(HSVA.Red, HSVA(0, 1.0, 1.0, 1.0))
    assertEquals(HSVA.Green, HSVA(120, 1.0, 1.0, 1.0))
    assertEquals(HSVA.Blue, HSVA(240, 1.0, 1.0, 1.0))
    assertEquals(HSVA.Yellow, HSVA(60, 1.0, 1.0, 1.0))
    assertEquals(HSVA.Cyan, HSVA(180, 1.0, 1.0, 1.0))
    assertEquals(HSVA.Magenta, HSVA(300, 1.0, 1.0, 1.0))
    assertEquals(HSVA.White, HSVA(0, 0.0, 1.0, 1.0))
    assertEquals(HSVA.Black, HSVA(0, 0.0, 0.0, 1.0))
    assertEquals(HSVA.Zero, HSVA(0, 0.0, 0.0, 0.0))
  }

  test("HSVA apply with 3 args creates opaque color") {
    assertEquals(HSVA(100, 0.5, 0.5), HSVA(100, 0.5, 0.5, 1.0))
  }

  test("withHue creates a new HSVA with updated hue") {
    val hsva = HSVA(100, 0.5, 0.5, 0.8)
    assertEquals(hsva.withHue(200), HSVA(200, 0.5, 0.5, 0.8))
  }

  test("withSaturation creates a new HSVA with updated saturation") {
    val hsva = HSVA(100, 0.5, 0.5, 0.8)
    assertEquals(hsva.withSaturation(0.8), HSVA(100, 0.8, 0.5, 0.8))
  }

  test("withValue creates a new HSVA with updated value") {
    val hsva = HSVA(100, 0.5, 0.5, 0.8)
    assertEquals(hsva.withValue(0.8), HSVA(100, 0.5, 0.8, 0.8))
  }

  test("withAlpha creates a new HSVA with updated alpha") {
    val hsva = HSVA(100, 0.5, 0.5, 0.8)
    assertEquals(hsva.withAlpha(0.3), HSVA(100, 0.5, 0.5, 0.3))
  }

  test("makeOpaque sets alpha to 1.0") {
    val hsva = HSVA(100, 0.5, 0.5, 0.3)
    assertEquals(hsva.makeOpaque, HSVA(100, 0.5, 0.5, 1.0))
  }

  test("makeTransparent sets alpha to 0.0") {
    val hsva = HSVA(100, 0.5, 0.5, 0.8)
    assertEquals(hsva.makeTransparent, HSVA(100, 0.5, 0.5, 0.0))
  }

  test("rotateHue rotates hue correctly and preserves alpha") {
    val hsva    = HSVA(0, 1.0, 1.0, 0.5)
    val rotated = hsva.rotateHue(Degrees(120))
    assertEquals(rotated.h, 120.0)
    assertEquals(rotated.a, 0.5)
  }

  test("rotateHue wraps around at 360") {
    val rotated = HSVA.Blue.rotateHue(Degrees(180))
    assertEquals(rotated.h, 60.0) // 240 + 180 = 420 -> 60
  }

  test("rotateHue handles negative rotation") {
    val rotated = HSVA.Red.rotateHue(Degrees(-90))
    assertEqualsDouble(rotated.h, 270.0, 0.01)
  }

  test("brighten increases value and preserves alpha") {
    val hsva       = HSVA(0, 1.0, 0.5, 0.5)
    val brightened = hsva.brighten(0.25)
    assertEqualsDouble(brightened.v, 0.75, 0.01)
    assertEquals(brightened.a, 0.5)
  }

  test("brighten clamps to 1.0") {
    val brightened = HSVA.White.brighten(0.5)
    assertEqualsDouble(brightened.v, 1.0, 0.01)
  }

  test("darken decreases value and preserves alpha") {
    val hsva     = HSVA(0, 1.0, 1.0, 0.5)
    val darkened = hsva.darken(0.25)
    assertEqualsDouble(darkened.v, 0.75, 0.01)
    assertEquals(darkened.a, 0.5)
  }

  test("darken clamps to 0.0") {
    val darkened = HSVA.Black.darken(0.5)
    assertEqualsDouble(darkened.v, 0.0, 0.01)
  }

  test("saturate increases saturation and preserves alpha") {
    val hsva      = HSVA(0, 0.5, 1.0, 0.5)
    val saturated = hsva.saturate(0.3)
    assertEqualsDouble(saturated.s, 0.8, 0.01)
    assertEquals(saturated.a, 0.5)
  }

  test("saturate clamps to 1.0") {
    val saturated = HSVA.Red.saturate(0.5)
    assertEqualsDouble(saturated.s, 1.0, 0.01)
  }

  test("desaturate decreases saturation and preserves alpha") {
    val hsva        = HSVA(0, 1.0, 1.0, 0.5)
    val desaturated = hsva.desaturate(0.5)
    assertEqualsDouble(desaturated.s, 0.5, 0.01)
    assertEquals(desaturated.a, 0.5)
  }

  test("desaturate clamps to 0.0") {
    val desaturated = HSVA.White.desaturate(0.5)
    assertEqualsDouble(desaturated.s, 0.0, 0.01)
  }

  test("toHSV drops alpha") {
    val hsva = HSVA(100, 0.5, 0.6, 0.3)
    val hsv  = hsva.toHSV
    assertEquals(hsv.h, 100.0)
    assertEquals(hsv.s, 0.5)
    assertEquals(hsv.v, 0.6)
  }

  test("toRGB converts correctly (drops alpha)") {
    val rgb = HSVA.Red.toRGB
    assertEqualsDouble(rgb.r, 1.0, 0.01)
    assertEqualsDouble(rgb.g, 0.0, 0.01)
    assertEqualsDouble(rgb.b, 0.0, 0.01)
  }

  test("toRGBA converts correctly and preserves alpha") {
    val hsva = HSVA(0, 1.0, 1.0, 0.5)
    val rgba = hsva.toRGBA
    assertEqualsDouble(rgba.r, 1.0, 0.01)
    assertEqualsDouble(rgba.g, 0.0, 0.01)
    assertEqualsDouble(rgba.b, 0.0, 0.01)
    assertEqualsDouble(rgba.a, 0.5, 0.01)
  }

  test("toRGBA for all primary colors") {
    val red = HSVA.Red.toRGBA
    assertEqualsDouble(red.r, 1.0, 0.01)
    assertEqualsDouble(red.g, 0.0, 0.01)
    assertEqualsDouble(red.b, 0.0, 0.01)

    val green = HSVA.Green.toRGBA
    assertEqualsDouble(green.r, 0.0, 0.01)
    assertEqualsDouble(green.g, 1.0, 0.01)
    assertEqualsDouble(green.b, 0.0, 0.01)

    val blue = HSVA.Blue.toRGBA
    assertEqualsDouble(blue.r, 0.0, 0.01)
    assertEqualsDouble(blue.g, 0.0, 0.01)
    assertEqualsDouble(blue.b, 1.0, 0.01)

    val yellow = HSVA.Yellow.toRGBA
    assertEqualsDouble(yellow.r, 1.0, 0.01)
    assertEqualsDouble(yellow.g, 1.0, 0.01)
    assertEqualsDouble(yellow.b, 0.0, 0.01)

    val cyan = HSVA.Cyan.toRGBA
    assertEqualsDouble(cyan.r, 0.0, 0.01)
    assertEqualsDouble(cyan.g, 1.0, 0.01)
    assertEqualsDouble(cyan.b, 1.0, 0.01)

    val magenta = HSVA.Magenta.toRGBA
    assertEqualsDouble(magenta.r, 1.0, 0.01)
    assertEqualsDouble(magenta.g, 0.0, 0.01)
    assertEqualsDouble(magenta.b, 1.0, 0.01)
  }

  test("fromRGBA preserves alpha") {
    val rgba = RGBA(1.0, 0.0, 0.0, 0.5)
    val hsva = HSVA.fromRGBA(rgba)
    assertEqualsDouble(hsva.h, 0.0, 1.0)
    assertEqualsDouble(hsva.s, 1.0, 0.01)
    assertEqualsDouble(hsva.v, 1.0, 0.01)
    assertEqualsDouble(hsva.a, 0.5, 0.01)
  }

  test("fromRGB creates opaque HSVA") {
    val rgb  = RGB.Red
    val hsva = HSVA.fromRGB(rgb)
    assertEqualsDouble(hsva.h, 0.0, 1.0)
    assertEqualsDouble(hsva.s, 1.0, 0.01)
    assertEqualsDouble(hsva.v, 1.0, 0.01)
    assertEquals(hsva.a, 1.0)
  }

  test("RGBA -> HSVA -> RGBA round-trip") {
    val colors = List(RGBA.Red, RGBA.Green, RGBA.Blue, RGBA.Cyan, RGBA.Magenta, RGBA.Yellow, RGBA.Orange)
    colors.foreach { original =>
      val roundTrip = HSVA.fromRGBA(original).toRGBA
      assertEqualsDouble(roundTrip.r, original.r, 0.01)
      assertEqualsDouble(roundTrip.g, original.g, 0.01)
      assertEqualsDouble(roundTrip.b, original.b, 0.01)
      assertEqualsDouble(roundTrip.a, original.a, 0.01)
    }
  }

  test("RGBA -> HSVA -> RGBA round-trip with alpha") {
    val original  = RGBA(1.0, 0.5, 0.0, 0.7)
    val roundTrip = HSVA.fromRGBA(original).toRGBA
    assertEqualsDouble(roundTrip.r, original.r, 0.01)
    assertEqualsDouble(roundTrip.g, original.g, 0.01)
    assertEqualsDouble(roundTrip.b, original.b, 0.01)
    assertEqualsDouble(roundTrip.a, original.a, 0.01)
  }

}
