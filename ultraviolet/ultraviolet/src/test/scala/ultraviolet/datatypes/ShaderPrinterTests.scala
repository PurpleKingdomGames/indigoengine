package ultraviolet.datatypes

class ShaderPrinterTests extends munit.FunSuite {

  test("The default printer can print an AST") {

    val printer = new ShaderPrinter.DefaultPrinter

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
      printer.print(ast)

    val expected =
      List(
        "float x=1.0;",
        "const float y=2.0;",
        "float z=3.0;"
      )

    assertEquals(actual, expected)

  }

}
