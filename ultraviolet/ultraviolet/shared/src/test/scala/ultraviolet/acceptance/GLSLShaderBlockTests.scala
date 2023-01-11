package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLShaderBlockTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("Shader blocks can be nested") {

    inline def foo =
      Shader[FragEnv, Unit] { env2 =>
        val f = env2.COLOR.x
      }

    inline def fragment =
      Shader[FragEnv, Unit] { env =>
        val x = env.UV.y
        foo

        Shader {
          val b = 1.0f
        }
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=UV.y;
      |float f=COLOR.x;
      |float b=1.0;
      |""".stripMargin.trim
    )

  }

  test("Shader blocks can be nested using an intermediary") {

    trait IShader {
      inline def frag: Shader[Unit, vec4]
    }

    class CustomShader extends IShader:
      inline def frag =
        Shader[Unit, vec4] { env =>
          vec4(123.0f)
        }

    inline def fragment(inline s: CustomShader): Shader[FragEnv, Unit] =
      Shader[FragEnv, Unit] { env =>
        val x = env.UV.y

        Shader {
          val b = 1.0f
        }

        s.frag
      }

    val actual =
      fragment(new CustomShader).toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment(new CustomShader))
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=UV.y;
      |float b=1.0;
      |vec4(123.0);
      |""".stripMargin.trim
    )

  }

  test("inline shader functions can take and use arguments") {

    inline def fragment(angle: Float): Shader[Unit, Unit] =
      Shader { _ =>
        val a_rotation: Float = angle;
      }

    val actual =
      fragment(12.0f).toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment(12.0f))
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float a_rotation=12.0;
      |""".stripMargin.trim
    )
  }

}
