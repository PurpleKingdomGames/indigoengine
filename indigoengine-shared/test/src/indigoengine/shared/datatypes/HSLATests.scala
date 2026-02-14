package indigoengine.shared.datatypes

class HSLATests extends munit.FunSuite {

  test("HSLA color constants have correct values") {
    assertEquals(HSLA.Red, HSLA(0, 1.0, 0.5, 1.0))
    assertEquals(HSLA.Green, HSLA(120, 1.0, 0.5, 1.0))
    assertEquals(HSLA.Blue, HSLA(240, 1.0, 0.5, 1.0))
    assertEquals(HSLA.Yellow, HSLA(60, 1.0, 0.5, 1.0))
    assertEquals(HSLA.Cyan, HSLA(180, 1.0, 0.5, 1.0))
    assertEquals(HSLA.Magenta, HSLA(300, 1.0, 0.5, 1.0))
    assertEquals(HSLA.White, HSLA(0, 0.0, 1.0, 1.0))
    assertEquals(HSLA.Black, HSLA(0, 0.0, 0.0, 1.0))
    assertEquals(HSLA.Zero, HSLA(0, 0.0, 0.0, 0.0))
  }

  test("HSLA apply with 3 args creates opaque color") {
    assertEquals(HSLA(100, 0.5, 0.5), HSLA(100, 0.5, 0.5, 1.0))
  }

  test("withHue creates a new HSLA with updated hue") {
    val hsla = HSLA(100, 0.5, 0.5, 0.8)
    assertEquals(hsla.withHue(200), HSLA(200, 0.5, 0.5, 0.8))
  }

  test("withSaturation creates a new HSLA with updated saturation") {
    val hsla = HSLA(100, 0.5, 0.5, 0.8)
    assertEquals(hsla.withSaturation(0.8), HSLA(100, 0.8, 0.5, 0.8))
  }

  test("withLightness creates a new HSLA with updated lightness") {
    val hsla = HSLA(100, 0.5, 0.5, 0.8)
    assertEquals(hsla.withLightness(0.8), HSLA(100, 0.5, 0.8, 0.8))
  }

  test("withAlpha creates a new HSLA with updated alpha") {
    val hsla = HSLA(100, 0.5, 0.5, 0.8)
    assertEquals(hsla.withAlpha(0.3), HSLA(100, 0.5, 0.5, 0.3))
  }

  test("makeOpaque sets alpha to 1.0") {
    val hsla = HSLA(100, 0.5, 0.5, 0.3)
    assertEquals(hsla.makeOpaque, HSLA(100, 0.5, 0.5, 1.0))
  }

  test("makeTransparent sets alpha to 0.0") {
    val hsla = HSLA(100, 0.5, 0.5, 0.8)
    assertEquals(hsla.makeTransparent, HSLA(100, 0.5, 0.5, 0.0))
  }

  test("rotateHue rotates hue correctly and preserves alpha") {
    val hsla    = HSLA(0, 1.0, 0.5, 0.5)
    val rotated = hsla.rotateHue(Degrees(120))
    assertEquals(rotated.h, 120.0)
    assertEquals(rotated.a, 0.5)
  }

  test("rotateHue wraps around at 360") {
    val rotated = HSLA.Blue.rotateHue(Degrees(180))
    assertEquals(rotated.h, 60.0) // 240 + 180 = 420 -> 60
  }

  test("rotateHue handles negative rotation") {
    val rotated = HSLA.Red.rotateHue(Degrees(-90))
    assertEqualsDouble(rotated.h, 270.0, 0.01)
  }

  test("lighten increases lightness and preserves alpha") {
    val hsla      = HSLA(0, 1.0, 0.5, 0.5)
    val lightened = hsla.lighten(0.25)
    assertEqualsDouble(lightened.l, 0.75, 0.01)
    assertEquals(lightened.a, 0.5)
  }

  test("lighten clamps to 1.0") {
    val lightened = HSLA.White.lighten(0.5)
    assertEqualsDouble(lightened.l, 1.0, 0.01)
  }

  test("darken decreases lightness and preserves alpha") {
    val hsla     = HSLA(0, 1.0, 0.5, 0.5)
    val darkened = hsla.darken(0.25)
    assertEqualsDouble(darkened.l, 0.25, 0.01)
    assertEquals(darkened.a, 0.5)
  }

  test("darken clamps to 0.0") {
    val darkened = HSLA.Black.darken(0.5)
    assertEqualsDouble(darkened.l, 0.0, 0.01)
  }

  test("saturate increases saturation and preserves alpha") {
    val hsla      = HSLA(0, 0.5, 0.5, 0.5)
    val saturated = hsla.saturate(0.3)
    assertEqualsDouble(saturated.s, 0.8, 0.01)
    assertEquals(saturated.a, 0.5)
  }

  test("saturate clamps to 1.0") {
    val saturated = HSLA.Red.saturate(0.5)
    assertEqualsDouble(saturated.s, 1.0, 0.01)
  }

  test("desaturate decreases saturation and preserves alpha") {
    val hsla        = HSLA(0, 1.0, 0.5, 0.5)
    val desaturated = hsla.desaturate(0.5)
    assertEqualsDouble(desaturated.s, 0.5, 0.01)
    assertEquals(desaturated.a, 0.5)
  }

