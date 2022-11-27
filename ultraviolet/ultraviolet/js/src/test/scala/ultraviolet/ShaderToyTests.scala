package ultraviolet

import ultraviolet.shadertoy.*
import ultraviolet.shadertoyexamples.*
import ultraviolet.syntax.*

class ShaderToyTests extends munit.FunSuite {

  test("Able to fully define the default shadertoy example") {

    inline def fragment =
      Shader[ShaderToyEnv, Unit] { env =>
        def mainImage(fragColor: vec4, fragCoord: vec2): vec4 = {
          // Normalized pixel coordinates (from 0 to 1)
          val uv: vec2 = fragCoord / env.iResolution.xy

          // Time varying pixel color
          val col: vec3 = 0.5f + 0.5f * cos(env.iTime + uv.xyx + vec3(0.0f, 2.0f, 4.0f))

          // Output to screen
          vec4(col, 1.0f)
        }
      }

    val actual =
      fragment.toGLSL[ShaderToy].code

    // DebugAST.toAST(fragment)
    // println(actual)

    val expected: String =
      """
      |void mainImage(out vec4 fragColor,in vec2 fragCoord){
      |  vec2 uv=fragCoord/iResolution.xy;
      |  vec3 col=0.5+(0.5*(cos((iTime+uv.xyx)+vec3(0.0,2.0,4.0))));
      |  fragColor=vec4(col,1.0);
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)

  }

  test("Real example: Plasma") {
    // Buffer A
    // DebugAST.toAST(Plasma.bufferA)
    // println(Plasma.bufferAShader)
    assertEquals(Plasma.bufferAShader, Plasma.bufferAExpected)

    // Image
    // DebugAST.toAST(Plasma.image)
    // println(Plasma.imageShader)
    assertEquals(Plasma.imageShader, Plasma.imageExpected)
  }

  test("Real example: Seascape") {
    // Image
    // DebugAST.toAST(Seascape.image)
    // println(Seascape.imageShader)
    assertEquals(Seascape.imageShader, Seascape.imageExpected)
  }

}
