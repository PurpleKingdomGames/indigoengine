package ultraviolet.acceptance

import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class GLSLReservedWordsTests extends munit.FunSuite {

  test("GLSL reserved words cannot be used as variable names") {
    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader[Unit, Float] { _ =>
      val switch = 1.0f
      switch
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("'switch' is a reserved word in GLSL, please choose a different name."), errors)
  }

  test("GLSL reserved words cannot be used as function names") {
    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader[Unit, Float] { _ =>
      def switch(): Float = 1.0f
      switch()
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("'switch' is a reserved word in GLSL, please choose a different name."), errors)
  }

  test("GLSL reserved words cannot be used as function parameter names") {
    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader[Unit, Float] { _ =>
      def f(switch: Float): Float = switch
      f(1.0f)
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("'switch' is a reserved word in GLSL, please choose a different name."), errors)
  }

  test("GLSL reserved words are not checked when validation is disabled") {
    inline def fragment =
      Shader[Unit, Float] { _ =>
        val switch = 1.0f
        switch
      }

    val actual =
      fragment.toGLSL(ProgramVersion.GLSL_300, Nil, false).code

    assert(actual.contains("float switch=1.0;"), actual)
  }

  test("GLSL reserved words cannot be declared inside external function call arguments") {
    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader[Unit, Unit] { _ =>
      def f(): Float = sin({ val switch = 1.0f; switch })
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("'switch' is a reserved word in GLSL, please choose a different name."), errors)
  }

  test("GLSL reserved words cannot be declared inside function call arguments") {
    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader[Unit, Unit] { _ =>
      def g(x: Float): Float = x
      def f(): Float = g({ val switch = 1.0f; switch })
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("'switch' is a reserved word in GLSL, please choose a different name."), errors)
  }

  test("GLSL reserved words cannot be declared inside struct constructor arguments") {
    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader[Unit, Unit] { _ =>
      class Light(val strength: Float)
      def f(): Light = Light({ val switch = 1.0f; switch })
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("'switch' is a reserved word in GLSL, please choose a different name."), errors)
  }

  test("GLSL reserved words cannot be used for struct members") {
    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader[Unit, Unit] { _ =>
      class Light(val switch: Float)
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("'switch' is a reserved word in GLSL, please choose a different name."), errors)
  }

  test("GLSL reserved words cannot be used for annotated variables") {
    val errors =
      compileErrors(
        """{
  inline def fragment =
    Shader {
      @uniform val switch: Float = 0.0f
    }

  fragment.toGLSL300.code
}"""
      )

    assert(errors.contains("'switch' is a reserved word in GLSL, please choose a different name."), errors)
  }

}
