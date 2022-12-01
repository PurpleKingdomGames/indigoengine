package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLCastingTests extends munit.FunSuite {

  test("casting") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>
        val x  = 1.0f.toInt
        val y  = 1.toFloat
        val z  = y.toInt
        val w1 = 2
        val w2 = (1 + w1).toFloat
        x + y
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    assertEquals(
      actual,
      s"""
      |int x=int(1.0);
      |float y=float(1);
      |int z=int(y);
      |int w1=2;
      |float w2=float(1+w1);
      |x+y;
      |""".stripMargin.trim
    )
  }

}
