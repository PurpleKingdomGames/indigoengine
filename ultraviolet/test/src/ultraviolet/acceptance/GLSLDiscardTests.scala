package ultraviolet.acceptance

// import ultraviolet.DebugAST
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class GLSLDiscardTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("discard in the fragment function") {
    inline def fragment =
      Shader[FragEnv, Unit] { _ =>
        discard()
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |discard;
      |""".stripMargin.trim
    )
  }

  test("discard in one branch of an if statement inside a called function") {
    inline def fragment =
      Shader[FragEnv, vec4] { env =>
        def pick(c: vec4): vec4 =
          if c.a < 0.5f then discard()
          else c

        pick(env.COLOR)
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |vec4 pick(in vec4 c){
      |  vec4 val0;
      |  if(c.a<0.5){
      |    discard;
      |  }else{
      |    val0=c;
      |  }
      |  return val0;
      |}
      |pick(COLOR);
      |""".stripMargin.trim
    )
  }

}
