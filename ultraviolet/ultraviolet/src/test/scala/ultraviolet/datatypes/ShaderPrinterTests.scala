package ultraviolet.datatypes

class ShaderPrinterTests extends munit.FunSuite {

  test("The default printer can print an AST") {

    val ast =
      ShaderAST.Block(
        List(
          ShaderAST.Val("x", ShaderAST.DataTypes.float(1.0), Option("float")),
          ShaderAST.Block(
            List(
              ShaderAST.Annotated(
                ShaderAST.DataTypes.ident("const"),
                ShaderAST.Empty(),
                ShaderAST.Val("y", ShaderAST.DataTypes.float(2.0), Option("float"))
              ),
              ShaderAST.Val("z", ShaderAST.DataTypes.float(3.0), Option("float"))
            )
          )
        )
      )

    val actual =
      ShaderPrinter.print(ast)

    val expected =
      List(
        "float x=1.0;",
        "const float y=2.0;",
        "float z=3.0;"
      )

    assertEquals(actual, expected)

  }

  test("A custom printer can partially match to modify the output of the print") {

    given ShaderPrinter = new ShaderPrinter {
      def transformer: PartialFunction[ShaderAST, ShaderAST] = {
        case ShaderAST.Val("x", ShaderAST.DataTypes.float(1.0), Some("float")) =>
          ShaderAST.Val("xx", ShaderAST.DataTypes.float(100.0), Some("float"))
      }

      def printer: PartialFunction[ShaderAST, List[String]] = {
        case ShaderAST.Val("y", ShaderAST.DataTypes.float(2.0), Some("float")) =>
          List("float foo")
      }
    }

    val ast =
      ShaderAST.Block(
        List(
          ShaderAST.Val("x", ShaderAST.DataTypes.float(1.0), Option("float")),
          ShaderAST.Block(
            List(
              ShaderAST.Annotated(
                ShaderAST.DataTypes.ident("const"),
                ShaderAST.Empty(),
                ShaderAST.Val("y", ShaderAST.DataTypes.float(2.0), Option("float"))
              ),
              ShaderAST.Val("z", ShaderAST.DataTypes.float(3.0), Option("float"))
            )
          )
        )
      )

    val actual =
      ShaderPrinter.print(ast)

    val expected =
      List(
        "float xx=100.0;",
        "const float foo;",
        "float z=3.0;"
      )

    assertEquals(actual, expected)

  }

}
