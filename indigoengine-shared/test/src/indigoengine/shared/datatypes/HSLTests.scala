package indigoengine.shared.datatypes

class HSLTests extends munit.FunSuite {

  test("HSL color constants have correct values") {
    assertEquals(HSL.Red, HSL(0, 1.0, 0.5))
    assertEquals(HSL.Green, HSL(120, 1.0, 0.5))
    assertEquals(HSL.Blue, HSL(240, 1.0, 0.5))
    assertEquals(HSL.Yellow, HSL(60, 1.0, 0.5))
    assertEquals(HSL.Cyan, HSL(180, 1.0, 0.5))
    assertEquals(HSL.Magenta, HSL(300, 1.0, 0.5))
    assertEquals(HSL.White, HSL(0, 0.0, 1.0))
    assertEquals(HSL.Black, HSL(0, 0.0, 0.0))
  }

  test("withHue creates a new HSL with updated hue") {
    val hsl = HSL(100, 0.5, 0.5)
    assertEquals(hsl.withHue(200), HSL(200, 0.5, 0.5))
  }

  test("withSaturation creates a new HSL with updated saturation") {
    val hsl = HSL(100, 0.5, 0.5)
    assertEquals(hsl.withSaturation(0.8), HSL(100, 0.8, 0.5))
  }

  test("withLightness creates a new HSL with updated lightness") {
    val hsl = HSL(100, 0.5, 0.5)
    assertEquals(hsl.withLightness(0.8), HSL(100, 0.5, 0.8))
  }

  test("rotateHue rotates hue correctly") {
    assertEquals(HSL.Red.rotateHue(Degrees(120)).h, 120.0)
    assertEquals(HSL.Red.rotateHue(Degrees(240)).h, 240.0)
    assertEquals(HSL.Red.rotateHue(Degrees(360)).h, 0.0)
  }

  test("rotateHue wraps around at 360") {
    assertEquals(HSL.Blue.rotateHue(Degrees(180)).h, 60.0) // 240 + 180 = 420 -> 60
  }

  test("rotateHue handles negative rotation") {
    val rotated = HSL.Red.rotateHue(Degrees(-90))
    assertEqualsDouble(rotated.h, 270.0, 0.01)
  }

  test("lighten increases lightness") {
    val lightened = HSL.Red.lighten(0.25)
    assertEqualsDouble(lightened.l, 0.75, 0.01)
  }

  test("lighten clamps to 1.0") {
    val lightened = HSL.White.lighten(0.5)
    assertEqualsDouble(lightened.l, 1.0, 0.01)
  }

  test("darken decreases lightness") {
    val darkened = HSL.Red.darken(0.25)
    assertEqualsDouble(darkened.l, 0.25, 0.01)
  }

  test("darken clamps to 0.0") {
    val darkened = HSL.Black.darken(0.5)
    assertEqualsDouble(darkened.l, 0.0, 0.01)
  }

  test("saturate increases saturation") {
    val muted     = HSL(0, 0.5, 0.5)
    val saturated = muted.saturate(0.3)
    assertEqualsDouble(saturated.s, 0.8, 0.01)
  }

  test("saturate clamps to 1.0") {
    val saturated = HSL.Red.saturate(0.5)
    assertEqualsDouble(saturated.s, 1.0, 0.01)
  }

  test("desaturate decreases saturation") {
    val desaturated = HSL.Red.desaturate(0.5)
    assertEqualsDouble(desaturated.s, 0.5, 0.01)
  }

  test("desaturate clamps to 0.0") {
    val desaturated = HSL.White.desaturate(0.5)
    assertEqualsDouble(desaturated.s, 0.0, 0.01)
  }

  test("toRGB converts to RGB correctly") {
    val red = HSL.Red.toRGB
    assertEqualsDouble(red.r, 1.0, 0.01)
    assertEqualsDouble(red.g, 0.0, 0.01)
    assertEqualsDouble(red.b, 0.0, 0.01)

    val green = HSL.Green.toRGB
    assertEqualsDouble(green.r, 0.0, 0.01)
    assertEqualsDouble(green.g, 1.0, 0.01)
    assertEqualsDouble(green.b, 0.0, 0.01)

    val blue = HSL.Blue.toRGB
    assertEqualsDouble(blue.r, 0.0, 0.01)
    assertEqualsDouble(blue.g, 0.0, 0.01)
    assertEqualsDouble(blue.b, 1.0, 0.01)
  }

  test("toRGBA converts to RGBA with full opacity") {
    val rgba = HSL.Red.toRGBA
    assertEqualsDouble(rgba.r, 1.0, 0.01)
    assertEqualsDouble(rgba.g, 0.0, 0.01)
    assertEqualsDouble(rgba.b, 0.0, 0.01)
    assertEqualsDouble(rgba.a, 1.0, 0.01)
  }

  test("toHSLA converts to HSLA with full opacity") {
    val hsla = HSL.Red.toHSLA
    assertEquals(hsla.h, 0.0)
    assertEquals(hsla.s, 1.0)
    assertEquals(hsla.l, 0.5)
    assertEquals(hsla.a, 1.0)
  }

  test("toCSSValue formats correctly") {
    assertEquals(HSL.Red.toCSSValue, "hsl(0, 100%, 50%)")
    assertEquals(HSL.Green.toCSSValue, "hsl(120, 100%, 50%)")
  }

  test("fromRGB creates HSL correctly") {
    val hsl = HSL.fromRGB(RGB.Red)
    assertEqualsDouble(hsl.h, 0.0, 1.0)
    assertEqualsDouble(hsl.s, 1.0, 0.01)
    assertEqualsDouble(hsl.l, 0.5, 0.01)
  }

  test("fromRGBA creates HSL correctly (ignores alpha)") {
    val hsl = HSL.fromRGBA(RGBA(1.0, 0.0, 0.0, 0.5))
    assertEqualsDouble(hsl.h, 0.0, 1.0)
    assertEqualsDouble(hsl.s, 1.0, 0.01)
    assertEqualsDouble(hsl.l, 0.5, 0.01)
  }

  test("RGB -> HSL -> RGB round-trip") {
    val colors = List(RGB.Red, RGB.Green, RGB.Blue, RGB.Cyan, RGB.Magenta, RGB.Yellow, RGB.Orange)
    colors.foreach { original =>
      val roundTrip = original.toHSL.toRGB
      assertEqualsDouble(roundTrip.r, original.r, 0.01)
      assertEqualsDouble(roundTrip.g, original.g, 0.01)
      assertEqualsDouble(roundTrip.b, original.b, 0.01)
    }
  }

  test("HSL -> RGB -> HSL round-trip for chromatic colors") {
    val colors = List(HSL.Red, HSL.Green, HSL.Blue, HSL.Cyan, HSL.Magenta, HSL.Yellow)
    colors.foreach { original =>
      val roundTrip = HSL.fromRGB(original.toRGB)
      assertEqualsDouble(roundTrip.h, original.h, 1.0)
      assertEqualsDouble(roundTrip.s, original.s, 0.01)
      assertEqualsDouble(roundTrip.l, original.l, 0.01)
    }
  }

}
