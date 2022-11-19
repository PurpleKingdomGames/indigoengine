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
      |  vec2 uv=fragCoord/iResolution.xy;
      |  vec3 col=0.5+(0.5*(cos((iTime+uv.xyx)+vec3(0.0,2.0,4.0))));
      |  fragColor=vec4(col,1.0);
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)

  }

  test("Real example: Plasma") {
    // Buffer A
    // DebugAST.toAST(Plasma.shaderToyExample)
    // println(actual)

    val bufferAExpected: String =
      """
      |const float pi=3.1415927410125732;
      |void mainImage(out vec4 fragColor, in vec2 fragCoord){
      |  float i=fragCoord/iResolution;
      |  vec3 t=(iTime+iMouse)/vec3(63.0,78.0,45.0);
      |  vec3 cs=cos(((i*pi)*2.0)+((vec3(0.0,1.0,-0.5))*pi)+t);
      |  fragColor=vec4(0.5+(0.5*cs),1.0);
      |}
      |""".stripMargin.trim

    assertEquals(Plasma.bufferAShader, bufferAExpected)

    // Image
    // DebugAST.toAST(Plasma.shaderToyExample)
    // println(actual)

    val imageExpected: String =
      """
      |const vec2 vp=vec2(320.0,200.0);
      |void mainImage(out vec4 fragColor, in vec2 fragCoord){
      |  float t=(iTime*10.0)+iMouse;
      |  vec2 uv=fragCoord.xy/iResolution.xy;
      |  vec2 p0=(uv-0.5)*vp;
      |  vec2 hvp=vp*0.5;
      |  vec2 p1d=((vec2(cos(t/98.0),sin(t/178.0)))*hvp)-p0;
      |  vec2 p2d=((vec2(sin((-t)/124.0),cos((-t)/104.0)))*hvp)-p0;
      |  vec2 p3d=((vec2(cos((-t)/165.0),cos(t/45.0)))*hvp)-p0;
      |  float sum=0.25+(0.5*(cos(length(p1d)/30.0))+(cos(length(p2d)/20.0))+(sin(length(p3d)/25.0))*(sin(p3d.x/20.0))*(sin(p3d.y/15.0)));
      |  fragColor=texture(iChannel0,vec2(fract(sum),0.0));
      |}
      |""".stripMargin.trim

    assertEquals(Plasma.imageShader, imageExpected)
  }

}

object Plasma:

  import ultraviolet.predef.shadertoy.*
  import ultraviolet.predef.shadertoy.given

  inline def bufferA =
    Shader[ShaderToyEnv, vec4] { env =>
      // TODO: This needs to appear outside the main() function in the template...
      @const val pi: Float = 3.1415926435f

      val i: Float = env.fragCoord.x / env.iResolution.x
      val t: vec3  = (env.iTime + env.iMouse.y) / vec3(63.0f, 78.0f, 45.0f)
      val cs: vec3 = cos(i * pi * 2.0f + vec3(0.0f, 1.0f, -0.5f) * pi + t)

      vec4(0.5f + 0.5f * cs, 1.0f)
    }

  val bufferAShader = bufferA.toGLSL

  inline def image =
    Shader[ShaderToyEnv, vec4] { env =>
      // TODO: This needs to appear outside the main() function in the template...
      @const val vp: vec2 = vec2(320.0, 200.0)

      val t: Float  = env.iTime * 10.0f + env.iMouse.x
      val uv: vec2  = env.fragCoord.xy / env.iResolution.xy
      val p0: vec2  = (uv - 0.5f) * vp
      val hvp: vec2 = vp * 0.5f
      val p1d: vec2 = vec2(cos(t / 98.0f), sin(t / 178.0f)) * hvp - p0
      val p2d: vec2 = vec2(sin(-t / 124.0f), cos(-t / 104.0f)) * hvp - p0
      val p3d: vec2 = vec2(cos(-t / 165.0f), cos(t / 45.0f)) * hvp - p0
      val sum: Float = 0.25f + 0.5f * (cos(length(p1d) / 30.0f) +
        cos(length(p2d) / 20.0f) +
        sin(length(p3d) / 25.0f) * sin(p3d.x / 20.0f) * sin(p3d.y / 15.0f))

      texture(env.iChannel0, vec2(fract(sum), 0))
    }

  val imageShader = image.toGLSL

/*
Original - https://www.shadertoy.com/view/XsVSDz

This shader comes in two parts:
1. 'Buffer A' serves as an input (iChannel0) for 'Image'.
2. 'Image' gives the final output.
 */

/* Buffer A
const float pi = 3.1415926435;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    float i = fragCoord.x / iResolution.x;
    vec3 t = (iTime + iMouse.y) / vec3(63.0, 78.0, 45.0);
    vec3 cs = cos(i * pi * 2.0 + vec3(0.0, 1.0, -0.5) * pi + t);
    fragColor = vec4(0.5 + 0.5 * cs, 1.0);
}
 */

/* Image
const vec2 vp = vec2(320.0, 200.0);

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
  float t = iTime * 10.0 + iMouse.x;
  vec2 uv = fragCoord.xy / iResolution.xy;
    vec2 p0 = (uv - 0.5) * vp;
    vec2 hvp = vp * 0.5;
	vec2 p1d = vec2(cos( t / 98.0),  sin( t / 178.0)) * hvp - p0;
	vec2 p2d = vec2(sin(-t / 124.0), cos(-t / 104.0)) * hvp - p0;
	vec2 p3d = vec2(cos(-t / 165.0), cos( t / 45.0))  * hvp - p0;
    float sum = 0.5 + 0.5 * (
    cos(length(p1d) / 30.0) +
    cos(length(p2d) / 20.0) +
    sin(length(p3d) / 25.0) * sin(p3d.x / 20.0) * sin(p3d.y / 15.0));
    fragColor = texture(iChannel0, vec2(fract(sum), 0));
}
 */
end Plasma
