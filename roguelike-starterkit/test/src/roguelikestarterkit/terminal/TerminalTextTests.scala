package roguelikestarterkit.terminal

// import ultraviolet.utils.ShaderASTPrinter

class TerminalTextTests extends munit.FunSuite {

  test("Validate the terminal text shader") {
    import ultraviolet.syntax.*

    inline def fragment =
      TerminalText.ShaderImpl.frag

    val actual =
      fragment.toGLSL300.code

    // val outputAST = ShaderASTPrinter.printAST(fragment)
    // val outputASTTransformed = ShaderASTPrinter.printASTTransformed(fragment, ultraviolet.datatypes.ProgramVersion.GLSL_300)
    // println(actual)
    // println("---")
    // println(outputAST)
    // println("---")
    // println(outputASTTransformed)

    assert(actual.nonEmpty)
  }

}
