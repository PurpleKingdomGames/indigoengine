package ultraviolet.macros

import ultraviolet.DebugAST
import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderError

class ShaderProgramValidationTests extends munit.FunSuite {

  val errorPrefix: String = "[ultraviolet] "

  test("functions must not contain forward references") {
    val ast =
      ShaderAST.Function(
        "def0",
        List((ShaderAST.DataTypes.ident("float"), "r")),
        ShaderAST.Block(
          List(
            ShaderAST.Val("g", ShaderAST.DataTypes.float(0.5f), ShaderAST.DataTypes.ident("float")),
            ShaderAST.DataTypes.vec4(
              List(
                ShaderAST.DataTypes.ident("r"),
                ShaderAST.DataTypes.ident("g"),
                ShaderAST.DataTypes.ident("b"), // Forward reference, because it doesn't exist in the known refs
                ShaderAST.DataTypes.float(1.0)
              )
            )
          )
        ),
        ShaderAST.DataTypes.ident("vec4")
      )

    interceptMessage[ShaderError.Validation](errorPrefix + "b is an illegal forward reference.") {
      ShaderProgramValidation.validate(0, Nil)(ast)
    }
  }

  test("Functions should not contain other nested functions") {
    val ast =
      ShaderAST.Function(
        "foo",
        List((ShaderAST.DataTypes.ident("float"), "r")),
        ShaderAST.Block(
          List(
            ShaderAST.Function(
              "bar",
              List((ShaderAST.DataTypes.ident("float"), "g")),
              ShaderAST.Block(
                List(
                  ShaderAST.DataTypes.ident("g")
                )
              ),
              ShaderAST.DataTypes.ident("vec4")
            )
          )
        ),
        ShaderAST.DataTypes.ident("vec4")
      )

    interceptMessage[ShaderError.Validation](errorPrefix + ShaderProgramValidation.ErrorMsgNestedFunction) {
      ShaderProgramValidation.validate(0, Nil)(ast)
    }
  }

  test("Global variables much be null or constant values, not function calls") {
    val ast =
      ShaderAST.ShaderBlock(
        None,
        None,
        None,
        List(
          ShaderAST.Val(
            "foo",
            ShaderAST.CallFunction("makeVec2", Nil, ShaderAST.DataTypes.ident("vec2")),
            ShaderAST.DataTypes.ident("vec2")
          )
        )
      )

    interceptMessage[ShaderError.Validation](
      errorPrefix + "foo is a top level variable, and so must be a constant value or null."
    ) {
      ShaderProgramValidation.validate(0, Nil)(ast)
    }
  }

  test("Function calls cannot be forward references") {
    val ast =
      ShaderAST.ShaderBlock(
        None,
        None,
        None,
        List(
          ShaderAST.CallFunction("makeVec2", Nil, ShaderAST.DataTypes.ident("vec2")),
          ShaderAST.Function(
            "makeVec2",
            Nil,
            ShaderAST.DataTypes.vec2(List(ShaderAST.DataTypes.float(1.0f))),
            ShaderAST.DataTypes.ident("vec2")
          )
        )
      )

    interceptMessage[ShaderError.Validation](errorPrefix + "makeVec2 is an illegal forward reference.") {
      ShaderProgramValidation.validate(0, Nil)(ast)
    }
  }

  test("Variables cannot be forward references") {
    val ast =
      ShaderAST.ShaderBlock(
        None,
        None,
        None,
        List(
          ShaderAST.Function(
            "makeVec2",
            Nil,
            ShaderAST.DataTypes.ident("foo"),
            ShaderAST.DataTypes.ident("vec2")
          ),
          ShaderAST.Val(
            "foo",
            ShaderAST.DataTypes.vec2(List(ShaderAST.DataTypes.float(1.0f))),
            ShaderAST.DataTypes.ident("vec2")
          )
        )
      )

    interceptMessage[ShaderError.Validation](errorPrefix + "foo is an illegal forward reference.") {
      ShaderProgramValidation.validate(0, Nil)(ast)
    }
  }

  test("Variables can reference previous variables") {
    val ast: ShaderAST =
      ShaderAST.ShaderBlock(
        None,
        None,
        None,
        List(
          ShaderAST.Val(
            "foo",
            ShaderAST.DataTypes.int(0),
            ShaderAST.DataTypes.ident("int")
          ),
          ShaderAST.Val(
            "bar",
            ShaderAST.DataTypes.ident("foo"),
            ShaderAST.DataTypes.ident("int")
          )
        )
      )

    val result = ShaderProgramValidation.validate(0, Nil)(ast)

    assertEquals(ast, result)
  }

  test("validate a block of statements") {

    val statements =
      List(
        ShaderAST.Val(
          "foo",
          ShaderAST.DataTypes.int(0),
          ShaderAST.DataTypes.ident("int")
        ),
        ShaderAST.Val(
          "bar",
          ShaderAST.DataTypes.ident("foo"),
          ShaderAST.DataTypes.ident("int")
        )
      )

    val actual =
      ShaderProgramValidation.validateStatementBlock(statements, 1, Nil)

    assertEquals(actual, statements)

  }

}
