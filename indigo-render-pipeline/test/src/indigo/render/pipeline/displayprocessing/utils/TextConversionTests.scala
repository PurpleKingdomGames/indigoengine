package indigo.render.pipeline.displayprocessing.utils

import indigo.core.datatypes.FontChar
import indigo.core.datatypes.FontInfo
import indigo.core.datatypes.FontKey
import indigo.core.datatypes.Rectangle
import indigoengine.shared.collections.Batch

class TextConversionTests extends munit.FunSuite {

  val unknownChar = FontChar("?", Rectangle(0, 0, 10, 10))
  val charA       = FontChar("A", Rectangle(0, 0, 8, 12))
  val charB       = FontChar("B", Rectangle(8, 0, 7, 12))
  val charC       = FontChar("C", Rectangle(15, 0, 9, 12))
  val fontInfo    = FontInfo(FontKey("test"), unknownChar, charA, charB, charC)

  test("zipWithCharDetails - empty string") {
    val result = TextConversion.zipWithCharDetails(Batch.empty[Char], fontInfo, 0)
    assertEquals(result.length, 0)
  }

  test("zipWithCharDetails - single character, no spacing") {
    val result = TextConversion.zipWithCharDetails(Batch('A'), fontInfo, 0)
    assertEquals(result.length, 1)
    assertEquals(result(0)._1, charA)
    assertEquals(result(0)._2, 0)
  }

  test("zipWithCharDetails - multiple characters with spacing") {
    val result = TextConversion.zipWithCharDetails(Batch('A', 'B', 'C'), fontInfo, 2)
    assertEquals(result.length, 3)
    assertEquals(result(0)._2, 0)
    assertEquals(result(1)._2, 10)
    assertEquals(result(2)._2, 19)
  }

  test("zipWithCharDetails - unknown character uses fallback") {
    val result = TextConversion.zipWithCharDetails(Batch('Z'), fontInfo, 0)
    assertEquals(result.length, 1)
    assertEquals(result(0)._1, unknownChar)
  }

  test("zipWithCharDetails - no letter spacing") {
    val result = TextConversion.zipWithCharDetails(Batch('A', 'B'), fontInfo, 0)
    assertEquals(result.length, 2)
    assertEquals(result(0)._2, 0)
    assertEquals(result(1)._2, 8)
  }
}
