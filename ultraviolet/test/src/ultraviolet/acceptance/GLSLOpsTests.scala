package ultraviolet.acceptance

// import ultraviolet.DebugAST
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class GLSLOpsTests extends munit.FunSuite {

  test("Will convert % to mod() for Float types") {

    inline def fragment =
      Shader {
        def main: Unit =
          val x = mod(10.0f, 2.0f)
          val y = 2.0f
          val z = 10.0f % y
          val w = 10.0f % 3.0f
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void main(){
      |  float x=mod(10.0,2.0);
      |  float y=2.0;
      |  float z=mod(10.0,y);
      |  float w=1.0;
      |}
      |""".stripMargin.trim
    )
  }

  test("Will retain % for Int types") {

    inline def fragment =
      Shader {
        def main: Unit =
          val i: Int = 10
          val x: Int = i % 3
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void main(){
      |  int i=10;
      |  int x=i%3;
      |}
      |""".stripMargin.trim
    )
  }

  test("Will retain % for Int variables") {

    inline def fragment =
      Shader {
        def main: Unit =
          val a      = 7
          val b      = 3
          val result = a % b
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void main(){
      |  int a=7;
      |  int b=3;
      |  int result=a%b;
      |}
      |""".stripMargin.trim
    )
  }

  test("Will retain % when only one side is an Int variable") {

    inline def fragment =
      Shader {
        def main: Unit =
          val a = 7
          val b = 3
          val x = a % 3
          val y = 7 % b
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void main(){
      |  int a=7;
      |  int b=3;
      |  int x=a%3;
      |  int y=7%b;
      |}
      |""".stripMargin.trim
    )
  }

  test("Will retain % for Int function arguments") {

    inline def fragment =
      Shader {
        def wrap(a: Int, b: Int): Int =
          a % b
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |int wrap(in int a,in int b){
      |  return a%b;
      |}
      |""".stripMargin.trim
    )
  }

  test("Will retain % for Int array elements") {

    inline def fragment =
      Shader {
        def main: Unit =
          val arr: array[4, Int] = array[4, Int](7, 3, 2, 1)
          val result             = arr(0) % arr(1)
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void main(){
      |  int arr[4]=int[4](7,3,2,1);
      |  int result=arr[0]%arr[1];
      |}
      |""".stripMargin.trim
    )
  }

  test("Will retain % for Int types in %= assignments") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    inline def fragment =
      Shader {
        def main: Unit =
          val j = 3
          var i = 10
          i %= j
          val k = i
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void main(){
      |  int j=3;
      |  int i=10;
      |  i=i%j;
      |  int k=i;
      |}
      |""".stripMargin.trim
    )
  }

  test("Will convert % to mod() for Float variables") {

    inline def fragment =
      Shader {
        def main: Unit =
          val x = 10.0f
          val y = 2.0f
          val z = x % y
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void main(){
      |  float x=10.0;
      |  float y=2.0;
      |  float z=mod(x,y);
      |}
      |""".stripMargin.trim
    )
  }

  test("GLSL 100 expands integer % to a-(b*(a/b))") {

    inline def fragment =
      Shader {
        def main: Unit =
          val a      = 7
          val b      = 3
          val result = a % b
      }

    val actual =
      fragment.toGLSL100.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void main(){
      |  int a=7;
      |  int b=3;
      |  int result=a-(b*(a/b));
      |}
      |""".stripMargin.trim
    )
  }

  test("Mixing Int and Float operands is a compile error") {

    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader {
      def main: Unit =
        val a = 7
        val b = 2.0f
        val c = a % b
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("Shaders do not support mixing integer and floating point types"), errors)
    assert(errors.contains("'int % float'"), errors)
  }

  test("Mixing Int and Float operands in math operators is a compile error") {

    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader {
      def main: Unit =
        val a = 2.0f
        val b = 7
        val c = a * b
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("Shaders do not support mixing integer and floating point types"), errors)
    assert(errors.contains("'float * int'"), errors)
  }

  test("clamp vec3 will accept float gentypes") {

    inline def fragment =
      Shader {
        def main: Unit =
          val x = clamp(vec4(1.0f), vec4(0.0f), vec4(1.0f))
          val y = clamp(vec4(1.0f), 0.0f, 1.0f)
      }

    val actual =
      fragment.toGLSL300.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertNoDiff(
      actual,
      s"""
      |void main(){
      |  vec4 x=clamp(vec4(1.0),vec4(0.0),vec4(1.0));
      |  vec4 y=clamp(vec4(1.0),0.0,1.0);
      |}
      |""".stripMargin.trim
    )
  }

}
