package io.indigoengine.roguelike.starterkit.terminal

class TerminalEntityTests extends munit.FunSuite {
  
  test("Validate the terminal entity shader") {
    import ultraviolet.syntax.*

    val actual =
      TerminalEntity.ShaderImpl.frag.toGLSL[WebGL2].toOutput.code

    // println(actual)

    assert(actual.nonEmpty)
  }

}
