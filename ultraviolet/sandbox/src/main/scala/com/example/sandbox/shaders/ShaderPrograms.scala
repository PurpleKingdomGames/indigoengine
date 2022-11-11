package com.example.sandbox.shaders

import ultraviolet.syntax.*

object ShaderPrograms:

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  final case class FragEnv(UV: vec2, var COLOR: vec4)

  inline def fragment1: Shader[FragEnv, vec4] =
    Shader { env =>
      val zero  = 0.0f
      val alpha = 1.0f
      vec4(env.UV, zero, alpha)
    }

  val frag1: String = fragment2.toGLSL

  inline def fragment2: Shader[FragEnv, Unit] =
    Shader { env =>
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

  val frag2: String = fragment2.toGLSL
