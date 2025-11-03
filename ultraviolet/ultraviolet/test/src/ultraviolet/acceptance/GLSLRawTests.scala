package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class GLSLRawTests extends munit.FunSuite {

  test("can embed raw GLSL") {
    inline def fragment: Shader[Unit, RawGLSL] =
      Shader { _ =>
        RawGLSL("float v = 1.0;")
        raw("COLOR = vec4(v, v, v, 0.5);")
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float v = 1.0;
      |COLOR = vec4(v, v, v, 0.5);
      |""".stripMargin.trim
    )
  }

  test("can embed raw Indigo vertex GLSL") {
    inline def toEmbed: Shader[Unit, Unit] =
      Shader {
        RawGLSL(
          """
//#vertex_start
vec4 vertex(vec4 v){
  return v;
}
//#vertex_end
          """
        )
      }

    inline def fragment(inline embed: Shader[Unit, Unit]) =
      Shader {
        embed.run(())
      }

    val actual =
      fragment(toEmbed).toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |//#vertex_start
      |vec4 vertex(vec4 v){
      |  return v;
      |}
      |//#vertex_end
      |""".stripMargin.trim
    )
  }

}
