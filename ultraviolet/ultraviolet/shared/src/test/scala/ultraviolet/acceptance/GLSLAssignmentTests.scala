package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
class GLSLAssignmentTests extends munit.FunSuite {

  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("Output a color / Assign") {

    inline def fragment =
      Shader[FragEnv] { env =>
        env.COLOR = vec4(1.0f, 0.0f, 0.0f, 1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |COLOR=vec4(1.0,0.0,0.0,1.0);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: =") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1.0f
        x = 1.0f
        vec4(x)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=1.0;
      |x=1.0;
      |vec4(x);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: +=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1.0f
        x += 1.0f
        vec4(x)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=1.0;
      |x=x+1.0;
      |vec4(x);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: -=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1.0f
        x -= 1.0f
        vec4(x)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=1.0;
      |x=x-1.0;
      |vec4(x);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: *=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1.0f
        x *= 1.0f
        vec4(x)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=1.0;
      |x=x*1.0;
      |vec4(x);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: /=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1.0f
        x /= 1.0f
        vec4(x)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=1.0;
      |x=x/1.0;
      |vec4(x);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: %=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1.0f
        x %= 1.0f
        vec4(x)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=1.0;
      |x=mod(x,1.0);
      |vec4(x);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: <<=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1
        x <<= 1
        vec4(1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int x=1;
      |x=x<<1;
      |vec4(1.0);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: >>=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1
        x >>= 1
        vec4(1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int x=1;
      |x=x>>1;
      |vec4(1.0);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: &=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1
        x &= 1
        vec4(1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int x=1;
      |x=x&1;
      |vec4(1.0);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: ^=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1
        x ^= 1
        vec4(1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int x=1;
      |x=x^1;
      |vec4(1.0);
      |""".stripMargin.trim
    )
  }

  test("assignment operators: |=") {

    inline def fragment =
      Shader[FragEnv] { env =>
        var x = 1
        x |= 1
        vec4(1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int x=1;
      |x=x|1;
      |vec4(1.0);
      |""".stripMargin.trim
    )
  }

}
