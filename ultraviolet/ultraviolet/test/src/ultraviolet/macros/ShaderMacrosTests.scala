package ultraviolet.macros

import ultraviolet.DebugAST
import ultraviolet.syntax.*
import scala.util.Properties

class ShaderMacrosTests extends munit.FunSuite {

  test("placeholder") {
    assert(true)
  }

  // TODO: Bring back, or find another way to test. These tests are very flakey because Mill is sandboxed,
  //       and while we can get the workspace root, the macro's won't support the string interpolation.
  // test("Loading a shader from a file") {

  //   val code: String =
  //     """
  //     |void mainImage( out vec4 fragColor, in vec2 fragCoord )
  //     |{
  //     |    // Normalized pixel coordinates (from 0 to 1)
  //     |    vec2 uv = fragCoord/iResolution.xy;
  //     |
  //     |    // Time varying pixel color
  //     |    vec3 col = 0.5 + 0.5*cos(iTime+uv.xyx+vec3(0,2,4));
  //     |
  //     |    // Output to screen
  //     |    fragColor = vec4(col,1.0);
  //     |}
  //     |""".stripMargin.trim

  //   inline def base = Properties.envOrElse("MILL_WORKSPACE_ROOT", "<missing MILL_WORKSPACE_ROOT>")

  //   inline def shader: Shader[Unit, Unit] =
  //     Shader.fromFile(s"${base}/test-resources/shader.frag")

  //   // DebugAST.toAST(shader)

  //   val actualAst: ShaderAST =
  //     ShaderMacros.toAST(shader).main

  //   val expectedAst: ShaderAST =
  //     ShaderAST.RawLiteral(code)

  //   val actualCode: String =
  //     shader.toGLSL[WebGL2].toOutput.code

  //   val expectedCode: String =
  //     code

  //   assertEquals(actualAst, expectedAst)
  //   assertEquals(actualCode, expectedCode)

  // }

}
