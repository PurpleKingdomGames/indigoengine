package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLOpsTests extends munit.FunSuite {

  test("modulus") {

    inline def fragment =
      Shader {
        val x = mod(10.0f, 2.0f)
        val y = 2.0f
        val z = 10.0f % y
        val w = 10.0f % 3.0f
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=mod(10.0,2.0);
      |float y=2.0;
      |float z=mod(10.0,y);
      |float w=1.0;
      |""".stripMargin.trim
    )
  }

}
