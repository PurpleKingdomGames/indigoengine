package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLIfStatementTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("if else statements") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        val red    = vec4(1.0, 0.0, 0.0, 1.0)
        val green  = vec4(0.0, 1.0, 0.0, 1.0)
        val blue   = vec4(0.0, 0.0, 1.0, 1.0)
        val x: Int = 1

        if x <= 0 then red
        else if x == 1 then blue
        else green
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec4 red=vec4(1.0,0.0,0.0,1.0);
      |vec4 green=vec4(0.0,1.0,0.0,1.0);
      |vec4 blue=vec4(0.0,0.0,1.0,1.0);
      |int x=1;
      |if(x<=0){
      |  red;
      |}else{
      |  if(x==1){
      |    blue;
      |  }else{
      |    green;
      |  }
      |}
      |""".stripMargin.trim
    )
  }

  test("if (no else) statements") {
    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    inline def fragment =
      Shader[FragEnv] { _ =>
        var x: Int = 1
        if x <= 10 then x = 15
        x
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int x=1;
      |if(x<=10){
      |  x=15;
      |}
      |x;
      |""".stripMargin.trim
    )
  }

  test("if statements can be used to set vals") {
    inline def fragment =
      Shader[FragEnv, Int] { _ =>
        val x: Int = 1
        val y      = if x <= 10 then 15 else 20
        val z =
          val amount = 15
          if x <= 10 then amount else 20
        y + z
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int x=1;
      |int y;
      |if(x<=10){
      |  y=15;
      |}else{
      |  y=20;
      |}
      |int amount=15;
      |int z;
      |if(x<=10){
      |  z=amount;
      |}else{
      |  z=20;
      |}
      |y+z;
      |""".stripMargin.trim
    )
  }

  test("if statements can be used to return functions") {
    inline def fragment =
      Shader[FragEnv, Int] { _ =>
        def p1(x: Int): Int =
          if x <= 10 then 15 else 20
        p1(1)

        def p2(x: Int): Int =
          val amount = 10 // Forces a Block in the function body.
          if x <= amount then
            val y = 15
            y
          else 20
        p2(1)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int p1(in int x){
      |  int val0;
      |  if(x<=10){
      |    val0=15;
      |  }else{
      |    val0=20;
      |  }
      |  return val0;
      |}
      |p1(1);
      |int p2(in int x){
      |  int amount=10;
      |  int val1;
      |  if(x<=amount){
      |    int y=15;
      |    val1=y;
      |  }else{
      |    val1=20;
      |  }
      |  return val1;
      |}
      |p2(1);
      |""".stripMargin.trim
    )
  }

  test("Unit if statements at the end of unit functions") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    inline def fragment =
      Shader {
        def p1(x: Int): Unit =
          var foo: Int = 0
          if x <= 10 then foo = 15
        p1(5)
        var bar: Int = 0
        def p2(x: Int): Unit =
          if x <= 10 then bar = 15
        p2(5)
      }

    val actual =
      fragment.toGLSL[WebGL2].code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |void p1(in int x){
      |  int foo=0;
      |  if(x<=10){
      |    foo=15;
      |  }
      |}
      |p1(5);
      |int bar=0;
      |void p2(in int x){
      |  if(x<=10){
      |    bar=15;
      |  }
      |}
      |p2(5);
      |""".stripMargin.trim
    )
  }

}
