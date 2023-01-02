package ultraviolet.acceptance

import ultraviolet.indigoexamples.WebGL2Merge
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

  test("Real example: WebGL2Merge") {
    // DebugAST.toAST(WebGL2Merge.vertex.shader)
    // println(WebGL2Merge.vertex.shader)
    assertEquals(WebGL2Merge.vertex.output.code, WebGL2Merge.vertex.expected)

    // DebugAST.toAST(WebGL2Merge.fragment.shader)
    // println(WebGL2Merge.fragment.shader)
    assertEquals(WebGL2Merge.fragment.output.code, WebGL2Merge.fragment.expected)
  }

  test("Real example: WebGL2Base") {
    // DebugAST.toAST(WebGL2Base.vertex.shader)
    // println(WebGL2Base.vertex.output.code)
    assertEquals(WebGL2Base.vertex.output.code, WebGL2Base.vertex.expected)

    // DebugAST.toAST(WebGL2Base.fragment.shader)
    // println(WebGL2Base.fragment.output.code)
    assertEquals(WebGL2Base.fragment.output.code, WebGL2Base.fragment.expected)
  }

}
