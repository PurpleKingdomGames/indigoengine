package ultraviolet

import scala.util.Random

import syntax.*

class SyntaxTests extends munit.FunSuite {

  test("hex interpolator") {
    assertEquals(hex"#00FF00", vec3(0f, 1f, 0f))
    assertEquals(hex"#ff00ff", vec3(1f, 0f, 1f))
    val (hex1, hex2, hex3) = ("00", "ff", "00")
    assertEquals(hex"#$hex1$hex2$hex3", vec3(0f, 1f, 0f))

    intercept[IllegalArgumentException](hex"#00000"): Unit
    intercept[IllegalArgumentException](hex"#0000000"): Unit
    intercept[IllegalArgumentException](hex"#gggggg"): Unit
  }

  test("hexa interpolator") {
    assertEquals(hexa"#00FF00FF", vec4(0f, 1f, 0f, 1f))
    assertEquals(hexa"#ff00ff00", vec4(1f, 0f, 1f, 0f))
    val (hex1, hex2, hex3, hex4) = ("00", "ff", "00", "ff")
    assertEquals(hexa"#$hex1$hex2$hex3$hex4", vec4(0f, 1f, 0f, 1f))

    intercept[IllegalArgumentException](hexa"#0000000"): Unit
    intercept[IllegalArgumentException](hexa"#000000000"): Unit
    intercept[IllegalArgumentException](hexa"#gggggggg"): Unit
  }

  test("rgb interpolator") {
    assertEquals(rgb"0,0,0", vec3(0f, 0f, 0f))
    assertEquals(rgb"255,0,255", vec3(1f, 0f, 1f))
    val (int1, int2, int3) = (0, 255, 0)
    assertEquals(rgb"$int1,$int2,$int3", vec3(0f, 1f, 0f))

    intercept[IllegalArgumentException](rgb"0,0"): Unit
    intercept[IllegalArgumentException](rgb"0,0,0,0"): Unit
    intercept[IllegalArgumentException](rgb"0, 0, 0"): Unit
    intercept[IllegalArgumentException](rgb"-1,0,0"): Unit
    intercept[IllegalArgumentException](rgb"256,0,0"): Unit
  }

  test("rgba interpolator") {
    assertEquals(rgba"0,0,0,0", vec4(0f, 0f, 0f, 0f))
    assertEquals(rgba"255,0,255,0", vec4(1f, 0f, 1f, 0f))
    val (int1, int2, int3, int4) = (0, 255, 0, 255)
    assertEquals(rgba"$int1,$int2,$int3,$int4", vec4(0f, 1f, 0f, 1f))

    intercept[IllegalArgumentException](rgba"0,0,0"): Unit
    intercept[IllegalArgumentException](rgba"0,0,0,0,0"): Unit
    intercept[IllegalArgumentException](rgba"0, 0, 0, 0"): Unit
    intercept[IllegalArgumentException](rgba"-1,0,0,0"): Unit
    intercept[IllegalArgumentException](rgba"256,0,0,0"): Unit
  }
}
