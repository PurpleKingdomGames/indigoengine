package ultraviolet.indigoexamples

import ultraviolet.predef.indigo.*
import ultraviolet.syntax.*

object WebGL2Base:

  case class IndigoProjectionData(u_projection: mat4)
  case class IndigoFrameData(
      TIME: highp[Float], // Running time
      VIEWPORT_SIZE: vec2 // Size of the viewport in pixels
  )
  case class IndigoCloneReferenceData( // Used during cloning.
      u_ref_refFlip: vec4,
      u_ref_sizeAndFrameScale: vec4,
      u_ref_channelOffsets01: vec4,
      u_ref_channelOffsets23: vec4,
      u_ref_textureSizeAtlasSize: vec4
  )

  case class GLEnv(gl_InstanceID: Int)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  case class VertEnv(var gl_Position: vec4)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  object vertex:
    inline def shader =
      Shader[GLEnv & VertEnv & IndigoFrameData & IndigoProjectionData & IndigoCloneReferenceData] { env =>
        Version300ES
        PrecisionMediumPFloat

        @layout(0) @in val a_verticesAndCoords: vec4    = null
        @layout(1) @in val a_translateScale: vec4       = null
        @layout(2) @in val a_refFlip: vec4              = null
        @layout(3) @in val a_sizeAndFrameScale: vec4    = null
        @layout(4) @in val a_channelOffsets01: vec4     = null
        @layout(5) @in val a_channelOffsets23: vec4     = null
        @layout(6) @in val a_textureSizeAtlasSize: vec4 = null
        @layout(7) @in val a_rotation: Float            = 0.0f

        ubo[IndigoProjectionData]
        ubo[IndigoFrameData]
        ubo[IndigoCloneReferenceData]

        @uniform val u_baseTransform: mat4 = null
        @uniform val u_mode: Int           = 0

        @out var v_channel_coords_01: vec4    = null // Scaled to position on texture atlas
        @out var v_channel_coords_23: vec4    = null // Scaled to position on texture atlas
        @out var v_uv_size: vec4              = null // Unscaled texture coordinates + Width / height of the objects
        @out var v_screenCoordsRotation: vec3 = null // Where is this pixel on the screen? How much is it rotated by
        @out var v_textureSize: vec2          = null // Actual size of the texture in pixels.
        @out var v_atlasSizeAsUV: vec4 =
          null // Actual size of the atlas in pixels, and it's relative size in UV coords.
        @out var v_channel_pos_01: vec4         = null // Position on the atlas of channels 0 and 1.
        @out var v_channel_pos_23: vec4         = null // Position on the atlas of channels 2 and 3.
        @flat @out var v_instanceId: Int = 0    // The current instance id
        // flat out int v_instanceId // The current instance id

        // Constants
        @const val PI: Float    = 3.141592653589793f;
        @const val PI_2: Float  = PI * 0.5f;
        @const val PI_4: Float  = PI * 0.25f;
        @const val TAU: Float   = 2.0f * PI;
        @const val TAU_2: Float = PI;
        @const val TAU_4: Float = PI_2;
        @const val TAU_8: Float = PI_4;

        // Variables
        var ATLAS_SIZE: vec2               = null
        var VERTEX: vec4                   = null
        var TEXTURE_SIZE: vec2             = null
        var UV: vec2                       = null
        var SIZE: vec2                     = null
        var FRAME_SIZE: vec2               = null
        var CHANNEL_0_ATLAS_OFFSET: vec2   = null
        var CHANNEL_1_ATLAS_OFFSET: vec2   = null
        var CHANNEL_2_ATLAS_OFFSET: vec2   = null
        var CHANNEL_3_ATLAS_OFFSET: vec2   = null
        var CHANNEL_0_TEXTURE_COORDS: vec2 = null
        var CHANNEL_1_TEXTURE_COORDS: vec2 = null
        var CHANNEL_2_TEXTURE_COORDS: vec2 = null
        var CHANNEL_3_TEXTURE_COORDS: vec2 = null
        var CHANNEL_0_POSITION: vec2       = null
        var CHANNEL_1_POSITION: vec2       = null
        var CHANNEL_2_POSITION: vec2       = null
        var CHANNEL_3_POSITION: vec2       = null
        var CHANNEL_0_SIZE: vec2           = null
        var POSITION: vec2                 = null
        var SCALE: vec2                    = null
        var REF: vec2                      = null
        var FLIP: vec2                     = null
        var ROTATION: Float                = 0.0f
        var INSTANCE_ID: Int               = 0

        // format: off
        def translate2d(t: vec2): mat4 =
          mat4(1.0f, 0.0f, 0.0f, 0.0f,
               0.0f, 1.0f, 0.0f, 0.0f,
               0.0f, 0.0f, 1.0f, 0.0f,
               t.x,  t.y,  0.0f, 1.0f
          )

        // format: off
        def scale2d(s: vec2): mat4 =
          mat4(s.x,  0.0f, 0.0f, 0.0f,
               0.0f, s.y,  0.0f, 0.0f,
               0.0f, 0.0f, 1.0f, 0.0f,
               0.0f, 0.0f, 0.0f, 1.0f
          )

        // format: off
        def rotate2d(angle: Float): mat4 =
          mat4(cos(angle), -sin(angle), 0.0f, 0.0f,
               sin(angle), cos(angle),  0.0f, 0.0f,
               0.0f,       0.0f,        1.0f, 0.0f,
               0.0f,       0.0f,        0.0f, 1.0f
          )
        
        def scaleCoordsWithOffset(texcoord: vec2, offset: vec2): vec2 =
          val transform: mat4 = translate2d(offset) * scale2d(FRAME_SIZE)
          (transform * vec4(texcoord, 1.0f, 1.0f)).xy
        
        //#vertex_start
        def vertex(): Unit = ()
        //#vertex_end

        def main: Unit =

            INSTANCE_ID = env.gl_InstanceID

            VERTEX = vec4(a_verticesAndCoords.xy, 1.0f, 1.0f)
            UV = a_verticesAndCoords.zw
            ROTATION = a_rotation
            POSITION = a_translateScale.xy
            SCALE = a_translateScale.zw

            // 0 = normal, 1 = clone batch, 2 = clone tiles
            u_mode match
              case 0 =>
                ATLAS_SIZE = a_textureSizeAtlasSize.zw
                TEXTURE_SIZE = a_textureSizeAtlasSize.xy
                SIZE = a_sizeAndFrameScale.xy
                FRAME_SIZE = a_sizeAndFrameScale.zw
                REF = a_refFlip.xy
                FLIP = a_refFlip.zw
                CHANNEL_0_ATLAS_OFFSET = a_channelOffsets01.xy
                CHANNEL_1_ATLAS_OFFSET = a_channelOffsets01.zw
                CHANNEL_2_ATLAS_OFFSET = a_channelOffsets23.xy
                CHANNEL_3_ATLAS_OFFSET = a_channelOffsets23.zw

              case 1 =>
                ATLAS_SIZE = env.u_ref_textureSizeAtlasSize.zw
                TEXTURE_SIZE = env.u_ref_textureSizeAtlasSize.xy
                SIZE = env.u_ref_sizeAndFrameScale.xy
                FRAME_SIZE = env.u_ref_sizeAndFrameScale.zw
                REF = env.u_ref_refFlip.xy
                FLIP = env.u_ref_refFlip.zw
                CHANNEL_0_ATLAS_OFFSET = env.u_ref_channelOffsets01.xy
                CHANNEL_1_ATLAS_OFFSET = env.u_ref_channelOffsets01.zw
                CHANNEL_2_ATLAS_OFFSET = env.u_ref_channelOffsets23.xy
                CHANNEL_3_ATLAS_OFFSET = env.u_ref_channelOffsets23.zw

              case 2 =>
                ATLAS_SIZE = env.u_ref_textureSizeAtlasSize.zw
                TEXTURE_SIZE = env.u_ref_textureSizeAtlasSize.xy
                SIZE = a_sizeAndFrameScale.xy
                FRAME_SIZE = a_sizeAndFrameScale.zw
                REF = env.u_ref_refFlip.xy
                FLIP = env.u_ref_refFlip.zw
                CHANNEL_0_ATLAS_OFFSET = a_channelOffsets01.xy
                CHANNEL_1_ATLAS_OFFSET = a_channelOffsets01.zw
                CHANNEL_2_ATLAS_OFFSET = a_channelOffsets23.xy
                CHANNEL_3_ATLAS_OFFSET = a_channelOffsets23.zw

              case _ =>
                ()
            

          vertex();

          CHANNEL_0_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_0_ATLAS_OFFSET)
          CHANNEL_1_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_1_ATLAS_OFFSET)
          CHANNEL_2_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_2_ATLAS_OFFSET)
          CHANNEL_3_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_3_ATLAS_OFFSET)
          CHANNEL_0_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_0_ATLAS_OFFSET)
          CHANNEL_1_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_1_ATLAS_OFFSET)
          CHANNEL_2_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_2_ATLAS_OFFSET)
          CHANNEL_3_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_3_ATLAS_OFFSET)
          CHANNEL_0_SIZE = TEXTURE_SIZE / ATLAS_SIZE;

          val transform: mat4 = 
            translate2d(POSITION) *
            rotate2d(-1.0f * ROTATION) *
            scale2d(SIZE * SCALE) *
            translate2d(-(REF / SIZE) + 0.5f) *
            scale2d(vec2(1.0f, -1.0f) * FLIP)

          env.gl_Position = env.u_projection * u_baseTransform * transform * VERTEX

          val screenCoords: vec2 = env.gl_Position.xy * 0.5f + 0.5f
          v_screenCoordsRotation = vec3(vec2(screenCoords.x, 1.0f - screenCoords.y) * env.VIEWPORT_SIZE, ROTATION)

          v_uv_size = vec4(UV, SIZE)
          v_channel_coords_01 = vec4(CHANNEL_0_TEXTURE_COORDS, CHANNEL_1_TEXTURE_COORDS)
          v_channel_coords_23 = vec4(CHANNEL_2_TEXTURE_COORDS, CHANNEL_3_TEXTURE_COORDS)
          v_textureSize = TEXTURE_SIZE
          v_atlasSizeAsUV = vec4(ATLAS_SIZE, CHANNEL_0_SIZE)
          v_channel_pos_01 = vec4(CHANNEL_0_POSITION, CHANNEL_1_POSITION)
          v_channel_pos_23 = vec4(CHANNEL_2_POSITION, CHANNEL_3_POSITION)
          v_instanceId = INSTANCE_ID
        
      }

    val output = shader.toGLSL[Indigo]

    val expected: String =
      """
      |#version 300 es
      |precision mediump float;
      |layout (location = 0) in vec4 a_verticesAndCoords;
      |layout (location = 1) in vec4 a_translateScale;
      |layout (location = 2) in vec4 a_refFlip;
      |layout (location = 3) in vec4 a_sizeAndFrameScale;
      |layout (location = 4) in vec4 a_channelOffsets01;
      |layout (location = 5) in vec4 a_channelOffsets23;
      |layout (location = 6) in vec4 a_textureSizeAtlasSize;
      |layout (location = 7) in float a_rotation;
      |layout (std140) uniform IndigoProjectionData {
      |  mat4 u_projection;
      |};
      |layout (std140) uniform IndigoFrameData {
      |  highp float TIME;
      |  vec2 VIEWPORT_SIZE;
      |};
      |layout (std140) uniform IndigoCloneReferenceData {
      |  vec4 u_ref_refFlip;
      |  vec4 u_ref_sizeAndFrameScale;
      |  vec4 u_ref_channelOffsets01;
      |  vec4 u_ref_channelOffsets23;
      |  vec4 u_ref_textureSizeAtlasSize;
      |};
      |uniform mat4 u_baseTransform;
      |uniform int u_mode;
      |out vec4 v_channel_coords_01;
      |out vec4 v_channel_coords_23;
      |out vec4 v_uv_size;
      |out vec3 v_screenCoordsRotation;
      |out vec2 v_textureSize;
      |out vec4 v_atlasSizeAsUV;
      |out vec4 v_channel_pos_01;
      |out vec4 v_channel_pos_23;
      |flat out int v_instanceId;
      |const float PI=3.1415927;
      |const float PI_2=PI*0.5;
      |const float PI_4=PI*0.25;
      |const float TAU=2.0*PI;
      |const float TAU_2=PI;
      |const float TAU_4=PI_2;
      |const float TAU_8=PI_4;
      |vec2 ATLAS_SIZE;
      |vec4 VERTEX;
      |vec2 TEXTURE_SIZE;
      |vec2 UV;
      |vec2 SIZE;
      |vec2 FRAME_SIZE;
      |vec2 CHANNEL_0_ATLAS_OFFSET;
      |vec2 CHANNEL_1_ATLAS_OFFSET;
      |vec2 CHANNEL_2_ATLAS_OFFSET;
      |vec2 CHANNEL_3_ATLAS_OFFSET;
      |vec2 CHANNEL_0_TEXTURE_COORDS;
      |vec2 CHANNEL_1_TEXTURE_COORDS;
      |vec2 CHANNEL_2_TEXTURE_COORDS;
      |vec2 CHANNEL_3_TEXTURE_COORDS;
      |vec2 CHANNEL_0_POSITION;
      |vec2 CHANNEL_1_POSITION;
      |vec2 CHANNEL_2_POSITION;
      |vec2 CHANNEL_3_POSITION;
      |vec2 CHANNEL_0_SIZE;
      |vec2 POSITION;
      |vec2 SCALE;
      |vec2 REF;
      |vec2 FLIP;
      |float ROTATION=0.0;
      |int INSTANCE_ID=0;
      |mat4 translate2d(in vec2 t){
      |  return mat4(1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,t.x,t.y,0.0,1.0);
      |}
      |mat4 scale2d(in vec2 s){
      |  return mat4(s.x,0.0,0.0,0.0,0.0,s.y,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0);
      |}
      |mat4 rotate2d(in float angle){
      |  return mat4(cos(angle),-sin(angle),0.0,0.0,sin(angle),cos(angle),0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0);
      |}
      |vec2 scaleCoordsWithOffset(in vec2 texcoord,in vec2 offset){
      |  mat4 transform=translate2d(offset)*scale2d(FRAME_SIZE);
      |  return (transform*vec4(texcoord,1.0,1.0)).xy;
      |}
      |void vertex(){
      |}
      |void main(){
      |  INSTANCE_ID=gl_InstanceID;
      |  VERTEX=vec4(a_verticesAndCoords.xy,1.0,1.0);
      |  UV=a_verticesAndCoords.zw;
      |  ROTATION=a_rotation;
      |  POSITION=a_translateScale.xy;
      |  SCALE=a_translateScale.zw;
      |  switch(u_mode){
      |    case 0:
      |      ATLAS_SIZE=a_textureSizeAtlasSize.zw;
      |      TEXTURE_SIZE=a_textureSizeAtlasSize.xy;
      |      SIZE=a_sizeAndFrameScale.xy;
      |      FRAME_SIZE=a_sizeAndFrameScale.zw;
      |      REF=a_refFlip.xy;
      |      FLIP=a_refFlip.zw;
      |      CHANNEL_0_ATLAS_OFFSET=a_channelOffsets01.xy;
      |      CHANNEL_1_ATLAS_OFFSET=a_channelOffsets01.zw;
      |      CHANNEL_2_ATLAS_OFFSET=a_channelOffsets23.xy;
      |      CHANNEL_3_ATLAS_OFFSET=a_channelOffsets23.zw;
      |      break;
      |    case 1:
      |      ATLAS_SIZE=u_ref_textureSizeAtlasSize.zw;
      |      TEXTURE_SIZE=u_ref_textureSizeAtlasSize.xy;
      |      SIZE=u_ref_sizeAndFrameScale.xy;
      |      FRAME_SIZE=u_ref_sizeAndFrameScale.zw;
      |      REF=u_ref_refFlip.xy;
      |      FLIP=u_ref_refFlip.zw;
      |      CHANNEL_0_ATLAS_OFFSET=u_ref_channelOffsets01.xy;
      |      CHANNEL_1_ATLAS_OFFSET=u_ref_channelOffsets01.zw;
      |      CHANNEL_2_ATLAS_OFFSET=u_ref_channelOffsets23.xy;
      |      CHANNEL_3_ATLAS_OFFSET=u_ref_channelOffsets23.zw;
      |      break;
      |    case 2:
      |      ATLAS_SIZE=u_ref_textureSizeAtlasSize.zw;
      |      TEXTURE_SIZE=u_ref_textureSizeAtlasSize.xy;
      |      SIZE=a_sizeAndFrameScale.xy;
      |      FRAME_SIZE=a_sizeAndFrameScale.zw;
      |      REF=u_ref_refFlip.xy;
      |      FLIP=u_ref_refFlip.zw;
      |      CHANNEL_0_ATLAS_OFFSET=a_channelOffsets01.xy;
      |      CHANNEL_1_ATLAS_OFFSET=a_channelOffsets01.zw;
      |      CHANNEL_2_ATLAS_OFFSET=a_channelOffsets23.xy;
      |      CHANNEL_3_ATLAS_OFFSET=a_channelOffsets23.zw;
      |      break;
      |    default:
      |      break;
      |  }
      |}
      |vertex();
      |CHANNEL_0_TEXTURE_COORDS=scaleCoordsWithOffset(UV,CHANNEL_0_ATLAS_OFFSET);
      |CHANNEL_1_TEXTURE_COORDS=scaleCoordsWithOffset(UV,CHANNEL_1_ATLAS_OFFSET);
      |CHANNEL_2_TEXTURE_COORDS=scaleCoordsWithOffset(UV,CHANNEL_2_ATLAS_OFFSET);
      |CHANNEL_3_TEXTURE_COORDS=scaleCoordsWithOffset(UV,CHANNEL_3_ATLAS_OFFSET);
      |CHANNEL_0_POSITION=scaleCoordsWithOffset(vec2(0.0),CHANNEL_0_ATLAS_OFFSET);
      |CHANNEL_1_POSITION=scaleCoordsWithOffset(vec2(0.0),CHANNEL_1_ATLAS_OFFSET);
      |CHANNEL_2_POSITION=scaleCoordsWithOffset(vec2(0.0),CHANNEL_2_ATLAS_OFFSET);
      |CHANNEL_3_POSITION=scaleCoordsWithOffset(vec2(0.0),CHANNEL_3_ATLAS_OFFSET);
      |CHANNEL_0_SIZE=TEXTURE_SIZE/ATLAS_SIZE;
      |mat4 transform=(translate2d(POSITION)*(rotate2d((-1.0)*ROTATION)))*(scale2d(SIZE*SCALE))*(translate2d((-(REF/SIZE))+0.5))*(scale2d((vec2(1.0,-1.0))*FLIP));
      |gl_Position=((u_projection*u_baseTransform)*transform)*VERTEX;
      |vec2 screenCoords=(gl_Position.xy*0.5)+0.5;
      |v_screenCoordsRotation=vec3((vec2(screenCoords.x,1.0-screenCoords.y))*VIEWPORT_SIZE,ROTATION);
      |v_uv_size=vec4(UV,SIZE);
      |v_channel_coords_01=vec4(CHANNEL_0_TEXTURE_COORDS,CHANNEL_1_TEXTURE_COORDS);
      |v_channel_coords_23=vec4(CHANNEL_2_TEXTURE_COORDS,CHANNEL_3_TEXTURE_COORDS);
      |v_textureSize=TEXTURE_SIZE;
      |v_atlasSizeAsUV=vec4(ATLAS_SIZE,CHANNEL_0_SIZE);
      |v_channel_pos_01=vec4(CHANNEL_0_POSITION,CHANNEL_1_POSITION);
      |v_channel_pos_23=vec4(CHANNEL_2_POSITION,CHANNEL_3_POSITION);
      |v_instanceId=INSTANCE_ID;
      |""".stripMargin.trim

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  object fragment:
    inline def shader =
      Shader {
        Version300ES
        PrecisionMediumPFloat
      }

    val output = shader.toGLSL[Indigo]

    val expected: String =
      """
      |x
      |""".stripMargin.trim
