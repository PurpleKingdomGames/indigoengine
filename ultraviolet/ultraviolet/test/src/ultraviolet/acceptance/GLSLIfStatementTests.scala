package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
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
        else {
          val y = 10
          if x == 1 && y == 10 then blue
          else green
        }
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

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
      |  int y=10;
      |  if((x==1)&&(y==10)){
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
      fragment.toGLSL[WebGL2].toOutput.code

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
      fragment.toGLSL[WebGL2].toOutput.code

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
      fragment.toGLSL[WebGL2].toOutput.code

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
      fragment.toGLSL[WebGL2].toOutput.code

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

  test("if-else-if-else that sets a value") {

    inline def fragment =
      Shader {
        val amount = 0.0f

        def main(): Unit =
          val checkedAmount = abs(clamp(amount, 0.0f, 3.0f)).toInt

          val borderAmount: Int =
            if checkedAmount == 1 then 10
            else if checkedAmount == 2 then 20
            else if checkedAmount == 3 then 30
            else 40
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float amount=0.0;
      |void main(){
      |  int checkedAmount=int(abs(clamp(amount,0.0,3.0)));
      |  int borderAmount;
      |  if(checkedAmount==1){
      |    borderAmount=10;
      |  }else if(checkedAmount==2){
      |    borderAmount=20;
      |  }else if(checkedAmount==3){
      |    borderAmount=30;
      |  }else{
      |    borderAmount=40;
      |  }
      |}
      |""".stripMargin.trim
    )
  }

  test("if-else-if-else that returns a value") {

    inline def fragment =
      Shader {
        val amount = 0.0f

        def foo(checkedAmount: Int): Int =
          if checkedAmount == 1 then 10
          else if checkedAmount == 2 then 20
          else if checkedAmount == 3 then 30
          else 40

        foo(abs(clamp(amount, 0.0f, 3.0f)).toInt)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float amount=0.0;
      |int foo(in int checkedAmount){
      |  int val0;
      |  if(checkedAmount==1){
      |    val0=10;
      |  }else if(checkedAmount==2){
      |    val0=20;
      |  }else if(checkedAmount==3){
      |    val0=30;
      |  }else{
      |    val0=40;
      |  }
      |  return val0;
      |}
      |foo(int(abs(clamp(amount,0.0,3.0))));
      |""".stripMargin.trim
    )
  }

  test("nested if else statements can return") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        val red    = vec4(1.0, 0.0, 0.0, 1.0)
        val green  = vec4(0.0, 1.0, 0.0, 1.0)
        val blue   = vec4(0.0, 0.0, 1.0, 1.0)
        val x: Int = 1

        val color =
          if x <= 0 then red
          else {
            val y = 10
            if x == 1 && y == 10 then blue
            else green
          }
        color
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec4 red=vec4(1.0,0.0,0.0,1.0);
      |vec4 green=vec4(0.0,1.0,0.0,1.0);
      |vec4 blue=vec4(0.0,0.0,1.0,1.0);
      |int x=1;
      |vec4 color;
      |if(x<=0){
      |  color=red;
      |}else{
      |  int y=10;
      |  if((x==1)&&(y==10)){
      |    color=blue;
      |  }else{
      |    color=green;
      |  }
      |}
      |color;
      |""".stripMargin.trim
    )
  }

  test("if statements with !") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        val p = true

        if !p then vec4(1.0)
        else vec4(2.0)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |bool p=true;
      |if(!p){
      |  vec4(1.0);
      |}else{
      |  vec4(2.0);
      |}
      |""".stripMargin.trim
    )
  }

  test("if statement with not operator comparison") {
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        val x = 1.0f
        if x != 2.0f then vec4(10.0f) else vec4(20.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |float x=1.0;
      |if(x!=2.0){
      |  vec4(10.0);
      |}else{
      |  vec4(20.0);
      |}
      |""".stripMargin.trim
    )
  }

  test("If statements can return and assign to var's") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
    inline def fragment: Shader[FragEnv, vec4] =
      Shader { _ =>
        val x0       = vec2(1.0f, 2.0f)
        var i1: vec2 = null
        i1 = if x0.x > x0.y then vec2(1.0, 0.0) else vec2(0.0, 1.0)

        vec4(i1, 0.0f, 1.0f)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |vec2 x0=vec2(1.0,2.0);
      |vec2 i1;
      |if(x0.x>x0.y){
      |  i1=vec2(1.0,0.0);
      |}else{
      |  i1=vec2(0.0,1.0);
      |}
      |vec4(i1,0.0,1.0);
      |""".stripMargin.trim
    )
  }

}
