package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.macros.ShaderMacros
import ultraviolet.syntax.*

class GLSLArrayTests extends munit.FunSuite {

  test("arrays - initialise and check length") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    inline def fragment =
      Shader {
        val x: array[12, Float] = null
        val y                   = x.length
        y
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float[12] x;
      |int y=x.length();
      |y;
      |""".stripMargin.trim
    )
  }

  test("arrays - component access") {

    case class Env(VERTICES: array[16, vec2])

    inline def fragment =
      Shader[Env] { env =>
        val foo                  = env.VERTICES(2)
        val arr: array[4, Float] = array[4, Float](0.0f, 2.0f, 3.0f, 4.0f)
        val bar                  = arr(1)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 foo=VERTICES[2];
      |float arr[4]=float[4](0.0,2.0,3.0,4.0);
      |float bar=arr[1];
      |""".stripMargin.trim
    )
  }

  test("arrays - as return type") {

    case class Env(VERTICES: array[16, vec2])

    inline def fragment =
      Shader[Env] { env =>
        def func(): array[16, vec2] = env.VERTICES
        val foo                     = func()
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2[16] func(){
      |  return VERTICES;
      |}
      |vec2[16] foo=func();
      |""".stripMargin.trim
    )
  }

  test("arrays - more complicated example") {

    case class Env(
        VERTICES: array[16, vec2],
        COUNT: Float,
        SIZE: vec2
    )

    @SuppressWarnings(
      Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while")
    )
    inline def fragment =
      Shader[Env, Unit] { env =>
        @const val MAX_VERTICES: 16 = 16

        def toUvSpace(count: Int, v: array[MAX_VERTICES.type, vec2]): array[MAX_VERTICES.type, vec2] =
          val polygon: array[MAX_VERTICES.type, vec2] = null

          var i = 0
          while i < count do
            polygon(i) = v(i) / env.SIZE;
            i += 1

          polygon

        val iCount: Int                             = env.COUNT.toInt;
        val polygon: array[MAX_VERTICES.type, vec2] = toUvSpace(iCount, env.VERTICES);
      }

    // DebugAST.toAST(fragment)
    // println(actual)

    val actual =
      fragment.toGLSL[WebGL2].code

    assertEquals(
      actual,
      s"""
      |const int MAX_VERTICES=16;
      |vec2[MAX_VERTICES] toUvSpace(in int count,in vec2[MAX_VERTICES] v){
      |  vec2[MAX_VERTICES] polygon;
      |  int i=0;
      |  while(i<count){
      |    polygon[i]=(v[i]/SIZE);
      |    i=i+1;
      |  }
      |  return polygon;
      |}
      |int iCount=int(COUNT);
      |vec2[MAX_VERTICES] polygon=toUvSpace(iCount,VERTICES);
      |""".stripMargin.trim
    )
  }

  test("arrays - constructors") {

    inline def fragment =
      Shader {
        @const val foo = array[3, Float](2.5f, 7.0f, 1.5f)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |const float foo[3]=float[3](2.5,7.0,1.5);
      |""".stripMargin.trim
    )
  }

  test("arrays - component access") {

    inline def fragment =
      Shader {
        @const val foo = array[3, vec2](vec2(2.5f), vec2(7.0f), vec2(1.5f))
        foo(1).y
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |const vec2 foo[3]=vec2[3](vec2(2.5),vec2(7.0),vec2(1.5));
      |foo[1].y;
      |""".stripMargin.trim
    )
  }

  test("arrays - component access from function argument") {

    inline def fragment =
      Shader {
        val i = 0
        def func(v: array[3, vec2]): Float =
          v(i).y
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int i=0;
      |float func(in vec2[3] v){
      |  return v[i].y;
      |}
      |""".stripMargin.trim
    )
  }

  test("arrays - component access from array in env") {

    case class Env(v: array[3, vec2])

    inline def fragment =
      Shader[Env] { env =>
        val i = 0
        env.v(i).y
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int i=0;
      |v[i].y;
      |""".stripMargin.trim
    )
  }

  test("arrays - swizzle") {

    inline def fragment =
      Shader {
        @const val foo = array[1, vec4](vec4(1.0f))
        foo(0).xyz
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |const vec4 foo[1]=vec4[1](vec4(1.0));
      |foo[0].xyz;
      |""".stripMargin.trim
    )
  }

}
