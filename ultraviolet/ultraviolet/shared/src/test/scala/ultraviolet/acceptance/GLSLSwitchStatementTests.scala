package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.macros.ShaderMacros
import ultraviolet.syntax.*

class GLSLSwitchStatementTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("switch statements / pattern matching") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    inline def fragment =
      Shader[FragEnv] { _ =>
        val flag: Int = 2

        var res: Int = -1

        flag match
          case 0 => res = 10
          case 1 => res = 20
          case 2 => res = 30
          case _ => res = -100

        res
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int flag=2;
      |int res=-1;
      |switch(flag){
      |  case 0:
      |    res=10;
      |    break;
      |  case 1:
      |    res=20;
      |    break;
      |  case 2:
      |    res=30;
      |    break;
      |  default:
      |    res=-100;
      |    break;
      |}
      |res;
      |""".stripMargin.trim
    )
  }

  test("pattern matching can set a val") {

    inline def fragment =
      Shader[FragEnv, Int] { _ =>
        val flag: Int = 2

        val res1: Int = flag match
          case 0 => 10
          case 1 => 20
          case 2 => 30
          case _ => -100

        val res2: Int = flag match
          case 0 => 10
          case 1 => 20
          case 2 =>
            val amount = 30
            amount + 1
          case _ => -100

        val res3: Int =
          val zeroRes = 10
          flag match
            case 0 => zeroRes
            case 1 => 20
            case 2 =>
              val amount = 30
              amount + 1
            case _ => -100

        res1 + res2 + res3
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int flag=2;
      |int res1;
      |switch(flag){
      |  case 0:
      |    res1=10;
      |    break;
      |  case 1:
      |    res1=20;
      |    break;
      |  case 2:
      |    res1=30;
      |    break;
      |  default:
      |    res1=-100;
      |    break;
      |}
      |int res2;
      |switch(flag){
      |  case 0:
      |    res2=10;
      |    break;
      |  case 1:
      |    res2=20;
      |    break;
      |  case 2:
      |    int amount=30;
      |    res2=amount+1;
      |    break;
      |  default:
      |    res2=-100;
      |    break;
      |}
      |int zeroRes=10;
      |int res3;
      |switch(flag){
      |  case 0:
      |    res3=zeroRes;
      |    break;
      |  case 1:
      |    res3=20;
      |    break;
      |  case 2:
      |    int amount=30;
      |    res3=amount+1;
      |    break;
      |  default:
      |    res3=-100;
      |    break;
      |}
      |(res1+res2)+res3;
      |""".stripMargin.trim
    )
  }

  test("pattern matching can be used to return a function") {

    inline def fragment =
      Shader[FragEnv, Int] { _ =>
        def p1(flag: Int): Int =
          flag match
            case 0 => 10
            case 1 => 20
            case 2 => 30
            case _ => -100
        p1(2)

        def p2(flag: Int): Int =
          val amount = 5 // Forces the function body to be a Block
          flag match
            case 0 => amount
            case 1 => 20
            case 2 =>
              val thirty = 30
              thirty
            case _ => -100
        p2(1)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int p1(in int flag){
      |  int val0;
      |  switch(flag){
      |    case 0:
      |      val0=10;
      |      break;
      |    case 1:
      |      val0=20;
      |      break;
      |    case 2:
      |      val0=30;
      |      break;
      |    default:
      |      val0=-100;
      |      break;
      |  }
      |  return val0;
      |}
      |p1(2);
      |int p2(in int flag){
      |  int amount=5;
      |  int val1;
      |  switch(flag){
      |    case 0:
      |      val1=amount;
      |      break;
      |    case 1:
      |      val1=20;
      |      break;
      |    case 2:
      |      int thirty=30;
      |      val1=thirty;
      |      break;
      |    default:
      |      val1=-100;
      |      break;
      |  }
      |  return val1;
      |}
      |p2(1);
      |""".stripMargin.trim
    )
  }

  test("pattern matching can be used to return a lambda function") {

    inline def fragment =
      Shader[FragEnv, Int] { _ =>
        def p1: Int => Int = flag =>
          flag match
            case 0 => 10
            case 1 => 20
            case 2 => 30
            case _ => -100
        p1(2)

        def p2: Int => Int = flag =>
          val amount = 5 // Forces the function body to be a Block
          flag match
            case 0 => amount
            case 1 => 20
            case 2 =>
              val thirty = 30
              thirty
            case _ => -100
        p2(1)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(ShaderMacros.toAST(fragment))
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int def0(in int flag){
      |  int val0;
      |  switch(flag){
      |    case 0:
      |      val0=10;
      |      break;
      |    case 1:
      |      val0=20;
      |      break;
      |    case 2:
      |      val0=30;
      |      break;
      |    default:
      |      val0=-100;
      |      break;
      |  }
      |  return val0;
      |}
      |int def1(in int flag){
      |  int amount=5;
      |  int val1;
      |  switch(flag){
      |    case 0:
      |      val1=amount;
      |      break;
      |    case 1:
      |      val1=20;
      |      break;
      |    case 2:
      |      int thirty=30;
      |      val1=thirty;
      |      break;
      |    default:
      |      val1=-100;
      |      break;
      |  }
      |  return val1;
      |}
      |def0(2);
      |def1(1);
      |""".stripMargin.trim
    )
  }

  test("Unit pattern matching works in return position of unit function") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    inline def fragment =
      Shader {
        var foo: Int = 0
        def p1(flag: Int): Unit =
          flag match
            case 0 => foo = 10
            case _ => foo = -100
        p1(2)
        def p2(flag: Int): Unit =
          var bar: Int = 0
          flag match
            case 0 => bar = 10
            case _ => bar = -100
        p2(2)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      s"""
      |int foo=0;
      |void p1(in int flag){
      |  switch(flag){
      |    case 0:
      |      foo=10;
      |      break;
      |    default:
      |      foo=-100;
      |      break;
      |  }
      |}
      |p1(2);
      |void p2(in int flag){
      |  int bar=0;
      |  switch(flag){
      |    case 0:
      |      bar=10;
      |      break;
      |    default:
      |      bar=-100;
      |      break;
      |  }
      |}
      |p2(2);
      |""".stripMargin.trim
    )
  }

}
