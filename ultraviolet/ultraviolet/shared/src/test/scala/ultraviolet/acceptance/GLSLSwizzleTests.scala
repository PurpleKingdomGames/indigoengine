package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLSwizzleTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4, SRC_CHANNEL: sampler2D.type)

  test("swizzling") {
    inline def fragment1: Shader[FragEnv, vec4] =
      Shader { _ =>
        vec4(1.0f, 2.0f, 3.0f, 4.0f).wzyx
      }

    val actual1 =
      fragment1.toGLSL[WebGL2].code

    assertEquals(
      actual1,
      s"""
      |vec4(1.0,2.0,3.0,4.0).wzyx;
      |""".stripMargin.trim
    )

    inline def fragment2: Shader[FragEnv, vec3] =
      Shader { _ =>
        vec3(1.0f, 2.0f, 3.0f).xxy
      }

    val actual2 =
      fragment2.toGLSL[WebGL2].code

    assertEquals(
      actual2,
      s"""
      |vec3(1.0,2.0,3.0).xxy;
      |""".stripMargin.trim
    )

    inline def fragment3: Shader[FragEnv, vec3] =
      Shader { _ =>
        val fill = vec3(1.0f, 2.0f, 3.0f)
        fill.xyz
      }

    val actual3 =
      fragment3.toGLSL[WebGL2].code

    assertEquals(
      actual3,
      s"""
      |vec3 fill=vec3(1.0,2.0,3.0);
      |fill.xyz;
      |""".stripMargin.trim
    )
  }

  test("swizzling (rgba)") {
    inline def fragment1: Shader[FragEnv, vec4] =
      Shader { _ =>
        vec4(1.0f, 2.0f, 3.0f, 4.0f).abgr
      }

    val actual1 =
      fragment1.toGLSL[WebGL2].code

    assertEquals(
      actual1,
      s"""
      |vec4(1.0,2.0,3.0,4.0).abgr;
      |""".stripMargin.trim
    )

    inline def fragment2: Shader[FragEnv, vec3] =
      Shader { _ =>
        vec3(1.0f, 2.0f, 3.0f).rrg
      }

    val actual2 =
      fragment2.toGLSL[WebGL2].code

    assertEquals(
      actual2,
      s"""
      |vec3(1.0,2.0,3.0).rrg;
      |""".stripMargin.trim
    )

    inline def fragment3: Shader[FragEnv, vec3] =
      Shader { _ =>
        val fill = vec3(1.0f, 2.0f, 3.0f)
        fill.rgb
      }

    val actual3 =
      fragment3.toGLSL[WebGL2].code

    assertEquals(
      actual3,
      s"""
      |vec3 fill=vec3(1.0,2.0,3.0);
      |fill.rgb;
      |""".stripMargin.trim
    )
  }

  test("swizzling an env var") {
    inline def fragment: Shader[FragEnv, vec3] =
      Shader { env =>
        env.COLOR.xyz
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    assertEquals(
      actual,
      s"""
      |COLOR.xyz;
      |""".stripMargin.trim
    )
  }

  test("Brackets can be swizzled") {
    case class Env(ROTATION: Float)

    inline def fragment: Shader[Env, Unit] =
      Shader { env =>
        // format: off
        def rotationZ(angle: Float): mat4 =
          mat4(cos(angle),  -sin(angle), 0, 0,
               sin(angle),  cos(angle),  0, 0,
               0,           0,           1, 0,
               0,           0,           0, 1)

        val normal: vec3 = vec3(1.0)
        val rotatedNormal: vec3 = (vec4(normal, 1.0f) * rotationZ(env.ROTATION)).xyz
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |mat4 rotationZ(in float angle){
      |  return mat4(cos(angle),-sin(angle),0.0,0.0,sin(angle),cos(angle),0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0);
      |}
      |vec3 normal=vec3(1.0);
      |vec3 rotatedNormal=(vec4(normal,1.0)*rotationZ(ROTATION)).xyz;
      |""".stripMargin.trim
    )
  }

  test("swizzling a function call") {
    inline def fragment: Shader[FragEnv, Float] =
      Shader { env =>
        def foo(): vec4 =
          vec4(1.0)
        foo().xyz
        foo().w

        texture2D(env.SRC_CHANNEL, vec2(0.0f, 1.0f)).xyz
        texture2D(env.SRC_CHANNEL, vec2(0.0f, 1.0f)).w
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec4 foo(){
      |  return vec4(1.0);
      |}
      |foo().xyz;
      |foo().w;
      |texture(SRC_CHANNEL,vec2(0.0,1.0)).xyz;
      |texture(SRC_CHANNEL,vec2(0.0,1.0)).w;
      |""".stripMargin.trim
    )
  }

}
