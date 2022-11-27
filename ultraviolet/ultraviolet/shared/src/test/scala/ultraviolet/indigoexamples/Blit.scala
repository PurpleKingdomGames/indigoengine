package ultraviolet.indigoexamples

import ultraviolet.predef.indigo.*
import ultraviolet.syntax.*

object Blit:

  case class IndigoBitmapData(FILLTYPE: highp[Float])

  object fragment:
    inline def shader =
      Shader[IndigoFragmentEnv & IndigoBitmapData] { env =>
        ubo[IndigoBitmapData]

        def stretchedUVs(pos: vec2, size: vec2): vec2 =
          pos + env.UV * size

        def tiledUVs(pos: vec2, size: vec2): vec2 =
          pos + (fract(env.UV * (env.SIZE / env.TEXTURE_SIZE)) * size)

        def fragment: vec4 =
          env.FILLTYPE.toInt match
            case 1 =>
              env.CHANNEL_0 = texture2D(env.SRC_CHANNEL, stretchedUVs(env.CHANNEL_0_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_1 = texture2D(env.SRC_CHANNEL, stretchedUVs(env.CHANNEL_1_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_2 = texture2D(env.SRC_CHANNEL, stretchedUVs(env.CHANNEL_2_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_3 = texture2D(env.SRC_CHANNEL, stretchedUVs(env.CHANNEL_3_POSITION, env.CHANNEL_0_SIZE))

            case 2 =>
              env.CHANNEL_0 = texture2D(env.SRC_CHANNEL, tiledUVs(env.CHANNEL_0_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_1 = texture2D(env.SRC_CHANNEL, tiledUVs(env.CHANNEL_1_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_2 = texture2D(env.SRC_CHANNEL, tiledUVs(env.CHANNEL_2_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_3 = texture2D(env.SRC_CHANNEL, tiledUVs(env.CHANNEL_3_POSITION, env.CHANNEL_0_SIZE))

            case _ =>
              ()

          env.CHANNEL_0;
      }

    val output = shader.toGLSL[Indigo]

    val expected: String =
      """
      |layout (std140) uniform IndigoBitmapData {
      |  highp float FILLTYPE;
      |};
      |vec2 stretchedUVs(in vec2 pos,in vec2 size){
      |  return pos+(UV*size);
      |}
      |vec2 tiledUVs(in vec2 pos,in vec2 size){
      |  return pos+((fract(UV*(SIZE/TEXTURE_SIZE)))*size);
      |}
      |void fragment(){
      |  switch(int(FILLTYPE)){
      |    case 1:
      |      CHANNEL_0=texture(SRC_CHANNEL,stretchedUVs(CHANNEL_0_POSITION,CHANNEL_0_SIZE));
      |      CHANNEL_1=texture(SRC_CHANNEL,stretchedUVs(CHANNEL_1_POSITION,CHANNEL_0_SIZE));
      |      CHANNEL_2=texture(SRC_CHANNEL,stretchedUVs(CHANNEL_2_POSITION,CHANNEL_0_SIZE));
      |      CHANNEL_3=texture(SRC_CHANNEL,stretchedUVs(CHANNEL_3_POSITION,CHANNEL_0_SIZE));
      |      break;
      |    case 2:
      |      CHANNEL_0=texture(SRC_CHANNEL,tiledUVs(CHANNEL_0_POSITION,CHANNEL_0_SIZE));
      |      CHANNEL_1=texture(SRC_CHANNEL,tiledUVs(CHANNEL_1_POSITION,CHANNEL_0_SIZE));
      |      CHANNEL_2=texture(SRC_CHANNEL,tiledUVs(CHANNEL_2_POSITION,CHANNEL_0_SIZE));
      |      CHANNEL_3=texture(SRC_CHANNEL,tiledUVs(CHANNEL_3_POSITION,CHANNEL_0_SIZE));
      |      break;
      |    default:
      |      break;
      |  }
      |  COLOR=CHANNEL_0;
      |}
      |""".stripMargin.trim
/*
// Original

layout (std140) uniform IndigoBitmapData {
  highp float FILLTYPE;
};

vec2 stretchedUVs(vec2 pos, vec2 size) {
  return pos + UV * size;
}

vec2 tiledUVs(vec2 pos, vec2 size) {
  return pos + (fract(UV * (SIZE / TEXTURE_SIZE)) * size);
}

void fragment(){

  // 0 = normal; 1 = stretch; 2 = tile
  int fillType = int(round(FILLTYPE));

  switch(fillType) {
    case 1:
      CHANNEL_0 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_0_POSITION, CHANNEL_0_SIZE));
      CHANNEL_1 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_1_POSITION, CHANNEL_0_SIZE));
      CHANNEL_2 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_2_POSITION, CHANNEL_0_SIZE));
      CHANNEL_3 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_3_POSITION, CHANNEL_0_SIZE));
      break;

    case 2:
      CHANNEL_0 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_0_POSITION, CHANNEL_0_SIZE));
      CHANNEL_1 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_1_POSITION, CHANNEL_0_SIZE));
      CHANNEL_2 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_2_POSITION, CHANNEL_0_SIZE));
      CHANNEL_3 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_3_POSITION, CHANNEL_0_SIZE));
      break;

    default:
      break;
  }

  COLOR = CHANNEL_0;
}
 */
