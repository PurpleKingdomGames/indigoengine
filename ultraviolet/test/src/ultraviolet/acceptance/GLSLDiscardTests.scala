package ultraviolet.acceptance

// import ultraviolet.DebugAST
import ultraviolet.predef.indigo.IndigoProgram
import ultraviolet.predef.shadertoy.ShaderToyEnv
import ultraviolet.predef.shadertoy.ShaderToyProgram
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
@nowarn("msg=discarded")
class GLSLDiscardTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  inline def assignProgram: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("assign"),
      Nil,
      List(ProgramTransformer.AssignFunctionReturnValueToVariable("helper", "out"))
    )

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

  test("non-void helper whose body is a discard (direct)") {
    inline def fragment =
      Shader[FragEnv, Unit] { _ =>
        def kill(): vec4 = discard()

        kill()
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |vec4 kill(){
      |  discard;
      |}
      |kill();
      |""".stripMargin.trim
    )
  }

  test("non-void helper ending in a discard after other statements") {
    inline def fragment =
      Shader[FragEnv, Unit] { _ =>
        def kill(c: vec4): vec4 =
          val a = c.a * 2.0f
          discard()

        kill(vec4(1.0f))
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |vec4 kill(in vec4 c){
      |  float a=c.a*2.0;
      |  discard;
      |}
      |kill(vec4(1.0));
      |""".stripMargin.trim
    )
  }

  test("terminal discard in an Indigo fragment function (direct)") {
    inline def fragment =
      Shader[FragEnv] { _ =>
        def fragment(): vec4 = discard()
      }

    val actual =
      fragment.toGLSL(IndigoProgram).code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void fragment(){
      |  discard;
      |}
      |""".stripMargin.trim
    )
  }

  test("terminal discard at the end of an Indigo fragment function block") {
    inline def fragment =
      Shader[FragEnv] { _ =>
        def fragment(): vec4 =
          val c = vec4(1.0f)
          discard()
      }

    val actual =
      fragment.toGLSL(IndigoProgram).code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void fragment(){
      |  vec4 c=vec4(1.0);
      |  discard;
      |}
      |""".stripMargin.trim
    )
  }

  test("terminal discard in a ShaderToy mainImage function (direct)") {
    inline def fragment =
      Shader[ShaderToyEnv, Unit] { _ =>
        def mainImage(fragColor: vec4, fragCoord: vec2): vec4 = discard()
      }

    val actual =
      fragment.toGLSL(ShaderToyProgram).code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void mainImage(out vec4 fragColor,in vec2 fragCoord){
      |  discard;
      |}
      |""".stripMargin.trim
    )
  }

  test("terminal discard at the end of a ShaderToy mainImage block") {
    inline def fragment =
      Shader[ShaderToyEnv, Unit] { _ =>
        def mainImage(fragColor: vec4, fragCoord: vec2): vec4 =
          val c = vec4(1.0f)
          discard()
      }

    val actual =
      fragment.toGLSL(ShaderToyProgram).code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void mainImage(out vec4 fragColor,in vec2 fragCoord){
      |  vec4 c=vec4(1.0);
      |  discard;
      |}
      |""".stripMargin.trim
    )
  }

  test("inferred helper whose body is a discard (direct)") {
    inline def fragment =
      Shader[FragEnv, Unit] { _ =>
        def kill() = discard()

        kill()
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void kill(){
      |  discard;
      |}
      |kill();
      |""".stripMargin.trim
    )
  }

  test("inferred helper ending in a discard after other statements") {
    inline def fragment =
      Shader[FragEnv, Unit] { _ =>
        def kill() =
          val x = 1
          discard()

        kill()
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void kill(){
      |  int x=1;
      |  discard;
      |}
      |kill();
      |""".stripMargin.trim
    )
  }

  test("terminal discard in a helper converted by AssignFunctionReturnValueToVariable (direct)") {
    inline def fragment =
      Shader[FragEnv, Unit] { _ =>
        def helper(): vec4 = discard()
      }

    val actual =
      fragment.toGLSL(assignProgram).code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |vec4 helper(){
      |  discard;
      |}
      |""".stripMargin.trim
    )
  }

  test("terminal discard in a helper converted by AssignFunctionReturnValueToVariable (block)") {
    inline def fragment =
      Shader[FragEnv, Unit] { _ =>
        def helper(): vec4 =
          val c = vec4(1.0f)
          discard()
      }

    val actual =
      fragment.toGLSL(assignProgram).code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |vec4 helper(){
      |  vec4 c=vec4(1.0);
      |  discard;
      |}
      |""".stripMargin.trim
    )
  }

}
