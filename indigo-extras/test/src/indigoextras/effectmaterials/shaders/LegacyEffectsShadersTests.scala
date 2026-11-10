package indigoextras.effectmaterials.shaders

import ultraviolet.syntax.*

class LegacyEffectsShadersTests extends munit.FunSuite {

  test("Legacy effects vertex shader") {

    val actual =
      LegacyEffectsShaders.vertex.toGLSL300.code

    val expected: String =
      """
      |out vec2 v_offsetTL;
      |out vec2 v_offsetTC;
      |out vec2 v_offsetTR;
      |out vec2 v_offsetML;
      |out vec2 v_offsetMC;
      |out vec2 v_offsetMR;
      |out vec2 v_offsetBL;
      |out vec2 v_offsetBC;
      |out vec2 v_offsetBR;
      |const vec2 gridOffsets[9]=vec2[9](vec2(-1.0,-1.0),vec2(0.0,-1.0),vec2(1.0,-1.0),vec2(-1.0,0.0),vec2(0.0,0.0),vec2(1.0,0.0),vec2(-1.0,1.0),vec2(0.0,1.0),vec2(1.0,1.0));
      |vec2[9] generateTexCoords3x3(){
      |  return vec2[9](UV+(TEXEL_SIZE*gridOffsets[0]),UV+(TEXEL_SIZE*gridOffsets[1]),UV+(TEXEL_SIZE*gridOffsets[2]),UV+(TEXEL_SIZE*gridOffsets[3]),UV+(TEXEL_SIZE*gridOffsets[4]),UV+(TEXEL_SIZE*gridOffsets[5]),UV+(TEXEL_SIZE*gridOffsets[6]),UV+(TEXEL_SIZE*gridOffsets[7]),UV+(TEXEL_SIZE*gridOffsets[8]));
      |}
      |vec4 vertex(in vec4 v){
      |  vec2 offsets[9]=generateTexCoords3x3();
      |  v_offsetTL=(offsets[0]*FRAME_SIZE)+CHANNEL_0_ATLAS_OFFSET;
      |  v_offsetTC=(offsets[1]*FRAME_SIZE)+CHANNEL_0_ATLAS_OFFSET;
      |  v_offsetTR=(offsets[2]*FRAME_SIZE)+CHANNEL_0_ATLAS_OFFSET;
      |  v_offsetML=(offsets[3]*FRAME_SIZE)+CHANNEL_0_ATLAS_OFFSET;
      |  v_offsetMC=(offsets[4]*FRAME_SIZE)+CHANNEL_0_ATLAS_OFFSET;
      |  v_offsetMR=(offsets[5]*FRAME_SIZE)+CHANNEL_0_ATLAS_OFFSET;
      |  v_offsetBL=(offsets[6]*FRAME_SIZE)+CHANNEL_0_ATLAS_OFFSET;
      |  v_offsetBC=(offsets[7]*FRAME_SIZE)+CHANNEL_0_ATLAS_OFFSET;
      |  v_offsetBR=(offsets[8]*FRAME_SIZE)+CHANNEL_0_ATLAS_OFFSET;
      |  return v;
      |}
      |""".stripMargin.trim

    assertNoDiff(actual, expected)
  }

}
