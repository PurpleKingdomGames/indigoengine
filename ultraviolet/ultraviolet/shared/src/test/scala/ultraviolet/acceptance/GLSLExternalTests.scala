package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLExternalTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("Inlined external def") {

    inline def alpha: Float = 1.0f

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        vec4(1.0f, 1.0f, 0.0f, alpha)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)
    // println(ShaderMacros.toAST(fragment))

    assert(clue(actual) == clue("vec4(1.0,1.0,0.0,1.0);"))
  }

  test("Inlined external non-primitive (as def)") {

    inline def fn2: vec2 = vec2(0.0f, 1.0f)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        vec4(1.0f, 1.0f, fn2)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    assert(clue(actual) == clue("vec4(1.0,1.0,vec2(0.0,1.0));"))
  }

  test("Inlined external function") {
    // The argument here will be ignored and inlined. Inlines are weird.
    inline def fn1(v: Float): vec2 =
      vec2(v)

    inline def fn2: Float => vec2 =
      alpha => vec2(0.0f, alpha)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(fn1(1.0f), fn2(1.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 def0(in float alpha){
      |  return vec2(0.0,alpha);
      |}
      |vec4(vec2(1.0),def0(1.0));
      |""".stripMargin.trim
    )
  }

  test("Inlined external function N args") {

    // The argument here will be ignored and inlined. Inlines are weird.
    inline def fn1(red: Float, green: Float): vec2 =
      vec2(red, green)

    // Is treated like a function
    inline def fn2: (Float, Float) => vec2 =
      (blue, alpha) => vec2(blue, alpha)

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(fn1(1.0f, 0.25f), fn2(0.5f, 1.0f))
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    assertEquals(
      actual,
      s"""
      |vec2 def0(in float blue,in float alpha){
      |  return vec2(blue,alpha);
      |}
      |vec4(vec2(1.0,0.25),def0(0.5,1.0));
      |""".stripMargin.trim
    )
  }

}
