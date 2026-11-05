package ultraviolet.datatypes

// import ultraviolet.DebugAST
import ultraviolet.macros.ShaderMacros
import ultraviolet.syntax.*

import scala.annotation.nowarn
import scala.collection.mutable.ListBuffer

@nowarn("msg=unused")
class ShaderASTTests extends munit.FunSuite {

  test("traverse - can find all the val's") {
    inline def fragment =
      Shader {
        val h = vec2(10)
        val i = 10
      }

    val acc = new ListBuffer[String]()

    val ast = ShaderMacros.toAST(fragment, true).main

    ast.traverse { case v @ ShaderAST.Val(id, _, _) =>
      acc += id
      v
    }

    assertEquals(acc.length, 2)
    assertEquals(acc.toList, List("h", "i"))
  }

  test("Discard - findAll and traverse reach nested discards") {
    val ast =
      ShaderAST.Block(
        ShaderAST.If(
          ShaderAST.DataTypes.bool(true),
          ShaderAST.Block(ShaderAST.Discard()),
          None
        )
      )

    assertEquals(ast.findAll(_ == ShaderAST.Discard()), List(ShaderAST.Discard()))
    assert(ast.exists(ShaderAST.Discard()))

    val replaced =
      ast.traverse { case ShaderAST.Discard() => ShaderAST.Empty() }

    assert(!replaced.exists(ShaderAST.Discard()))
  }

  test("Discard - typeIdent is void") {
    assertEquals(ShaderAST.Discard().typeIdent, ShaderAST.void)
  }

}
