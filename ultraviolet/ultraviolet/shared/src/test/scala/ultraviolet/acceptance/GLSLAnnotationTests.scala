package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLAnnotationTests extends munit.FunSuite {

  test("Annotated variables render correctly") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
    inline def fragment =
      Shader {
        // @attribute var a: Float = 0.0f // Scala doesn't allow for primitives. // WebGL 1.0 only
        @const var b: vec2 = null
        // @in var c: vec3         = null // WebGL 2.0 only
        // @out var d: vec4        = null // WebGL 2.0 only
        @uniform var e: Float = 0.0f
        // @varying var f: Float   = 0.0f // WebGL 1.0 only
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |const vec2 b;
      |uniform float e;
      |""".stripMargin.trim
    )

  }

  test("layout locations") {

    inline def fragment =
      Shader { _ =>
        @layout(7) @in val a_rotation: Float = 0.0;
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |layout (location = 7) in float a_rotation;
      |""".stripMargin.trim
    )
  }

  test("Can have multiple annotations") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
    inline def fragment =
      Shader {
        @flat @in var a: vec2    = null
        @smooth @out val b: vec4 = null
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |flat in vec2 a;
      |smooth out vec4 b;
      |""".stripMargin.trim
    )

  }

  test("Variables annotated as global render as normal, but before defs") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
    inline def fragment =
      Shader {
        val addOne = (i: Int) => i + 1

        @global var b: vec2 = null

        val subtract1One = (i: Int) => i - 1

        addOne(subtract1One(2))
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 b;
      |int def0(in int i){
      |  return i+1;
      |}
      |int def1(in int i){
      |  return i-1;
      |}
      |def0(def1(2));
      |""".stripMargin.trim
    )

  }

}
