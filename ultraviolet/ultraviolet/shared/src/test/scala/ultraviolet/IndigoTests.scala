package ultraviolet

import ultraviolet.indigoexamples.*
import ultraviolet.syntax.*

class IndigoTests extends munit.FunSuite {

  test("Real example: NoOp") {
    // DebugAST.toAST(NoOp.vertex.shader)
    // println(NoOp.vertex.shader)
    assertEquals(NoOp.vertex.output.code, NoOp.vertex.expected)

    // DebugAST.toAST(NoOp.fragment.shader)
    // println(NoOp.fragment.shader)
    assertEquals(NoOp.fragment.output.code, NoOp.fragment.expected)
  }

  test("Real example: Blit") {
    // DebugAST.toAST(Blit.fragment.shader)
    // println(Blit.fragment.shader)
    assertEquals(Blit.fragment.output.code, Blit.fragment.expected)
  }

}
