package ultraviolet.datatypes

import ultraviolet.syntax.*

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

}
