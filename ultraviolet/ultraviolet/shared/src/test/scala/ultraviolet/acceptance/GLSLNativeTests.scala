package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLNativeTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("can call a native function") {

    inline def circleSdf(p: vec2, r: Float): Float =
      length(p) - r

    inline def fragment =
      Shader[FragEnv, Float] { env =>
        circleSdf(vec2(1.0, 2.0), 3.0)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float circleSdf(in vec2 val0,in float val1){
      |  return length(val0)-3.0;
      |}
      |circleSdf(vec2(1.0,2.0),3.0);
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
      fragment.toGLSL[WebGL2].code

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
      fragment.toGLSL[WebGL2].code

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
