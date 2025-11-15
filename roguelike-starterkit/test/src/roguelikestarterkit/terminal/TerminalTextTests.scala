package roguelikestarterkit.terminal

class TerminalTextTests extends munit.FunSuite {

  test("Validate the terminal text shader") {
    import ultraviolet.syntax.*

    val actual =
      TerminalText.ShaderImpl.frag.toGLSL[WebGL2].toOutput.code

    // println(actual)

    assert(actual.nonEmpty)
  }

}
