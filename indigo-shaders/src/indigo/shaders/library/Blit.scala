package indigo.shaders.library

import ultraviolet.syntax.*

import scala.annotation.nowarn

object Blit:

  trait Env extends Lighting.LightEnv {
    val FILLTYPE: highp[Float]         = 0.0f
    val NINE_SLICE_CENTER: highp[vec4] = vec4(0.0f)
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoBitmapData(
      FILLTYPE: highp[Float],
      NINE_SLICE_CENTER: highp[vec4]
  )

  @nowarn("msg=unused")
  inline def fragment =
    Shader[Env] { env =>
      import TileAndStretch.*

      // Delegates
      val _tileAndStretchChannel: (Int, sampler2D.type, vec2, vec2, vec2, vec2, vec2, vec4) => vec4 =
        tileAndStretchChannel

      ubo[IndigoBitmapData]

      def fragment(color: vec4): vec4 =
        env.CHANNEL_0 = _tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.SRC_CHANNEL,
          env.CHANNEL_0_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE,
          env.NINE_SLICE_CENTER
        )
        env.CHANNEL_1 = _tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.SRC_CHANNEL,
          env.CHANNEL_1_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE,
          env.NINE_SLICE_CENTER
        )
        env.CHANNEL_2 = _tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.SRC_CHANNEL,
          env.CHANNEL_2_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE,
          env.NINE_SLICE_CENTER
        )
        env.CHANNEL_3 = _tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.SRC_CHANNEL,
          env.CHANNEL_3_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE,
          env.NINE_SLICE_CENTER
        )

        env.CHANNEL_0
    }
