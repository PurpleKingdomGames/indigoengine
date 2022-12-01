package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLLambdaTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("local unary lambda function (no val)") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        ((r: Float, g: Float) => vec4(r, g, 0.0f, 1.0f))(10.0f, 20.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec4 def0(in float r,in float g){
      |  return vec4(r,g,0.0,1.0);
      |}
      |def0(10.0,20.0);
      |""".stripMargin.trim
    )
  }

  test("local unary lambda function fun") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        val p = ((r: Float, g: Float) => vec4(r, g, 0.0f, 1.0f))(10.0f, 20.0f)

        val q = (((v: Float) => vec2(v)) andThen ((vv: vec2) => vec4(vv, vv)))(1.0)

        ((b: Float) => vec4(vec2(1.0), b, 0.5f))(30.0f) + p + q
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec4 def0(in float r,in float g){
      |  return vec4(r,g,0.0,1.0);
      |}
      |vec2 def1(in float v){
      |  return vec2(v);
      |}
      |vec4 def2(in vec2 vv){
      |  return vec4(vv,vv);
      |}
      |vec4 def3(in float val0){
      |  return def2(def1(val0));
      |}
      |vec4 def4(in float b){
      |  return vec4(vec2(1.0),b,0.5);
      |}
      |vec4 p=def0(10.0,20.0);
      |vec4 q=def3(1.0);
      |(def4(30.0)+p)+q;
      |""".stripMargin.trim
    )
  }

  test("local unary lambda function (val)") {
    inline def fragment: Shader[FragEnv, vec3] =
      Shader { _ =>
        val f: Float => vec3 = r => vec3(r, 0.0f, 0.0f)
        val g                = (b: Float) => vec3(0.0f, 0.0f, b)
        f(1.0f) + g(2.0)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec3 def0(in float r){
      |  return vec3(r,0.0,0.0);
      |}
      |vec3 def1(in float b){
      |  return vec3(0.0,0.0,b);
      |}
      |def0(1.0)+def1(2.0);
      |""".stripMargin.trim
    )
  }

  test("local unary lambda function (def)") {
    inline def fragment: Shader[FragEnv, vec3] =
      Shader { _ =>
        def f: Float => vec3 = r => vec3(r, 0.0f, 0.0f)
        def g                = (b: Float) => vec3(0.0f, 0.0f, b)
        f(1.0f) + g(2.0)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec3 def0(in float r){
      |  return vec3(r,0.0,0.0);
      |}
      |vec3 def1(in float b){
      |  return vec3(0.0,0.0,b);
      |}
      |def0(1.0)+def1(2.0);
      |""".stripMargin.trim
    )
  }

  test("compose (Function1)") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        val e: Float => Float = v => v - 0.5f
        val f: Float => vec3  = r => vec3(r, 0.0f, 0.0f)
        val g: vec3 => vec4   = val3 => vec4(val3, 0.5f)
        val h: Float => vec4  = g compose f compose e

        h(1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float def0(in float v){
      |  return v-0.5;
      |}
      |vec3 def1(in float r){
      |  return vec3(r,0.0,0.0);
      |}
      |vec4 def2(in vec3 val3){
      |  return vec4(val3,0.5);
      |}
      |vec4 def3(in float val0){
      |  return def2(def1(val0));
      |}
      |vec4 def4(in float val1){
      |  return def3(def0(val1));
      |}
      |def4(1.0);
      |""".stripMargin.trim
    )
  }

  test("andThen (Function1)") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        val e: Float => Float = v => v - 0.5f
        val f: Float => vec3  = r => vec3(r, 0.0f, 0.0f)
        val g: vec3 => vec4   = val3 => vec4(val3, 0.5f)
        val h: Float => vec4  = f andThen g
        val i: Float => vec4  = e andThen h

        i(1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float def0(in float v){
      |  return v-0.5;
      |}
      |vec3 def1(in float r){
      |  return vec3(r,0.0,0.0);
      |}
      |vec4 def2(in vec3 val3){
      |  return vec4(val3,0.5);
      |}
      |vec4 def3(in float val0){
      |  return def2(def1(val0));
      |}
      |vec4 def4(in float val1){
      |  return def3(def0(val1));
      |}
      |def4(1.0);
      |""".stripMargin.trim
    )
  }

  test("andThen (Function1 external)") {

    inline def e: Float => Float = r => r - 0.5f
    inline def f: Float => vec3  = r => vec3(r, 0.0f, 0.0f)
    inline def g: vec3 => vec4   = v3 => vec4(v3, 0.5f)
    inline def h: Float => vec4  = e andThen f andThen g

    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        h(1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float def0(in float r){
      |  return r-0.5;
      |}
      |vec3 def1(in float r){
      |  return vec3(r,0.0,0.0);
      |}
      |vec3 def2(in float val0){
      |  return def1(def0(val0));
      |}
      |vec4 def3(in vec3 v3){
      |  return vec4(v3,0.5);
      |}
      |vec4 def4(in float val1){
      |  return def3(def2(val1));
      |}
      |def4(1.0);
      |""".stripMargin.trim
    )
  }

}
