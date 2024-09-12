package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class GLSLPrimitiveTests extends munit.FunSuite {

  test("bool") {
    inline def fragment =
      Shader {
        val a = true
        val b = vec2(false.toFloat, a.toFloat)
        val c = 1 + a.toInt
        val d = 1.0f.toBoolean
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |bool a=true;
      |vec2 b=vec2(float(false),float(a));
      |int c=1+int(a);
      |bool d=bool(1.0);
      |""".stripMargin.trim
    )
  }

  test("bvec") {
    inline def fragment =
      Shader {
        val a = bvec2(true, false)
        val b = bvec3(true, false, true)
        val c = bvec4(true, false, true, false)
        a.yx
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |bvec2 a=bvec2(true,false);
      |bvec3 b=bvec3(true,false,true);
      |bvec4 c=bvec4(true,false,true,false);
      |a.yx;
      |""".stripMargin.trim
    )
  }

  test("ivec") {
    inline def fragment =
      Shader {
        val a = ivec2(0, 1)
        val b = ivec3(0, 1, 2)
        val c = ivec4(0, 1, 2, 3)
        a.yx
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |ivec2 a=ivec2(0,1);
      |ivec3 b=ivec3(0,1,2);
      |ivec4 c=ivec4(0,1,2,3);
      |a.yx;
      |""".stripMargin.trim
    )
  }

  test("vec4") {
    inline def fragment =
      Shader {
        vec4(1.0f, 1.0f, 0.0f, 1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec4(1.0,1.0,0.0,1.0);
      |""".stripMargin.trim
    )
  }

  test("Samplers") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    case class Env(var COLOR: vec4)

    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    inline def fragment =
      Shader[Env, Unit] { env =>
        @in val v_texcoord: vec2   = null
        @in val v_normal: vec3     = null
        @uniform val u_texture2d   = sampler2D
        @uniform val u_textureCube = samplerCube

        def main: Unit =
          val c: vec4 = texture2D(u_texture2d, v_texcoord);
          env.COLOR = textureCube(u_textureCube, normalize(v_normal)) * c
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |in vec2 v_texcoord;
      |in vec3 v_normal;
      |uniform sampler2D u_texture2d;
      |uniform samplerCube u_textureCube;
      |void main(){
      |  vec4 c=texture(u_texture2d,v_texcoord);
      |  COLOR=texture(u_textureCube,normalize(v_normal))*c;
      |}
      |""".stripMargin.trim
    )

  }

}
