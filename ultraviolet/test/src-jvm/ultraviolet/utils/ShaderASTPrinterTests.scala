package ultraviolet.utils

import ultraviolet.macros.ShaderMacros
import ultraviolet.datatypes.ProceduralShader
import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderAST.*
import ultraviolet.datatypes.Shader
import ultraviolet.datatypes.ProgramVersion
import ultraviolet.datatypes.UBOField
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class ShaderASTPrinterTests extends munit.FunSuite {

  test("renders a leaf AST node") {
    val actual = ShaderASTPrinter.print(ShaderAST.DataTypes.float(1.5f))
    assertEquals(actual, "float 1.5")
  }

  test("renders an Empty node") {
    assertEquals(ShaderASTPrinter.print(ShaderAST.Empty()), "Empty")
  }

  test("renders a nested Block of Vals with indentation") {
    val ast =
      ShaderAST.Block(
        List(
          ShaderAST.Val("x", ShaderAST.DataTypes.float(1.0f), ShaderAST.DataTypes.ident("float")),
          ShaderAST.Val("y", ShaderAST.DataTypes.int(2), ShaderAST.DataTypes.ident("int"))
        )
      )

    val expected =
      """Block
        |  Val 'x' : float
        |    float 1.0
        |  Val 'y' : int
        |    int 2""".stripMargin

    assertEquals(ShaderASTPrinter.print(ast), expected)
  }

  test("renders an Annotated node") {
    val ast =
      ShaderAST.Annotated(
        ShaderAST.DataTypes.ident("uniform"),
        ShaderAST.Empty(),
        ShaderAST.Val("u_time", ShaderAST.Empty(), ShaderAST.DataTypes.ident("float"))
      )

    val expected =
      """Annotated uniform
        |  Empty
        |  Val 'u_time' : float
        |    Empty""".stripMargin

    assertEquals(ShaderASTPrinter.print(ast), expected)
  }

  test("renders a Function with args and body") {
    val ast =
      ShaderAST.Function(
        "addOne",
        List(ShaderAST.DataTypes.ident("float") -> "x"),
        ShaderAST.Block(
          List(
            ShaderAST.Infix(
              "+",
              ShaderAST.DataTypes.ident("x"),
              ShaderAST.DataTypes.float(1.0f),
              ShaderAST.DataTypes.ident("float")
            )
          )
        ),
        ShaderAST.DataTypes.ident("float")
      )

    val expected =
      """Function 'addOne'(float x) : float
        |  Block
        |    Infix '+' : float
        |      ident 'x'
        |      float 1.0""".stripMargin

    assertEquals(ShaderASTPrinter.print(ast), expected)
  }

  test("renders a UBO with fields") {
    val ast =
      ShaderAST.UBO(
        UBODef(
          "MyUBO",
          List(
            UBOField(None, "vec4", "u_color"),
            UBOField(Some("highp"), "float", "u_time")
          )
        )
      )

    val expected =
      """UBO 'MyUBO'
        |  vec4 u_color
        |  highp float u_time""".stripMargin

    assertEquals(ShaderASTPrinter.print(ast), expected)
  }

  test("renders a ProceduralShader with all sections") {
    val proc =
      ProceduralShader(
        defs = Nil,
        ubos = Nil,
        annotations = Nil,
        main = ShaderAST.Block(
          List(ShaderAST.Val("x", ShaderAST.DataTypes.float(1.0f), ShaderAST.DataTypes.ident("float")))
        )
      )

    val expected =
      """ProceduralShader
        |  defs: (empty)
        |  ubos: (empty)
        |  annotations: (empty)
        |  main:
        |    Block
        |      Val 'x' : float
        |        float 1.0""".stripMargin

    assertEquals(ShaderASTPrinter.print(proc), expected)
  }

  test("prints AST from a real Shader via ShaderMacros.toAST") {
    inline def fragment =
      Shader {
        val h: Float = 1.0f
      }

    val proc   = ShaderMacros.toAST(fragment, true)
    val output = ShaderASTPrinter.print(proc)

    assert(output.startsWith("ProceduralShader"))
    assert(output.contains("Val 'h'"))
  }

  test("prints AST from a real Shader directly") {
    inline def fragment =
      Shader {
        val h: Float = 1.0f
      }

    val output = ShaderASTPrinter.printAST(fragment)

    assert(output.startsWith("ProceduralShader"))
    assert(output.contains("Val 'h'"))
  }

  test("prints AST from a real transformed Shader directly") {
    inline def fragment =
      Shader {
        val h: Float = 1.0f
      }

    val output = ShaderASTPrinter.printASTTransformed(fragment, ProgramVersion.GLSL_300)

    assert(output.startsWith("ProceduralShader"))
    assert(output.contains("Val 'h'"))
  }

  test("Can print and invalid AST (Example used: Nested named functions)") {
    inline def fragment =
      Shader {
        def foo(i: Int): Int =
          def bar(): Int = 10
          bar()
      }

    val output =
      ShaderASTPrinter.printAST(
        fragment,
        useValidation = false // Does not compile if set to true.
      )

    assert(clue(output).startsWith("ProceduralShader"))
    assert(clue(output).contains("Function 'foo'"))
    assert(clue(output).contains("Function 'bar'"))
  }

}
