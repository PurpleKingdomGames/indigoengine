package indigoengine.shared.datatypes

class HSVTests extends munit.FunSuite {

  test("HSV color constants have correct values") {
    assertEquals(HSV.Red, HSV(0, 1.0, 1.0))
    assertEquals(HSV.Green, HSV(120, 1.0, 1.0))
    assertEquals(HSV.Blue, HSV(240, 1.0, 1.0))
    assertEquals(HSV.Yellow, HSV(60, 1.0, 1.0))
    assertEquals(HSV.Cyan, HSV(180, 1.0, 1.0))
    assertEquals(HSV.Magenta, HSV(300, 1.0, 1.0))
    assertEquals(HSV.White, HSV(0, 0.0, 1.0))
    assertEquals(HSV.Black, HSV(0, 0.0, 0.0))
  }

  test("withHue creates a new HSV with updated hue") {
    val hsv = HSV(100, 0.5, 0.5)
    assertEquals(hsv.withHue(200), HSV(200, 0.5, 0.5))
  }

  test("withSaturation creates a new HSV with updated saturation") {
    val hsv = HSV(100, 0.5, 0.5)
    assertEquals(hsv.withSaturation(0.8), HSV(100, 0.8, 0.5))
  }

  test("withValue creates a new HSV with updated value") {
    val hsv = HSV(100, 0.5, 0.5)
    assertEquals(hsv.withValue(0.8), HSV(100, 0.5, 0.8))
  }

  test("rotateHue rotates hue correctly") {
    assertEquals(HSV.Red.rotateHue(Degrees(120)).h, 120.0)
    assertEquals(HSV.Red.rotateHue(Degrees(240)).h, 240.0)
    assertEquals(HSV.Red.rotateHue(Degrees(360)).h, 0.0)
  }

  test("rotateHue wraps around at 360") {
    assertEquals(HSV.Blue.rotateHue(Degrees(180)).h, 60.0) // 240 + 180 = 420 -> 60
  }

  test("rotateHue handles negative rotation") {
    val rotated = HSV.Red.rotateHue(Degrees(-90))
    assertEqualsDouble(rotated.h, 270.0, 0.01)
  }

  test("brighten increases value") {
    val brightened = HSV.Red.brighten(0.25)
    assertEqualsDouble(brightened.v, 1.0, 0.01) // Already at max
    
    val hsv = HSV(0, 1.0, 0.5)
    val brightened2 = hsv.brighten(0.25)
    assertEqualsDouble(brightened2.v, 0.75, 0.01)
  }

  test("brighten clamps to 1.0") {
    val brightened = HSV.White.brighten(0.5)
    assertEqualsDouble(brightened.v, 1.0, 0.01)
  }

  test("darken decreases value") {
    val darkened = HSV.Red.darken(0.25)
    assertEqualsDouble(darkened.v, 0.75, 0.01)
  }

  test("darken clamps to 0.0") {
    val darkened = HSV.Black.darken(0.5)
    assertEqualsDouble(darkened.v, 0.0, 0.01)
  }

  test("saturate increases saturation") {
    val hsv       = HSV(0, 0.5, 1.0)
    val saturated = hsv.saturate(0.3)
    assertEqualsDouble(saturated.s, 0.8, 0.01)
  }

  test("saturate clamps to 1.0") {
    val saturated = HSV.Red.saturate(0.5)
    assertEqualsDouble(saturated.s, 1.0, 0.01)
  }

  test("desaturate decreases saturation") {
    val desaturated = HSV.Red.desaturate(0.5)
    assertEqualsDouble(desaturated.s, 0.5, 0.01)
  }

  test("desaturate clamps to 0.0") {
    val desaturated = HSV.White.desaturate(0.5)
    assertEqualsDouble(desaturated.s, 0.0, 0.01)
  }

  test("toRGB converts to RGB correctly") {
    val red = HSV.Red.toRGB
    assertEqualsDouble(red.r, 1.0, 0.01)
    assertEqualsDouble(red.g, 0.0, 0.01)
    assertEqualsDouble(red.b, 0.0, 0.01)

    val green = HSV.Green.toRGB
    assertEqualsDouble(green.r, 0.0, 0.01)
    assertEqualsDouble(green.g, 1.0, 0.01)
    assertEqualsDouble(green.b, 0.0, 0.01)

    val blue = HSV.Blue.toRGB
    assertEqualsDouble(blue.r, 0.0, 0.01)
    assertEqualsDouble(blue.g, 0.0, 0.01)
    assertEqualsDouble(blue.b, 1.0, 0.01)
  }

  test("toRGBA converts to RGBA with full opacity") {
    val rgba = HSV.Red.toRGBA
    assertEqualsDouble(rgba.r, 1.0, 0.01)
    assertEqualsDouble(rgba.g, 0.0, 0.01)
    assertEqualsDouble(rgba.b, 0.0, 0.01)
    assertEqualsDouble(rgba.a, 1.0, 0.01)
  }

  test("toHSVA converts to HSVA with full opacity") {
    val hsva = HSV.Red.toHSVA
    assertEquals(hsva.h, 0.0)
    assertEquals(hsva.s, 1.0)
    assertEquals(hsva.v, 1.0)
    assertEquals(hsva.a, 1.0)
  }

  test("fromRGB creates HSV correctly") {
    val hsv = HSV.fromRGB(RGB.Red)
    assertEqualsDouble(hsv.h, 0.0, 1.0)
    assertEqualsDouble(hsv.s, 1.0, 0.01)
    assertEqualsDouble(hsv.v, 1.0, 0.01)
  }

  test("fromRGBA creates HSV correctly (ignores alpha)") {
    val hsv = HSV.fromRGBA(RGBA(1.0, 0.0, 0.0, 0.5))
    assertEqualsDouble(hsv.h, 0.0, 1.0)
    assertEqualsDouble(hsv.s, 1.0, 0.01)
    assertEqualsDouble(hsv.v, 1.0, 0.01)
  }

  test("RGB -> HSV -> RGB round-trip") {
    val colors = List(RGB.Red, RGB.Green, RGB.Blue, RGB.Cyan, RGB.Magenta, RGB.Yellow, RGB.Orange)
    colors.foreach { original =>
      val roundTrip = original.toHSV.toRGB
      assertEqualsDouble(roundTrip.r, original.r, 0.01)
      assertEqualsDouble(roundTrip.g, original.g, 0.01)
      assertEqualsDouble(roundTrip.b, original.b, 0.01)
    }
  }

  test("HSV -> RGB -> HSV round-trip for chromatic colors") {
    val colors = List(HSV.Red, HSV.Green, HSV.Blue, HSV.Cyan, HSV.Magenta, HSV.Yellow)
    colors.foreach { original =>
      val roundTrip = HSV.fromRGB(original.toRGB)
      assertEqualsDouble(roundTrip.h, original.h, 1.0)
      assertEqualsDouble(roundTrip.s, original.s, 0.01)
      assertEqualsDouble(roundTrip.v, original.v, 0.01)
    }
  }

}
