package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class WebGL2Tests extends munit.FunSuite {

  test("Can generate the simplest valid WebGL 2.0 fragment shader") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
    inline def fragment =
      Shader {
        @out var outColor: vec4 = null

        def main: Unit =
          outColor = vec4(1.0f, 0.0f, 0.5f, 1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2](Version300ES, PrecisionHighPFloat).toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    val expected: String =
      """
      |#version 300 es
      |precision highp float;
      |out vec4 outColor;
      |void main(){
      |  outColor=vec4(1.0,0.0,0.5,1.0);
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)
  }

}
