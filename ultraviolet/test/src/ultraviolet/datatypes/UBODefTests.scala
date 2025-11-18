package ultraviolet.datatypes

import ultraviolet.macros.UBOReader
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class UBODefTests extends munit.FunSuite {

  test("UBOField renders correctly") {
    val actual =
      UBOField(Option("highp"), "float", "TIME").render

    val expected =
      "highp float TIME;"

    assertEquals(actual, expected)
  }

  test("UBODef renders correctly") {
    val actual =
      UBODef(
        "MyCustomData",
        List(
          UBOField(Option("highp"), "float", "TIME"),
          UBOField(None, "vec2", "VIEWPORT_SIZE")
        )
      ).render

    val expected =
      s"""
      |layout (std140) uniform MyCustomData {
      |  highp float TIME;
      |  vec2 VIEWPORT_SIZE;
      |};
      |""".stripMargin.trim

    assertEquals(actual, expected)
  }

  test("UBODef renders correctly with an array") {

    case class FragEnv(
        ALPHA: Float,
        VERTICES: array[16, vec2],
        COLORS: array[8, vec4]
    )

    val actualDef =
      UBOReader.readUBO[FragEnv]

    val expectedDef =
      UBODef(
        "FragEnv",
        List(
          UBOField(None, "float", "ALPHA"),
          UBOField(None, "vec2[16]", "VERTICES"),
          UBOField(None, "vec4[8]", "COLORS")
        )
      )

    assertEquals(actualDef, expectedDef)

    val actualRender =
      actualDef.render

    val expectedRender =
      s"""
      |layout (std140) uniform FragEnv {
      |  float ALPHA;
      |  vec2[16] VERTICES;
      |  vec4[8] COLORS;
      |};
      |""".stripMargin.trim

    assertEquals(actualRender, expectedRender)
  }

}
