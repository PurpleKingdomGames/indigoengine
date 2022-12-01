package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLEnvTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("Programs can use an env value like env.UV as UV") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        vec4(env.UV, 0.0f, 1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    assertEquals(
      actual,
      s"""
      |vec4(UV,0.0,1.0);
      |""".stripMargin.trim
    )
  }

  test("Programs can use and negate env values") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { env =>
        val a = vec2(1.0f, 2.0f)
        val b = -a
        val c = -a.x
        val d = -env.UV.y
        vec4(env.UV, 0.0f, 1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)

    assertEquals(
      actual,
      s"""
      |vec2 a=vec2(1.0,2.0);
      |vec2 b=-a;
      |float c=-a.x;
      |float d=-UV.y;
      |vec4(UV,0.0,1.0);
      |""".stripMargin.trim
    )
  }

}
