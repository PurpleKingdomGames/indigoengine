package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLUBOTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("Can define a UBO struct") {

    case class UBO1(TIME: highp[Float], val VIEWPORT_SIZE: vec2)
    case class UBO2(customColor: vec4, pos: lowp[vec3], VERTICES: array[16, vec2])

    inline def fragment =
      Shader[UBO1 & UBO2 & FragEnv, Unit](
        GLSLHeader.Version300ES,
        GLSLHeader.PrecisionHighPFloat
      ) { env =>
        ubo[UBO1]
        ubo[UBO2]
        env.COLOR = vec4(env.UV, env.TIME, 1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |#version 300 es
      |precision highp float;
      |layout (std140) uniform UBO1 {
      |  highp float TIME;
      |  vec2 VIEWPORT_SIZE;
      |};
      |layout (std140) uniform UBO2 {
      |  vec4 customColor;
      |  lowp vec3 pos;
      |  vec2[16] VERTICES;
      |};
      |COLOR=vec4(UV,TIME,1.0);
      |""".stripMargin.trim
    )

  }

}
