package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLRawTests extends munit.FunSuite {

  test("can embed raw GLSL") {
    inline def fragment: Shader[Unit, RawGLSL] =
      Shader { _ =>
        RawGLSL("float v = 1.0;")
        raw("COLOR = vec4(v, v, v, 0.5);")
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float v = 1.0;
      |COLOR = vec4(v, v, v, 0.5);
      |""".stripMargin.trim
    )
  }

}
