package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class GLSLNativeTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("inlined external functions are inlined") {

    inline def circleSdf(p: vec2, r: Float): Float =
      length(p) - r

    inline def fragment =
      Shader[FragEnv, Float] { env =>
        val x = 1.0f

        circleSdf(vec2(x, 2.0f), 3.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=1.0;
      |length(vec2(x,2.0))-3.0;
      |""".stripMargin.trim
    )
  }

  test("can call an external lambda function") {

    inline def circleSdf = (p: vec2, r: Float) => length(p) - r

    inline def fragment =
      Shader[FragEnv, Float] { env =>
        val x = 1.0f

        val proxy: (vec2, Float) => Float = circleSdf

        proxy(vec2(x, 2.0), 3.0)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float def0(in vec2 p,in float r){
      |  return length(p)-r;
      |}
      |float x=1.0;
      |def0(vec2(x,2.0),3.0);
      |""".stripMargin.trim
    )
  }

  test("can call an internal function") {

    inline def fragment: Shader[FragEnv, Float] =
      Shader { env =>
        def circleSdf(p: vec2, r: Float): Float =
          length(p) - r

        circleSdf(env.UV, 3.0)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float circleSdf(in vec2 p,in float r){
      |  return length(p)-r;
      |}
      |circleSdf(UV,3.0);
      |""".stripMargin.trim
    )
  }

  test("can call a stub function from the environment") {

    case class Env():
      def circleSdf(p: vec2, r: Float): Float =
        length(p) - r

    inline def fragment: Shader[FragEnv & Env, Float] =
      Shader { env =>
        env.circleSdf(env.UV, 3.0)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |circleSdf(UV,3.0);
      |""".stripMargin.trim
    )
  }

}
