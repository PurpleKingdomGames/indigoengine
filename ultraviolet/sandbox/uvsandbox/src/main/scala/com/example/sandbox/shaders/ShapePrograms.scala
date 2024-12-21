package com.example.sandbox.shaders

import indigo.*

import scala.annotation.nowarn

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
      import FillColorHelper.*

      def proxy: (vec2, vec2) => Float =
        (p, b) => box(p, b)

      def calculateColour: (vec2, Float) => vec4 = (uv, sdf) => fill(uv, sdf)

      def fragment(color: vec4): vec4 =
        calculateColour(env.UV, proxy(env.UV - 0.5f, vec2(0.2f, 0.4f)))
    }

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
      import FillColorHelper.*

      def calculateColour: (vec2, Float) => vec4 = (uv, sdf) => fill(uv, sdf)

      def fragment(color: vec4): vec4 =
        calculateColour(env.UV, circle(env.UV - 0.5f, 0.4f))
    }

object HexagonShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("hexagon shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.sdf.*
      import FillColorHelper.*

      def proxy: (vec2, Float) => Float =
        (p, r) => hexagon(p, r)

      def calculateColour: (vec2, Float) => vec4 = (uv, sdf) => fill(uv, sdf)

      def fragment(color: vec4): vec4 =
        calculateColour(env.UV, proxy(env.UV - 0.5f, 0.4f))
    }

object SegmentShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("segment shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.sdf.*
      import FillColorHelper.*

      def proxy: (vec2, vec2, vec2) => Float =
        (p, a, b) => segment(p, a, b)

      def calculateColour: (vec2, Float) => vec4 = (uv, sdf) => fill(uv, sdf)

      def fragment(color: vec4): vec4 =
        // We can't just render the line segment, because at 0.0f the SDF is 0.0f,
        // so we need the annular, i.e. abs(sdf) - thickness-of-border.
        val segSDF = proxy(env.UV, vec2(0.2f), vec2(0.8f))
        calculateColour(env.UV, abs(segSDF) - 0.1f)
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
      import FillColorHelper.*

      def proxy: (vec2, Float, Float) => Float =
        (p, rt, r) => star(p, rt, r)

      def calculateColour: (vec2, Float) => vec4 = (uv, sdf) => fill(uv, sdf)

      def fragment(color: vec4): vec4 =
        calculateColour(env.UV, proxy(env.UV - 0.5f, 0.3f, 0.6f))
    }

object TriangleShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("triangle shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.sdf.*
      import FillColorHelper.*

      def proxy: (vec2, Float) => Float =
        (p, r) => triangle(p, r)

      def calculateColour: (vec2, Float) => vec4 = (uv, sdf) => fill(uv, sdf)

      def fragment(color: vec4): vec4 =
        calculateColour(env.UV, proxy(env.UV - 0.5f, 0.4f))
    }

object FillColorHelper:

  import ultraviolet.syntax.*
  import ultraviolet.colors.*

  inline def fill(uv: vec2, sdf: Float): vec4 =
    val fill       = vec4(uv, Blue.z, 1.0f)
    val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
    vec4(fill.xyz * fillAmount, fillAmount)
