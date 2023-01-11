package ultraviolet.acceptance

import ultraviolet.DebugAST
import ultraviolet.syntax.*

class GLSLExamplesTests extends munit.FunSuite {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class FragEnv(UV: vec2, var COLOR: vec4)

  test("can build a multi-statement function") {

    inline def shader: Shader[FragEnv, vec4] =
      Shader { env =>
        def calculateColour(uv: vec2, sdf: Float): vec4 =
          val fill       = vec4(uv, 0.0f, 1.0f)
          val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
          vec4(fill.xyz * fillAmount, fillAmount)

        calculateColour(env.UV, 3.0)
      }

    val actual =
      shader.toGLSL[WebGL2].toOutput.code

    assertEquals(
      actual,
      s"""
      |vec4 calculateColour(in vec2 uv,in float sdf){
      |  vec4 fill=vec4(uv,0.0,1.0);
      |  float fillAmount=(1.0-step(0.0,sdf))*fill.w;
      |  return vec4(fill.xyz*fillAmount,fillAmount);
      |}
      |calculateColour(UV,3.0);
      |""".stripMargin.trim
    )
  }

  test("Small procedural shader") {

    inline def fragment: Shader[FragEnv, Unit] =
      Shader { env =>
        def circleSdf(p: vec2, r: Float): Float =
          length(p) - r

        def calculateColour(uv: vec2, sdf: Float): vec4 =
          val fill       = vec4(uv, 0.0f, 1.0f)
          val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
          vec4(fill.xyz * fillAmount, fillAmount)

        def main: Unit =
          val sdf = circleSdf(env.UV - 0.5f, 0.5f)
          calculateColour(env.UV, sdf)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    assertEquals(
      actual,
      s"""
      |float circleSdf(in vec2 p,in float r){
      |  return length(p)-r;
      |}
      |vec4 calculateColour(in vec2 uv,in float sdf){
      |  vec4 fill=vec4(uv,0.0,1.0);
      |  float fillAmount=(1.0-step(0.0,sdf))*fill.w;
      |  return vec4(fill.xyz*fillAmount,fillAmount);
      |}
      |void main(){
      |  float sdf=circleSdf(UV-0.5,0.5);
      |  calculateColour(UV,sdf);
      |}
      |""".stripMargin.trim
    )
  }

  test("Small procedural shader with fragment function") {

    inline def fragment =
      Shader[FragEnv] { env =>
        def circleSdf(p: vec2, r: Float): Float =
          length(p) - r

        def calculateColour(uv: vec2, sdf: Float): vec4 =
          val fill       = vec4(uv, 0.0f, 1.0f)
          val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
          vec4(fill.xyz * fillAmount, fillAmount)

        def fragment: Unit =
          val sdf = circleSdf(env.UV - 0.5f, 0.5f)
          env.COLOR = calculateColour(env.UV, sdf)
      }

    val actual =
      fragment.toGLSL[WebGL2].toOutput.code

    val expected =
      s"""
      |float circleSdf(in vec2 p,in float r){
      |  return length(p)-r;
      |}
      |vec4 calculateColour(in vec2 uv,in float sdf){
      |  vec4 fill=vec4(uv,0.0,1.0);
      |  float fillAmount=(1.0-step(0.0,sdf))*fill.w;
      |  return vec4(fill.xyz*fillAmount,fillAmount);
      |}
      |void fragment(){
      |  float sdf=circleSdf(UV-0.5,0.5);
      |  COLOR=calculateColour(UV,sdf);
      |}
      |""".stripMargin.trim

    assertEquals(
      actual,
      expected
    )
  }

}
