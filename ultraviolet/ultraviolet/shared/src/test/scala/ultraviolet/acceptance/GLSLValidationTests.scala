package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.datatypes.ShaderResult
import ultraviolet.syntax.*

class GLSLValidationTests extends munit.FunSuite {

  test("GLSL printer validation error are represented") {
    inline def fragment =
      Shader {
        val a = 1
      }

    given p: ShaderPrinter[WebGL2] = new ShaderPrinter[WebGL2] {

      val defaultConfig: ShaderPrinterConfig =
        ShaderPrinterConfig.noHeaders

      def isValid(
          inType: Option[String],
          outType: Option[String],
          functions: List[ShaderAST],
          body: ShaderAST
      ): ShaderValid = ShaderValid.Invalid(List("boom"))

      def transformer: PartialFunction[ShaderAST, ShaderAST] = { case x =>
        x
      }

      def printer: PartialFunction[ShaderAST, List[String]] = { case x =>
        Nil
      }
    }

    val actual = fragment.toGLSL[WebGL2](using p)

    // DebugAST.toAST(fragment)
    // println(actual)

    assertEquals(
      actual,
      ShaderResult.Error("[ultraviolet] Shader failed to validate because: [boom]")
    )
  }

}
