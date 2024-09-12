package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class GLSLCastingTests extends munit.FunSuite {

  test("casting") {
    inline def fragment: Shader[Unit, Float] =
      Shader { _ =>
        val x  = 1.0f.toInt // Cast is inlined on literal
        val y  = 1.toFloat  // Cast is inlined on literal
        val z  = x.toFloat
        val zz = y.toInt
        val w1 = 2
        val w2 = (1 + w1).toFloat
        x + y
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)

    assertEquals(
      actual,
      s"""
      |int x=1;
      |float y=1.0;
      |float z=float(x);
      |int zz=int(y);
      |int w1=2;
      |float w2=float(1+w1);
      |x+y;
      |""".stripMargin.trim
    )
  }

}
