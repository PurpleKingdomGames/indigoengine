package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLInterpolatorTests extends munit.FunSuite {
  test("hex interpolator") {
    inline def fragment: Shader[Unit, vec4] = Shader(_ => vec4(hex"#ff00ff", 0f))

    // println(DebugAST.toAST(fragment))

    val actual = fragment.toGLSL[WebGL2].toOutput.code

    assertEquals(actual, "vec4(vec3(1.0,0.0,1.0),0.0);")
  }

  test("hexa interpolator") {
    inline def fragment: Shader[Unit, vec4] = Shader(_ => hexa"#ff00ff00")

    // println(DebugAST.toAST(fragment))

    val actual = fragment.toGLSL[WebGL2].toOutput.code

    assertEquals(actual, "vec4(1.0,0.0,1.0,0.0);")
  }

  test("rgb interpolator") {
    inline def fragment: Shader[Unit, vec4] = Shader(_ => vec4(rgb"255,0,255", 0f))

    // println(DebugAST.toAST(fragment))

    val actual = fragment.toGLSL[WebGL2].toOutput.code

    assertEquals(actual, "vec4(vec3(1.0,0.0,1.0),0.0);")
  }

  test("rgba interpolator") {
    inline def fragment: Shader[Unit, vec4] = Shader(_ => rgba"255,0,255,0")

    // println(DebugAST.toAST(fragment))

    val actual = fragment.toGLSL[WebGL2].toOutput.code

    assertEquals(actual, "vec4(1.0,0.0,1.0,0.0);")
  }
}
