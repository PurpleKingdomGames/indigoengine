package ultraviolet

import ultraviolet.syntax.*

class ShaderToyTests extends munit.FunSuite {

  test("Able to fully define the default shadertoy example") {
    import ultraviolet.predef.shadertoy.*
    import ultraviolet.predef.shadertoy.given

    inline def fragment =
      Shader[ShaderToyEnv, vec4] { env =>
        // Normalized pixel coordinates (from 0 to 1)
        val uv: vec2 = env.fragCoord / env.iResolution.xy

        // Time varying pixel color
        val col: vec3 = 0.5f + 0.5f * cos(env.iTime + uv.xyx + vec3(0.0f, 2.0f, 4.0f))

        // Output to screen
        vec4(col, 1.0f)
      }

    assertEquals(fragment.run(ShaderToyEnv.Default), vec4(vec3(0.5f), 1.0f))

    val actual =
      fragment.toGLSL

    // DebugAST.toAST(fragment)
    // println(actual)

    val expected: String =
      """
      |void mainImage(out vec4 fragColor, in vec2 fragCoord){
      |  vec2 uv=(fragCoord)/(iResolution.xy);
      |  vec3 col=(0.5)+((0.5)*(cos(((iTime)+(uv.xyx))+(vec3(0.0,2.0,4.0)))));
      |  fragColor=vec4(col,1.0);
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)

  }

}