  test("desaturate clamps to 0.0") {
    val desaturated = HSLA.White.desaturate(0.5)
    assertEqualsDouble(desaturated.s, 0.0, 0.01)
  }

  test("toHSL drops alpha") {
    val hsla = HSLA(100, 0.5, 0.6, 0.3)
    val hsl  = hsla.toHSL
    assertEquals(hsl.h, 100.0)
    assertEquals(hsl.s, 0.5)
    assertEquals(hsl.l, 0.6)
  }

  test("toRGB converts correctly (drops alpha)") {
    val rgb = HSLA.Red.toRGB
    assertEqualsDouble(rgb.r, 1.0, 0.01)
    assertEqualsDouble(rgb.g, 0.0, 0.01)
    assertEqualsDouble(rgb.b, 0.0, 0.01)
  }

  test("toRGBA converts correctly and preserves alpha") {
    val hsla = HSLA(0, 1.0, 0.5, 0.5)
    val rgba = hsla.toRGBA
    assertEqualsDouble(rgba.r, 1.0, 0.01)
    assertEqualsDouble(rgba.g, 0.0, 0.01)
    assertEqualsDouble(rgba.b, 0.0, 0.01)
    assertEqualsDouble(rgba.a, 0.5, 0.01)
  }

  test("toRGBA for all primary colors") {
    val red = HSLA.Red.toRGBA
    assertEqualsDouble(red.r, 1.0, 0.01)
    assertEqualsDouble(red.g, 0.0, 0.01)
    assertEqualsDouble(red.b, 0.0, 0.01)

    val green = HSLA.Green.toRGBA
    assertEqualsDouble(green.r, 0.0, 0.01)
    assertEqualsDouble(green.g, 1.0, 0.01)
    assertEqualsDouble(green.b, 0.0, 0.01)

    val blue = HSLA.Blue.toRGBA
    assertEqualsDouble(blue.r, 0.0, 0.01)
    assertEqualsDouble(blue.g, 0.0, 0.01)
    assertEqualsDouble(blue.b, 1.0, 0.01)

    val yellow = HSLA.Yellow.toRGBA
    assertEqualsDouble(yellow.r, 1.0, 0.01)
    assertEqualsDouble(yellow.g, 1.0, 0.01)
    assertEqualsDouble(yellow.b, 0.0, 0.01)

    val cyan = HSLA.Cyan.toRGBA
    assertEqualsDouble(cyan.r, 0.0, 0.01)
    assertEqualsDouble(cyan.g, 1.0, 0.01)
    assertEqualsDouble(cyan.b, 1.0, 0.01)

    val magenta = HSLA.Magenta.toRGBA
    assertEqualsDouble(magenta.r, 1.0, 0.01)
    assertEqualsDouble(magenta.g, 0.0, 0.01)
    assertEqualsDouble(magenta.b, 1.0, 0.01)
  }

  test("toCSSValue formats correctly") {
    assertEquals(HSLA.Red.toCSSValue, "hsla(0, 100%, 50%, 100%)")
    assertEquals(HSLA(120, 0.5, 0.6, 0.5).toCSSValue, "hsla(120, 50%, 60%, 50%)")
  }

  test("fromRGBA preserves alpha") {
    val rgba = RGBA(1.0, 0.0, 0.0, 0.5)
    val hsla = HSLA.fromRGBA(rgba)
    assertEqualsDouble(hsla.h, 0.0, 1.0)
    assertEqualsDouble(hsla.s, 1.0, 0.01)
    assertEqualsDouble(hsla.l, 0.5, 0.01)
    assertEqualsDouble(hsla.a, 0.5, 0.01)
  }

  test("fromRGB creates opaque HSLA") {
    val rgb  = RGB.Red
    val hsla = HSLA.fromRGB(rgb)
    assertEqualsDouble(hsla.h, 0.0, 1.0)
    assertEqualsDouble(hsla.s, 1.0, 0.01)
    assertEqualsDouble(hsla.l, 0.5, 0.01)
    assertEquals(hsla.a, 1.0)
  }

  test("RGBA -> HSLA -> RGBA round-trip") {
    val colors = List(RGBA.Red, RGBA.Green, RGBA.Blue, RGBA.Cyan, RGBA.Magenta, RGBA.Yellow, RGBA.Orange)
    colors.foreach { original =>
      val roundTrip = HSLA.fromRGBA(original).toRGBA
      assertEqualsDouble(roundTrip.r, original.r, 0.01)
      assertEqualsDouble(roundTrip.g, original.g, 0.01)
      assertEqualsDouble(roundTrip.b, original.b, 0.01)
      assertEqualsDouble(roundTrip.a, original.a, 0.01)
    }
  }

  test("RGBA -> HSLA -> RGBA round-trip with alpha") {
    val original  = RGBA(1.0, 0.5, 0.0, 0.7)
    val roundTrip = HSLA.fromRGBA(original).toRGBA
    assertEqualsDouble(roundTrip.r, original.r, 0.01)
    assertEqualsDouble(roundTrip.g, original.g, 0.01)
    assertEqualsDouble(roundTrip.b, original.b, 0.01)
    assertEqualsDouble(roundTrip.a, original.a, 0.01)
  }

}
