package ultraviolet.acceptance

import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
@nowarn("msg=unset")
@nowarn("msg=mutated")
class Desktop410Tests extends munit.FunSuite {

  test("Can generate the simplest valid GLSL 4.10 Core fragment shader") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
    inline def fragment =
      Shader {
        @out var outColor: vec4 = null

        def main: Unit =
          outColor = vec4(1.0f, 0.0f, 0.5f, 1.0f)
      }

    val actual =
      fragment.toGLSL410(List(ShaderHeader.Version410Core)).code

    val expected: String =
      """
      |#version 410 core
      |out vec4 outColor;
      |void main(){
      |  outColor=vec4(1.0,0.0,0.5,1.0);
      |}
      |""".stripMargin.trim

    assertNoDiff(actual, expected)
    assert(!actual.contains("precision"), s"Expected no precision qualifier in:\n$actual")
  }

  test("Strips inline precision qualifiers from UBO fields on the 4.10 path") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    case class FragEnv(UV: vec2, var COLOR: vec4)
    case class UBO1(TIME: highp[Float], VIEWPORT_SIZE: vec2)
    case class UBO2(customColor: vec4, pos: lowp[vec3])

    inline def fragment =
      Shader[UBO1 & UBO2 & FragEnv, Unit] { env =>
        ubo[UBO1]
        ubo[UBO2]
        env.COLOR = vec4(env.UV, env.TIME, 1.0f)
      }

    val actual =
      fragment.toGLSL410(List(ShaderHeader.Version410Core)).code

    assertNoDiff(
      actual,
      s"""
      |#version 410 core
      |layout (std140) uniform UBO1 {
      |  float TIME;
      |  vec2 VIEWPORT_SIZE;
      |};
      |layout (std140) uniform UBO2 {
      |  vec4 customColor;
      |  vec3 pos;
      |};
      |COLOR=vec4(UV,TIME,1.0);
      |""".stripMargin.trim
    )
  }

  test("toGLSL300 still emits precision qualifiers on UBO fields (regression guard)") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    case class FragEnv(UV: vec2, var COLOR: vec4)
    case class UBO1(TIME: highp[Float], VIEWPORT_SIZE: vec2)

    inline def fragment =
      Shader[UBO1 & FragEnv, Unit] { env =>
        ubo[UBO1]
        env.COLOR = vec4(env.UV, env.TIME, 1.0f)
      }

    val actual =
      fragment.toGLSL300(List(ShaderHeader.Version300ES, ShaderHeader.PrecisionHighPFloat)).code

    assertNoDiff(
      actual,
      s"""
      |#version 300 es
      |precision highp float;
      |layout (std140) uniform UBO1 {
      |  highp float TIME;
      |  vec2 VIEWPORT_SIZE;
      |};
      |COLOR=vec4(UV,TIME,1.0);
      |""".stripMargin.trim
    )
  }

  test("`attribute` is renamed to `in` on the 4.10 path") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    inline def vertex =
      Shader {
        @attribute val a_position: vec4 = null

        def main: Unit =
          ()
      }

    val actual =
      vertex.toGLSL410(List(ShaderHeader.Version410Core)).code

    assert(actual.contains("in vec4 a_position"), s"Expected `in vec4 a_position` in:\n$actual")
    assert(!actual.contains("attribute"), s"Expected no `attribute` keyword in:\n$actual")
  }

  test("Sandbox-native-sdl: vertex shader emits expected GLSL 4.10 Core source") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    final case class VertEnv(gl_VertexID: Int, var gl_Position: vec4)

    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
    inline def vertex: Shader[VertEnv, Unit] =
      Shader[VertEnv, Unit] { env =>
        @out var vUV: vec2 = null

        def main: Unit =
          val pos: array[4, vec2] = array[4, vec2](
            vec2(-1.0f, -1.0f),
            vec2(1.0f, -1.0f),
            vec2(-1.0f, 1.0f),
            vec2(1.0f, 1.0f)
          )
          vUV = pos(env.gl_VertexID) * 0.5f + 0.5f
          env.gl_Position = vec4(pos(env.gl_VertexID), 0.0f, 1.0f)
      }

    val actual =
      vertex.toGLSL410(List(ShaderHeader.Version410Core)).code

    assertNoDiff(
      actual,
      s"""
      |#version 410 core
      |out vec2 vUV;
      |void main(){
      |  vec2 pos[4]=vec2[4](vec2(-1.0,-1.0),vec2(1.0,-1.0),vec2(-1.0,1.0),vec2(1.0,1.0));
      |  vUV=(pos[gl_VertexID]*0.5)+0.5;
      |  gl_Position=vec4(pos[gl_VertexID],0.0,1.0);
      |}
      |""".stripMargin.trim
    )
  }

  test("Sandbox-native-sdl: fragment shader emits expected GLSL 4.10 Core source") {

    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
    inline def fragment: Shader[Unit, Unit] =
      Shader {
        @in val vUV: vec2                   = null
        @layout(0) @out var fragColor: vec4 = null

        def main: Unit =
          fragColor = vec4(vUV.x, vUV.y, 0.0f, 1.0f)
      }

    val actual =
      fragment.toGLSL410(List(ShaderHeader.Version410Core)).code

    assertNoDiff(
      actual,
      s"""
      |#version 410 core
      |in vec2 vUV;
      |layout (location = 0) out vec4 fragColor;
      |void main(){
      |  fragColor=vec4(vUV.x,vUV.y,0.0,1.0);
      |}
      |""".stripMargin.trim
    )
  }

}
