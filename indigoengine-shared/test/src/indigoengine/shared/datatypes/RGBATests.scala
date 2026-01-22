package indigoengine.shared.datatypes

class RGBATests extends munit.FunSuite {

  test("Creating RGBA instances.Should convert from RGBA int values") {
    assertEquals(RGBA.fromColorInts(0, 0, 0, 0), RGBA.Black.withAlpha(0))
    assertEquals(RGBA.fromColorInts(255, 255, 255, 0), RGBA.White.withAlpha(0))
    assertEquals(RGBA.fromColorInts(255, 0, 0, 255), RGBA.Red)
    assertEquals(RGBA.fromColorInts(0, 255, 0, 255), RGBA.Green)
    assertEquals(RGBA.fromColorInts(0, 0, 255, 255), RGBA.Blue)

    val transparent = RGBA.fromColorInts(255, 255, 255, 127)
    assertEquals(transparent.a > 0.48 && transparent.a < 0.52, true)
  }

  test("Creating RGBA instances.should convert from RGB int values") {
    assertEquals(RGBA.fromColorInts(0, 0, 0), RGBA.Black)
    assertEquals(RGBA.fromColorInts(255, 255, 255), RGBA.White)
    assertEquals(RGBA.fromColorInts(255, 0, 0), RGBA.Red)
    assertEquals(RGBA.fromColorInts(0, 255, 0), RGBA.Green)
    assertEquals(RGBA.fromColorInts(0, 0, 255), RGBA.Blue)
  }

  test("Creating RGBA instances.should convert from Hexadecimal") {
    assertEquals(RGBA.fromHexString("0xFF0000FF"), RGBA.Red)
    assertEquals(RGBA.fromHexString("FF0000FF"), RGBA.Red)
    assertEquals(RGBA.fromHexString("#FF0000FF"), RGBA.Red)
    assertEquals(RGBA.fromHexString("00FF00FF"), RGBA.Green)
    assertEquals(RGBA.fromHexString("0000FFFF"), RGBA.Blue)

    assertEquals(RGBA.fromHexString("0xFF0000"), RGBA.Red)
    assertEquals(RGBA.fromHexString("FF0000"), RGBA.Red)
    assertEquals(RGBA.fromHexString("#FF0000"), RGBA.Red)
    assertEquals(RGBA.fromHexString("00FF00"), RGBA.Green)
    assertEquals(RGBA.fromHexString("#00FF00"), RGBA.Green)
    assertEquals(RGBA.fromHexString("0000FF"), RGBA.Blue)
    assertEquals(RGBA.fromHexString("#0000FF"), RGBA.Blue)

    val transparent = RGBA.fromHexString("0xFF000080")
    assertEquals(transparent.a > 0.48 && transparent.a < 0.52, true)
  }

  test("Can convert RGBA to Hex") {
    assertEquals(RGBA.Red.toHex, "ff0000ff")
    assertEquals(RGBA.Green.toHex, "00ff00ff")
    assertEquals(RGBA.Blue.toHex, "0000ffff")
    assertEquals(RGBA.Blue.toHex("#"), "#0000ffff")
  }

  test("mixing colours 50-50 red blue") {
    val colorA = RGBA.Red
    val colorB = RGBA.Blue

    val expected =
      RGBA(0.5, 0.0, 0.5, 1.0)

    val actual =
      colorA.mix(colorB)

    assertEquals(actual, expected)
  }

  test("mixing colours 50-50 red white") {
    val colorA = RGBA.Red
    val colorB = RGBA.White

    val expected =
      RGBA(1.0, 0.5, 0.5, 1.0)

    val actual =
      colorA.mix(colorB)

    assertEquals(actual, expected)
  }

  test("mixing colours 90-10 red white") {
    val colorA = RGBA.Red
    val colorB = RGBA.White

    val expected =
      RGBA(1.0, 0.1, 0.1, 1.0)

    val actual =
      colorA.mix(colorB, 0.1)

    assertEquals(actual, expected)
  }

  // HSL conversion tests

  test("luminance calculation for basic colors") {
    // White should have luminance ~1.0
    assert(RGBA.White.luminance > 0.99)

    // Black should have luminance ~0.0
    assert(RGBA.Black.luminance < 0.01)

    // Pure red has lower luminance than pure green
    assert(RGBA.Red.luminance < RGBA.Green.luminance)
  }

  test("isLight detection") {
    assertEquals(RGBA.White.isLight, true)
    assertEquals(RGBA.Black.isLight, false)
    assertEquals(RGBA.Yellow.isLight, true) // Yellow is bright
    assertEquals(RGBA.Blue.isLight, false)  // Pure blue is dark
  }

  test("toHSL for primary colors") {
    // Red: hue ~0, full saturation, 50% lightness
    val rHSL = RGBA.Red.toHSL
    assert(rHSL.h >= 0 && rHSL.h < 1 || rHSL.h > 359, s"Red hue should be ~0, got ${rHSL.h}")
    assertEqualsDouble(rHSL.s, 1.0, 0.01)
    assertEqualsDouble(rHSL.l, 0.5, 0.01)

    // Green: hue ~120
    val gHSL = RGBA.Green.toHSL
    assertEqualsDouble(gHSL.h, 120.0, 1.0)
    assertEqualsDouble(gHSL.s, 1.0, 0.01)
    assertEqualsDouble(gHSL.l, 0.5, 0.01)

    // Blue: hue ~240
    val bHSL = RGBA.Blue.toHSL
    assertEqualsDouble(bHSL.h, 240.0, 1.0)
    assertEqualsDouble(bHSL.s, 1.0, 0.01)
    assertEqualsDouble(bHSL.l, 0.5, 0.01)
  }

