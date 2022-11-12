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
      EnvReader.UBODef(
        "FragEnv",
        List(
          EnvReader.UBOField("", "alpha", "float"),
          EnvReader.UBOField("", "count", "int"),
          EnvReader.UBOField("", "UV", "vec2"),
          EnvReader.UBOField("", "pos", "vec3"),
          EnvReader.UBOField("", "COLOR", "vec4")
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
      EnvReader.UBODef(
        "FragEnv",
        List(
          EnvReader.UBOField("highp", "a", "float"),
          EnvReader.UBOField("mediump", "b", "float"),
          EnvReader.UBOField("lowp", "c", "float"),
          EnvReader.UBOField("", "d", "float")
        )
      )

    assertEquals(actual, expected)
  }

}
