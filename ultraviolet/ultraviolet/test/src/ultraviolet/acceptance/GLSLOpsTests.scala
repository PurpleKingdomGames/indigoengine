package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class GLSLOpsTests extends munit.FunSuite {

  test("Will convert % to mod() for Float types") {

    inline def fragment =
      Shader {
        def main: Unit =
          val x = mod(10.0f, 2.0f)
          val y = 2.0f
          val z = 10.0f % y
          val w = 10.0f % 3.0f
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |void main(){
      |  float x=mod(10.0,2.0);
      |  float y=2.0;
      |  float z=mod(10.0,y);
      |  float w=1.0;
      |}
      |""".stripMargin.trim
    )
  }

  test("Will retain % for Int types") {

    inline def fragment =
      Shader {
        def main: Unit =
          val i: Int = 10
          val x: Int = i % 3
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |void main(){
      |  int i=10;
      |  int x=i%3;
      |}
      |""".stripMargin.trim
    )
  }

  test("clamp vec3 will accept float gentypes") {

    inline def fragment =
      Shader {
        def main: Unit =
          val x = clamp(vec4(1.0f), vec4(0.0f), vec4(1.0f))
          val y = clamp(vec4(1.0f), 0.0f, 1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |void main(){
      |  vec4 x=clamp(vec4(1.0),vec4(0.0),vec4(1.0));
      |  vec4 y=clamp(vec4(1.0),0.0,1.0);
      |}
      |""".stripMargin.trim
    )
  }

}
