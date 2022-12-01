package ultraviolet.acceptance

import ultraviolet.DebugAST
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

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |const int MAX_VERTICES=16;
      |vec2[MAX_VERTICES] toUvSpace(in int count,in vec2[MAX_VERTICES] v){
      |  vec2[MAX_VERTICES] polygon;
      |  int i=0;
      |  while(i<count){
      |    polygon[i]=(v(i)/SIZE);
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

}
