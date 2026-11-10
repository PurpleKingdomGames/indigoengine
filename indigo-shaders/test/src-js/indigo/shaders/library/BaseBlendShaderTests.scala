package indigo.shaders.library

import indigo.shaders.BlendShader
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class BaseBlendShaderTests extends munit.FunSuite {

  test("Merge WebGL 2.0 vertex shader") {

    inline def modifyVertex: Shader[IndigoUV.VertexEnv, Unit] =
      Shader[IndigoUV.VertexEnv] { _ =>
        def vertex(v: vec4): vec4 =
          v + vec4(1.0f)
      }

    val actual =
      BlendShader.vertex(modifyVertex, IndigoUV.VertexEnv.reference).code

    val expected1: String =
      """
      |vec4 vertex(in vec4 v){
      |  return v+vec4(1.0);
      |}
      |""".stripMargin.trim

    val expected2: String =
      """
      |VERTEX=vertex(VERTEX);
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1.replace("\r\n", "\n"))))
    assert(clue(actual).contains(clue(expected2.replace("\r\n", "\n"))))
  }

  test("Merge WebGL 2.0 vertex shader template") {

    val noop = NoOp.vertex.toGLSL300.code

    val actual =
      BlendShader.vertexTemplate(noop)

    val expected1: String =
      """
      |vec4 vertex(in vec4 v){
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1)))
  }

  test("Merge WebGL 2.0 fragment shader") {

    inline def modifyColor: Shader[IndigoUV.BlendFragmentEnv, Unit] =
      Shader[IndigoUV.BlendFragmentEnv] { _ =>
        def fragment(v: vec4): vec4 =
          v + vec4(1.0f)
      }

    val actual =
      BlendShader.fragment(modifyColor, IndigoUV.BlendFragmentEnv.reference).code

    val expected1: String =
      """
      |vec4 fragment(in vec4 v){
      |  return v+vec4(1.0);
      |}
      |""".stripMargin.trim

    val expected2: String =
      """
      |COLOR=fragment(COLOR);
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1.replace("\r\n", "\n"))))
    assert(clue(actual).contains(clue(expected2.replace("\r\n", "\n"))))
  }

  test("Merge WebGL 2.0 fragment shader exposes SRC_TEXEL_SIZE and derives DST_TEXEL_SIZE from the viewport") {

    inline def useTexel: Shader[IndigoUV.BlendFragmentEnv, Unit] =
      Shader[IndigoUV.BlendFragmentEnv] { env =>
        def fragment(v: vec4): vec4 =
          texture2D(env.SRC_CHANNEL, env.UV + env.SRC_TEXEL_SIZE * 3.0f) +
            texture2D(env.DST_CHANNEL, env.UV + env.DST_TEXEL_SIZE * 3.0f)
      }

    val actual =
      BlendShader.fragment(useTexel, IndigoUV.BlendFragmentEnv.reference).code

    assert(clue(actual).contains("vec2 SRC_TEXEL_SIZE;"))
    assert(clue(actual).contains("vec2 DST_TEXEL_SIZE;"))
    assert(clue(actual).contains("DST_TEXEL_SIZE=1.0/VIEWPORT_SIZE;"))
    assert(clue(actual).contains("texture(SRC_CHANNEL,UV+(SRC_TEXEL_SIZE*3.0))"))
    assert(clue(actual).contains("texture(DST_CHANNEL,UV+(DST_TEXEL_SIZE*3.0))"))
    assert(!clue(actual).contains("vec2 TEXEL_SIZE;"))
  }

}
