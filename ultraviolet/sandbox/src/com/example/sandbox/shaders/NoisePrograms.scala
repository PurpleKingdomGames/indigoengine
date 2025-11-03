package com.example.sandbox.shaders

import indigo.*

import scala.annotation.nowarn

object CellularNoiseShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("cellular noise shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.noise.*

      def proxy: vec2 => vec2 =
        pt => cellular(pt)

      def fragment(color: vec4): vec4 =
        vec4(proxy(env.UV * 8.0f), 0.0f, 1.0f)
    }

object PerlinNoiseShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("perlin noise shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.noise.*

      def proxy: vec2 => Float =
        p => perlin(p)

      def fragment(color: vec4): vec4 =
        vec4(vec3(proxy(env.UV * 8.0f)), 1.0f)
    }

object GradientNoiseShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("gradient noise shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.noise.*

      def proxy: vec2 => vec3 =
        p => gradient(p)

      def fragment(color: vec4): vec4 =
        vec4(proxy(env.UV * 8.0f), 1.0f)
    }

object SimplexNoiseShader:

  val shader: UltravioletShader =
    UltravioletShader.entityFragment(
      ShaderId("simplex noise shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.noise.*

      def proxy: vec2 => Float =
        pt => simplex(pt)

      def fragment(color: vec4): vec4 =
        val n = (proxy(env.UV) * 0.25f) +
          (proxy(env.UV * 2.0f) * 0.25f) +
          (proxy(env.UV * 8.0f) * 0.25f) +
          (proxy(env.UV * 32.0f) * 0.25f)

        vec4(vec3(n), 1.0f)
    }

object WhiteNoiseShader:

  import ultraviolet.syntax.ShaderPrinter

  val shader: UltravioletShader =
    println(">> " + fragment.toGLSL[ShaderPrinter.WebGL2](false).toOutput.code)

    val res = UltravioletShader.entityFragment(
      ShaderId("white noise shader"),
      EntityShader.fragment(fragment, FragmentEnv.reference)
    )

    println("<< " + res)

    res

  import ultraviolet.syntax.*

  @nowarn
  inline def fragment =
    Shader[FragmentEnv] { env =>
      import ultraviolet.noise.*

      def proxy: vec2 => vec3 =
        p => white(p)

      def fragment(color: vec4): vec4 =
        vec4(proxy(env.UV + fract(env.TIME)), 1.0f)
    }