  test("toHSL for achromatic colors") {
    // White: 100% lightness, 0 saturation
    val wHSL = RGBA.White.toHSL
    assertEqualsDouble(wHSL.s, 0.0, 0.01)
    assertEqualsDouble(wHSL.l, 1.0, 0.01)

    // Black: 0% lightness, 0 saturation
    val blHSL = RGBA.Black.toHSL
    assertEqualsDouble(blHSL.s, 0.0, 0.01)
    assertEqualsDouble(blHSL.l, 0.0, 0.01)
  }

  test("fromHSL creates correct colors") {
    // Red from HSL
    val red = RGBA.fromHSL(HSL(0, 1.0, 0.5))
    assertEqualsDouble(red.r, 1.0, 0.01)
    assertEqualsDouble(red.g, 0.0, 0.01)
    assertEqualsDouble(red.b, 0.0, 0.01)

    // Green from HSL
    val green = RGBA.fromHSL(HSL(120, 1.0, 0.5))
    assertEqualsDouble(green.r, 0.0, 0.01)
    assertEqualsDouble(green.g, 1.0, 0.01)
    assertEqualsDouble(green.b, 0.0, 0.01)

    // Blue from HSL
    val blue = RGBA.fromHSL(HSL(240, 1.0, 0.5))
    assertEqualsDouble(blue.r, 0.0, 0.01)
    assertEqualsDouble(blue.g, 0.0, 0.01)
    assertEqualsDouble(blue.b, 1.0, 0.01)
  }

  test("HSL round-trip conversion") {
    val colors = List(RGBA.Red, RGBA.Green, RGBA.Blue, RGBA.Cyan, RGBA.Magenta, RGBA.Yellow, RGBA.Orange)
    colors.foreach { original =>
      val hsl       = original.toHSL
      val roundTrip = RGBA.fromHSL(hsl, original.a)
      assertEqualsDouble(roundTrip.r, original.r, 0.01)
      assertEqualsDouble(roundTrip.g, original.g, 0.01)
      assertEqualsDouble(roundTrip.b, original.b, 0.01)
    }
  }

  test("rotateHue by 180 degrees gives complementary color") {
    // Red (0°) -> Cyan (180°)
    val cyan = RGBA.Red.rotateHue(Degrees(180))
    assertEqualsDouble(cyan.r, 0.0, 0.01)
    assertEqualsDouble(cyan.g, 1.0, 0.01)
    assertEqualsDouble(cyan.b, 1.0, 0.01)

    // Green (120°) -> Magenta (300°)
    val magenta = RGBA.Green.rotateHue(Degrees(180))
    assertEqualsDouble(magenta.r, 1.0, 0.01)
    assertEqualsDouble(magenta.g, 0.0, 0.01)
    assertEqualsDouble(magenta.b, 1.0, 0.01)
  }

  test("rotateHue wraps around correctly") {
    // Blue (240°) + 180° = 420° -> 60° (Yellow)
    val yellow = RGBA.Blue.rotateHue(Degrees(180))
    assertEqualsDouble(yellow.r, 1.0, 0.01)
    assertEqualsDouble(yellow.g, 1.0, 0.01)
    assertEqualsDouble(yellow.b, 0.0, 0.01)
  }

  test("lighten increases lightness") {
    val lightRed = RGBA.Red.lighten(0.25)
    val hsl      = lightRed.toHSL
    // Original red has L=0.5, after lighten(0.25) should be 0.75
    assertEqualsDouble(hsl.l, 0.75, 0.01)
  }

  test("darken decreases lightness") {
    val darkRed = RGBA.Red.darken(0.25)
    val hsl     = darkRed.toHSL
    // Original red has L=0.5, after darken(0.25) should be 0.25
    assertEqualsDouble(hsl.l, 0.25, 0.01)
  }

  test("lighten and darken clamp to valid range") {
    // Lightening white should stay white (L=1.0)
    val lightWhite = RGBA.White.lighten(0.5)
    assertEqualsDouble(lightWhite.r, 1.0, 0.01)
    assertEqualsDouble(lightWhite.g, 1.0, 0.01)
    assertEqualsDouble(lightWhite.b, 1.0, 0.01)

    // Darkening black should stay black (L=0.0)
    val darkBlack = RGBA.Black.darken(0.5)
    assertEqualsDouble(darkBlack.r, 0.0, 0.01)
    assertEqualsDouble(darkBlack.g, 0.0, 0.01)
    assertEqualsDouble(darkBlack.b, 0.0, 0.01)
  }

  test("saturate increases saturation") {
    // Start with a desaturated color
    val muted     = RGBA.fromHSL(HSL(0, 0.5, 0.5)) // Red at 50% saturation
    val saturated = muted.saturate(0.3)
    val hsl       = saturated.toHSL
    assertEqualsDouble(hsl.s, 0.8, 0.01)
  }

  test("desaturate decreases saturation") {
    val desaturated = RGBA.Red.desaturate(0.5)
    val hsl         = desaturated.toHSL
    assertEqualsDouble(hsl.s, 0.5, 0.01)
  }

  test("HSL preserves alpha channel") {
    val semiTransparent = RGBA(1.0, 0.0, 0.0, 0.5)
    val rotated         = semiTransparent.rotateHue(Degrees(90))
    assertEquals(rotated.a, 0.5)

    val lightened = semiTransparent.lighten(0.2)
    assertEquals(lightened.a, 0.5)
  }

}
