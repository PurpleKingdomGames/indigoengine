package indigoengine.shared.datatypes

class RGBTests extends munit.FunSuite {

  test("Creating RGB instances.should convert from RGB int values") {
    assertEquals(RGB.fromColorInts(0, 0, 0), RGB.Black)
    assertEquals(RGB.fromColorInts(255, 255, 255), RGB.White)
    assertEquals(RGB.fromColorInts(255, 0, 0), RGB.Red)
    assertEquals(RGB.fromColorInts(0, 255, 0), RGB.Green)
    assertEquals(RGB.fromColorInts(0, 0, 255), RGB.Blue)
  }

  test("Creating RGB instances.should convert from Hexadecimal") {
    assertEquals(RGB.fromHex("0xFF0000"), RGB.Red)
    assertEquals(RGB.fromHex("#FF0000"), RGB.Red)
    assertEquals(RGB.fromHex("FF0000"), RGB.Red)
    assertEquals(RGB.fromHex("00FF00"), RGB.Green)
    assertEquals(RGB.fromHex("#00FF00"), RGB.Green)
    assertEquals(RGB.fromHex("0000FF"), RGB.Blue)
    assertEquals(RGB.fromHex("#0000FF"), RGB.Blue)
    assertEquals(RGB.fromHex("0xFF0000FF"), RGB.Red)
  }

  test("Can convert RGBA to Hex") {
    assertEquals(RGB.Red.toHex, "ff0000")
    assertEquals(RGB.Green.toHex, "00ff00")
    assertEquals(RGB.Blue.toHex, "0000ff")
  }

  test("mixing colours 50-50 red blue") {
    val colorA = RGB.Red
    val colorB = RGB.Blue

    val expected =
      RGB(0.5, 0.0, 0.5)

    val actual =
      colorA.mix(colorB)

    assertEquals(actual, expected)
  }

  test("mixing colours 50-50 red white") {
    val colorA = RGB.Red
    val colorB = RGB.White

    val expected =
      RGB(1.0, 0.5, 0.5)

    val actual =
      colorA.mix(colorB)

    assertEquals(actual, expected)
  }

  test("mixing colours 90-10 red white") {
    val colorA = RGB.Red
    val colorB = RGB.White

    val expected =
      RGB(1.0, 0.1, 0.1)

    val actual =
      colorA.mix(colorB, 0.1)

    assertEquals(actual, expected)
  }

}
