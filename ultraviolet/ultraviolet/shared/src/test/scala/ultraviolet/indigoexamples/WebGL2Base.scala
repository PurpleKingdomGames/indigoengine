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

  case class IndigoDynamicLightingData(
      numOfLights: Float,
      lightFlags: highp[array[8, vec4]], // vec4(active, type, far cut off, falloff type)
      lightColor: array[8, vec4],
      lightSpecular: array[8, vec4],
      lightPositionRotation: array[8, vec4],     // vec4(x, y, rotation, ???)
      lightNearFarAngleIntensity: array[8, vec4] // vec4(near, far, angle, intensity)
  )

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  object vertex:
    inline def shader =
      Shader[GLEnv & VertEnv & IndigoFrameData & IndigoProjectionData & IndigoCloneReferenceData] { env =>
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
        @out var v_channel_pos_01: vec4  = null // Position on the atlas of channels 0 and 1.
        @out var v_channel_pos_23: vec4  = null // Position on the atlas of channels 2 and 3.
        @flat @out var v_instanceId: Int = 0    // The current instance id
        // flat out int v_instanceId // The current instance id

        // Constants
        @const val PI: Float    = 3.141592653589793f
        @const val PI_2: Float  = PI * 0.5f
        @const val PI_4: Float  = PI * 0.25f
        @const val TAU: Float   = 2.0f * PI
        @const val TAU_2: Float = PI
        @const val TAU_4: Float = PI_2
        @const val TAU_8: Float = PI_4

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
            

          vertex()

          CHANNEL_0_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_0_ATLAS_OFFSET)
          CHANNEL_1_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_1_ATLAS_OFFSET)
          CHANNEL_2_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_2_ATLAS_OFFSET)
          CHANNEL_3_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_3_ATLAS_OFFSET)
          CHANNEL_0_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_0_ATLAS_OFFSET)
          CHANNEL_1_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_1_ATLAS_OFFSET)
          CHANNEL_2_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_2_ATLAS_OFFSET)
          CHANNEL_3_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_3_ATLAS_OFFSET)
          CHANNEL_0_SIZE = TEXTURE_SIZE / ATLAS_SIZE

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

    val output = shader.toGLSLDefaultHeaders[Indigo]

    val expected: String =
      """
      |#version 300 es
      |precision mediump float;
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
      |layout (location = 0) in vec4 a_verticesAndCoords;
      |layout (location = 1) in vec4 a_translateScale;
      |layout (location = 2) in vec4 a_refFlip;
      |layout (location = 3) in vec4 a_sizeAndFrameScale;
      |layout (location = 4) in vec4 a_channelOffsets01;
      |layout (location = 5) in vec4 a_channelOffsets23;
      |layout (location = 6) in vec4 a_textureSizeAtlasSize;
      |layout (location = 7) in float a_rotation;
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
      |void vertex(){}
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
      |  vertex();
      |  CHANNEL_0_TEXTURE_COORDS=scaleCoordsWithOffset(UV,CHANNEL_0_ATLAS_OFFSET);
      |  CHANNEL_1_TEXTURE_COORDS=scaleCoordsWithOffset(UV,CHANNEL_1_ATLAS_OFFSET);
      |  CHANNEL_2_TEXTURE_COORDS=scaleCoordsWithOffset(UV,CHANNEL_2_ATLAS_OFFSET);
      |  CHANNEL_3_TEXTURE_COORDS=scaleCoordsWithOffset(UV,CHANNEL_3_ATLAS_OFFSET);
      |  CHANNEL_0_POSITION=scaleCoordsWithOffset(vec2(0.0),CHANNEL_0_ATLAS_OFFSET);
      |  CHANNEL_1_POSITION=scaleCoordsWithOffset(vec2(0.0),CHANNEL_1_ATLAS_OFFSET);
      |  CHANNEL_2_POSITION=scaleCoordsWithOffset(vec2(0.0),CHANNEL_2_ATLAS_OFFSET);
      |  CHANNEL_3_POSITION=scaleCoordsWithOffset(vec2(0.0),CHANNEL_3_ATLAS_OFFSET);
      |  CHANNEL_0_SIZE=TEXTURE_SIZE/ATLAS_SIZE;
      |  mat4 transform=(translate2d(POSITION)*(rotate2d((-1.0)*ROTATION)))*(scale2d(SIZE*SCALE))*(translate2d((-(REF/SIZE))+0.5))*(scale2d((vec2(1.0,-1.0))*FLIP));
      |  gl_Position=((u_projection*u_baseTransform)*transform)*VERTEX;
      |  vec2 screenCoords=(gl_Position.xy*0.5)+0.5;
      |  v_screenCoordsRotation=vec3((vec2(screenCoords.x,1.0-screenCoords.y))*VIEWPORT_SIZE,ROTATION);
      |  v_uv_size=vec4(UV,SIZE);
      |  v_channel_coords_01=vec4(CHANNEL_0_TEXTURE_COORDS,CHANNEL_1_TEXTURE_COORDS);
      |  v_channel_coords_23=vec4(CHANNEL_2_TEXTURE_COORDS,CHANNEL_3_TEXTURE_COORDS);
      |  v_textureSize=TEXTURE_SIZE;
      |  v_atlasSizeAsUV=vec4(ATLAS_SIZE,CHANNEL_0_SIZE);
      |  v_channel_pos_01=vec4(CHANNEL_0_POSITION,CHANNEL_1_POSITION);
      |  v_channel_pos_23=vec4(CHANNEL_2_POSITION,CHANNEL_3_POSITION);
      |  v_instanceId=INSTANCE_ID;
      |}
      |""".stripMargin.trim

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  object fragment:
    inline def shader =
      Shader[IndigoDynamicLightingData] { env =>
        @layout(0) @out var fragColor: vec4 = null

        // ** Uniforms **
        // Currently we only ever bind one texture at a time.
        // The texture is however an atlas of textures, so in
        // practice you can read many sub-textures at once.
        // Could remove this limitation.
        @uniform val SRC_CHANNEL: sampler2D.type = sampler2D

        // public
        ubo[IndigoFrameData]
        ubo[IndigoDynamicLightingData]

        // ** Varyings **
        @in var v_channel_coords_01: vec4 = null
        @in var v_channel_coords_23: vec4 = null
        @in var v_uv_size: vec4 = null // Unscaled texture coordinates + Width / height of the objects
        @in var v_screenCoordsRotation: vec3 = null // Where is this pixel on the screen?
        @in var v_textureSize: vec2 = null // Actual size of the texture in pixels.
        @in var v_atlasSizeAsUV: vec4 = null // Actual size of the atlas in pixels, and it's relative size in UV coords.
        @in var v_channel_pos_01: vec4 = null // Position on the atlas of channels 0 and 1.
        @in var v_channel_pos_23: vec4 = null // Position on the atlas of channels 2 and 3.
        @flat @in var v_instanceId: Int = 0 // The current instance id

        // Variables
        var UV: vec2 = null // Unscaled texture coordinates
        var SIZE: vec2 = null // Width / height of the objects
        var CHANNEL_0: vec4 = null // Pixel value from texture channel 0
        var CHANNEL_1: vec4 = null // Pixel value from texture channel 1
        var CHANNEL_2: vec4 = null // Pixel value from texture channel 2
        var CHANNEL_3: vec4 = null // Pixel value from texture channel 3
        var CHANNEL_0_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
        var CHANNEL_1_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
        var CHANNEL_2_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
        var CHANNEL_3_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
        var CHANNEL_0_POSITION: vec2 = null // top left position of this texture on the atlas in UV coords
        var CHANNEL_1_POSITION: vec2 = null // top left position of this texture on the atlas in UV coords
        var CHANNEL_2_POSITION: vec2 = null // top left position of this texture on the atlas in UV coords
        var CHANNEL_3_POSITION: vec2 = null // top left position of this texture on the atlas in UV coords
        var CHANNEL_0_SIZE: vec2 = null // size of this texture on the atlas in UV coords
        var SCREEN_COORDS: vec2 = null
        var ROTATION: Float = 0.0f
        var TEXTURE_SIZE: vec2 = null // Size of the texture in pixels
        var ATLAS_SIZE: vec2 = null // Size of the atlas this texture is on, in pixels
        var INSTANCE_ID: Int = 0 // The current instance id

        var LIGHT_INDEX: Int = 0
        var LIGHT_COUNT: Int = 0
        var LIGHT_ACTIVE: Int = 0
        var LIGHT_TYPE: Int = 0
        var LIGHT_FAR_CUT_OFF: Int = 0
        var LIGHT_FALLOFF_TYPE: Int = 0
        var LIGHT_COLOR: vec4 = null
        var LIGHT_SPECULAR: vec4 = null
        var LIGHT_POSITION: vec2 = null
        var LIGHT_ROTATION: Float = 0.0f
        var LIGHT_NEAR: Float = 0.0f
        var LIGHT_FAR: Float = 0.0f
        var LIGHT_ANGLE: Float = 0.0f
        var LIGHT_INTENSITY: Float = 0.0f

        // Constants
        @const val PI: Float    = 3.141592653589793f
        @const val PI_2: Float  = PI * 0.5f
        @const val PI_4: Float  = PI * 0.25f
        @const val TAU: Float   = 2.0f * PI
        @const val TAU_2: Float = PI
        @const val TAU_4: Float = PI_2
        @const val TAU_8: Float = PI_4

        // Outputs
        var COLOR: vec4 = null

        //#fragment_start
        def fragment(): Unit = ()
        //#fragment_end

        //#prepare_start
        def prepare(): Unit = ()
        //#prepare_end

        //#light_start
        def light(): Unit = ()
        //#light_end

        //#composite_start
        def composite(): Unit = ()
        //#composite_end

        def main: Unit =

          INSTANCE_ID = v_instanceId

          // Defaults
          UV = v_uv_size.xy
          SIZE = v_uv_size.zw
          COLOR = vec4(0.0f)

          SCREEN_COORDS = v_screenCoordsRotation.xy
          ROTATION = v_screenCoordsRotation.z
          TEXTURE_SIZE = v_textureSize
          ATLAS_SIZE = v_atlasSizeAsUV.xy
          CHANNEL_0_POSITION = v_channel_pos_01.xy
          CHANNEL_1_POSITION = v_channel_pos_01.zw
          CHANNEL_2_POSITION = v_channel_pos_23.xy
          CHANNEL_3_POSITION = v_channel_pos_23.zw
          CHANNEL_0_SIZE = v_atlasSizeAsUV.zw

          CHANNEL_0_TEXTURE_COORDS = min(v_channel_coords_01.xy, CHANNEL_0_POSITION + CHANNEL_0_SIZE)
          CHANNEL_1_TEXTURE_COORDS = min(v_channel_coords_01.zw, CHANNEL_1_POSITION + CHANNEL_0_SIZE)
          CHANNEL_2_TEXTURE_COORDS = min(v_channel_coords_23.xy, CHANNEL_2_POSITION + CHANNEL_0_SIZE)
          CHANNEL_3_TEXTURE_COORDS = min(v_channel_coords_23.zw, CHANNEL_3_POSITION + CHANNEL_0_SIZE)
          CHANNEL_0 = texture2D(SRC_CHANNEL, CHANNEL_0_TEXTURE_COORDS)
          CHANNEL_1 = texture2D(SRC_CHANNEL, CHANNEL_1_TEXTURE_COORDS)
          CHANNEL_2 = texture2D(SRC_CHANNEL, CHANNEL_2_TEXTURE_COORDS)
          CHANNEL_3 = texture2D(SRC_CHANNEL, CHANNEL_3_TEXTURE_COORDS)

          // Colour - build up the COLOR
          fragment()

          // Lighting - prepare, light, composite
          prepare()

          LIGHT_COUNT = min(8, max(0, round(env.numOfLights).toInt))
          
          _for(0, _ < LIGHT_COUNT, _ + 1) { i =>
            LIGHT_INDEX = i
            LIGHT_ACTIVE = round(env.lightFlags(i).x).toInt
            LIGHT_TYPE = round(env.lightFlags(i).y).toInt
            LIGHT_FAR_CUT_OFF = round(env.lightFlags(i).z).toInt
            LIGHT_FALLOFF_TYPE = round(env.lightFlags(i).w).toInt
            LIGHT_COLOR = env.lightColor(i)
            LIGHT_SPECULAR = env.lightSpecular(i)
            LIGHT_POSITION = env.lightPositionRotation(i).xy
            LIGHT_ROTATION = env.lightPositionRotation(i).z
            LIGHT_NEAR = env.lightNearFarAngleIntensity(i).x
            LIGHT_FAR = env.lightNearFarAngleIntensity(i).y
            LIGHT_ANGLE = env.lightNearFarAngleIntensity(i).z
            LIGHT_INTENSITY = env.lightNearFarAngleIntensity(i).w

            light()
          }

          // Composite - COMBINE COLOR + Lighting into final pixel color.
          composite()
          
          fragColor = COLOR
        }

    val output = shader.toGLSLDefaultHeaders[Indigo]

    val expected: String =
      """
      |#version 300 es
      |precision mediump float;
      |layout (std140) uniform IndigoFrameData {
      |  highp float TIME;
      |  vec2 VIEWPORT_SIZE;
      |};
      |layout (std140) uniform IndigoDynamicLightingData {
      |  float numOfLights;
      |  highp vec4[8] lightFlags;
      |  vec4[8] lightColor;
      |  vec4[8] lightSpecular;
      |  vec4[8] lightPositionRotation;
      |  vec4[8] lightNearFarAngleIntensity;
      |};
      |layout (location = 0) out vec4 fragColor;
      |uniform sampler2D SRC_CHANNEL;
      |in vec4 v_channel_coords_01;
      |in vec4 v_channel_coords_23;
      |in vec4 v_uv_size;
      |in vec3 v_screenCoordsRotation;
      |in vec2 v_textureSize;
      |in vec4 v_atlasSizeAsUV;
      |in vec4 v_channel_pos_01;
      |in vec4 v_channel_pos_23;
      |flat in int v_instanceId;
      |const float PI=3.1415927;
      |const float PI_2=PI*0.5;
      |const float PI_4=PI*0.25;
      |const float TAU=2.0*PI;
      |const float TAU_2=PI;
      |const float TAU_4=PI_2;
      |const float TAU_8=PI_4;
      |vec2 UV;
      |vec2 SIZE;
      |vec4 CHANNEL_0;
      |vec4 CHANNEL_1;
      |vec4 CHANNEL_2;
      |vec4 CHANNEL_3;
      |vec2 CHANNEL_0_TEXTURE_COORDS;
      |vec2 CHANNEL_1_TEXTURE_COORDS;
      |vec2 CHANNEL_2_TEXTURE_COORDS;
      |vec2 CHANNEL_3_TEXTURE_COORDS;
      |vec2 CHANNEL_0_POSITION;
      |vec2 CHANNEL_1_POSITION;
      |vec2 CHANNEL_2_POSITION;
      |vec2 CHANNEL_3_POSITION;
      |vec2 CHANNEL_0_SIZE;
      |vec2 SCREEN_COORDS;
      |float ROTATION=0.0;
      |vec2 TEXTURE_SIZE;
      |vec2 ATLAS_SIZE;
      |int INSTANCE_ID=0;
      |int LIGHT_INDEX=0;
      |int LIGHT_COUNT=0;
      |int LIGHT_ACTIVE=0;
      |int LIGHT_TYPE=0;
      |int LIGHT_FAR_CUT_OFF=0;
      |int LIGHT_FALLOFF_TYPE=0;
      |vec4 LIGHT_COLOR;
      |vec4 LIGHT_SPECULAR;
      |vec2 LIGHT_POSITION;
      |float LIGHT_ROTATION=0.0;
      |float LIGHT_NEAR=0.0;
      |float LIGHT_FAR=0.0;
      |float LIGHT_ANGLE=0.0;
      |float LIGHT_INTENSITY=0.0;
      |vec4 COLOR;
      |void fragment(){}
      |void prepare(){}
      |void light(){}
      |void composite(){}
      |void main(){
      |  INSTANCE_ID=v_instanceId;
      |  UV=v_uv_size.xy;
      |  SIZE=v_uv_size.zw;
      |  COLOR=vec4(0.0);
      |  SCREEN_COORDS=v_screenCoordsRotation.xy;
      |  ROTATION=v_screenCoordsRotation.z;
      |  TEXTURE_SIZE=v_textureSize;
      |  ATLAS_SIZE=v_atlasSizeAsUV.xy;
      |  CHANNEL_0_POSITION=v_channel_pos_01.xy;
      |  CHANNEL_1_POSITION=v_channel_pos_01.zw;
      |  CHANNEL_2_POSITION=v_channel_pos_23.xy;
      |  CHANNEL_3_POSITION=v_channel_pos_23.zw;
      |  CHANNEL_0_SIZE=v_atlasSizeAsUV.zw;
      |  CHANNEL_0_TEXTURE_COORDS=min(v_channel_coords_01.xy,CHANNEL_0_POSITION+CHANNEL_0_SIZE);
      |  CHANNEL_1_TEXTURE_COORDS=min(v_channel_coords_01.zw,CHANNEL_1_POSITION+CHANNEL_0_SIZE);
      |  CHANNEL_2_TEXTURE_COORDS=min(v_channel_coords_23.xy,CHANNEL_2_POSITION+CHANNEL_0_SIZE);
      |  CHANNEL_3_TEXTURE_COORDS=min(v_channel_coords_23.zw,CHANNEL_3_POSITION+CHANNEL_0_SIZE);
      |  CHANNEL_0=texture(SRC_CHANNEL,CHANNEL_0_TEXTURE_COORDS);
      |  CHANNEL_1=texture(SRC_CHANNEL,CHANNEL_1_TEXTURE_COORDS);
      |  CHANNEL_2=texture(SRC_CHANNEL,CHANNEL_2_TEXTURE_COORDS);
      |  CHANNEL_3=texture(SRC_CHANNEL,CHANNEL_3_TEXTURE_COORDS);
      |  fragment();
      |  prepare();
      |  LIGHT_COUNT=min(8,max(0,int(round(numOfLights))));
      |  for(int i=0;i<LIGHT_COUNT;i=i+1){
      |    LIGHT_INDEX=i;
      |    LIGHT_ACTIVE=int(round(lightFlags[i].x));
      |    LIGHT_TYPE=int(round(lightFlags[i].y));
      |    LIGHT_FAR_CUT_OFF=int(round(lightFlags[i].z));
      |    LIGHT_FALLOFF_TYPE=int(round(lightFlags[i].w));
      |    LIGHT_COLOR=lightColor[i];
      |    LIGHT_SPECULAR=lightSpecular[i];
      |    LIGHT_POSITION=lightPositionRotation[i].xy;
      |    LIGHT_ROTATION=lightPositionRotation[i].z;
      |    LIGHT_NEAR=lightNearFarAngleIntensity[i].x;
      |    LIGHT_FAR=lightNearFarAngleIntensity[i].y;
      |    LIGHT_ANGLE=lightNearFarAngleIntensity[i].z;
      |    LIGHT_INTENSITY=lightNearFarAngleIntensity[i].w;
      |    light();
      |  }
      |  composite();
      |  fragColor=COLOR;
      |}
      |""".stripMargin.trim
