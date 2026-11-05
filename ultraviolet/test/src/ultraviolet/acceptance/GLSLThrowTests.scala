package ultraviolet.acceptance

import scala.annotation.nowarn

@nowarn("msg=unused")
class GLSLThrowTests extends munit.FunSuite {

  test("Throwing an arbitrary exception is a compile error") {

    val errors =
      compileErrors(
        """{
  import ultraviolet.syntax.*

  inline def fragment =
    Shader {
      def main: Unit =
        throw new RuntimeException("boom")
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("Shaders do not support throw"), errors)
  }

}
