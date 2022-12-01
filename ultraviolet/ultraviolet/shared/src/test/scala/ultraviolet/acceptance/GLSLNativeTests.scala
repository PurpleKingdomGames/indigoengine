package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLNativeTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("can call a native function 1") {

    inline def circleSdf(p: vec2, r: Float): Float =
      length(p) - r

    inline def circleShader =
      Shader[FragEnv, Float] { env =>
        circleSdf(vec2(1.0, 2.0), 3.0)
      }

    val actual1 =
      circleShader.toGLSL[WebGL2].code

    // println(ShaderMacros.toAST(circleShader))

    assertEquals(
      actual1,
      s"""
      |float circleSdf(in vec2 val0,in float val1){
      |  return length(val0)-3.0;
      |}
      |circleSdf(vec2(1.0,2.0),3.0);
      |""".stripMargin.trim
    )
  }

  test("can call a native function 2") {

    inline def circleShader2: Shader[FragEnv, Float] =
      Shader { env =>
        def circleSdf(p: vec2, r: Float): Float =
          length(p) - r

        circleSdf(env.UV, 3.0)
      }

    val actual2 =
      circleShader2.toGLSL[WebGL2].code

    // println(ShaderMacros.toAST(circleShader2))

    assertEquals(
      actual2,
      s"""
      |float circleSdf(in vec2 p,in float r){
      |  return length(p)-r;
      |}
      |circleSdf(UV,3.0);
      |""".stripMargin.trim
    )
  }

}
