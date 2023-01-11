package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLScopingTests extends munit.FunSuite {

  test("scoping is respected") {
    inline def fragment: Shader[Unit, vec3] =
      Shader { _ =>
        def foo: vec3 =
          val f: Float => vec3 = r => vec3(r, 0.0f, 0.0f)
          f(1.0)

        def bar: vec3 =
          val f: vec2 => vec3 = rg => vec3(rg, 0.0f)
          f(vec2(0.5))
        foo + bar
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec3 def0(in float r){
      |  return vec3(r,0.0,0.0);
      |}
      |vec3 def1(in vec2 rg){
      |  return vec3(rg,0.0);
      |}
      |vec3 foo(){
      |  return def0(1.0);
      |}
      |vec3 bar(){
      |  return def1(vec2(0.5));
      |}
      |foo()+bar();
      |""".stripMargin.trim
    )
  }

}
