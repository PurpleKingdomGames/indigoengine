package com.example.sandbox.shaders

import indigo.*

import scala.annotation.nowarn

object CircleShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("circle shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.sdf.*

      def calculateColour(uv: vec2, sdf: Float): vec4 =
        val fill       = vec4(uv, 0.0f, 1.0f)
        val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
        vec4(fill.xyz * fillAmount, fillAmount)

      def fragment(color: vec4): vec4 =
        calculateColour(env.UV, circle(env.UV - 0.5f, 0.5f))
    }

object BoxShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("box shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.sdf.*

      def proxy: (vec2, vec2) => Float =
        (p, b) => box(p, b)

      def calculateColour(uv: vec2, sdf: Float): vec4 =
        val fill       = vec4(uv, 0.0f, 1.0f)
        val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
        vec4(fill.xyz * fillAmount, fillAmount)

      def fragment(color: vec4): vec4 =
        calculateColour(env.UV, proxy(env.UV - 0.5f, vec2(0.4f, 0.6f)))
    }

object StarShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("star shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.sdf.*

      def proxy: (vec2, Float, Float) => Float =
        (p, rt, r) => star(p, rt, r)

      def calculateColour(uv: vec2, sdf: Float): vec4 =
        val fill       = vec4(uv, 0.0f, 1.0f)
        val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
        vec4(fill.xyz * fillAmount, fillAmount)

      def fragment(color: vec4): vec4 =
        calculateColour(env.UV, proxy(env.UV - 0.5f, 0.3f, 0.6f))
    }
