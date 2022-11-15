package ultraviolet.core

import ultraviolet.syntax.*

class EnvReaderTests extends munit.FunSuite {

  test("Create a UBO definition from a case class)") {

    case class FragEnv(
        alpha: Float,
        count: Int,
        UV: vec2,
        pos: vec3,
        COLOR: vec4
    )

    val actual =
      EnvReader.readUBO[FragEnv]

    val expected =
      UBODef(
        "FragEnv",
        List(
          UBOField(None, "float", "alpha"),
          UBOField(None, "int", "count"),
          UBOField(None, "vec2", "UV"),
          UBOField(None, "vec3", "pos"),
          UBOField(None, "vec4", "COLOR")
        )
      )

    assertEquals(actual, expected)
  }

  test("Create a UBO definition with precision annotated fields)") {

    case class FragEnv(
        a: highp[Float],
        b: mediump[Float],
        c: lowp[Float],
        d: Float
    )

    val actual =
      EnvReader.readUBO[FragEnv]

    val expected =
      UBODef(
        "FragEnv",
        List(
          UBOField(Option("highp"), "float", "a"),
          UBOField(Option("mediump"), "float", "b"),
          UBOField(Option("lowp"), "float", "c"),
          UBOField(None, "float", "d")
        )
      )

    assertEquals(actual, expected)
  }

}
