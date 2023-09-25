//> using scala "3.3.1"
//> using lib "io.indigoengine::ultraviolet:0.1.3"
//> using lib "com.lihaoyi::os-lib:0.9.1"

import ultraviolet.shadertoy.*
import ultraviolet.syntax.*
import os.*

object ShaderToy extends App:

  inline def bufferA =
    Shader[ShaderToyEnv, Unit] { env =>
      @const val pi: Float = 3.1415926435f

      def mainImage(fragColor: vec4, fragCoord: vec2): vec4 =
        val i: Float = fragCoord.x / env.iResolution.x
        val t: vec3  = (env.iTime + env.iMouse.y) / vec3(63.0f, 78.0f, 45.0f)
        val cs: vec3 = cos(i * pi * 2.0f + vec3(0.0f, 1.0f, -0.5f) * pi + t)

        vec4(0.5f + 0.5f * cs, 1.0f)
    }

  inline def image =
    Shader[ShaderToyEnv, Unit] { env =>
      @const val vp: vec2 = vec2(320.0, 200.0)

      def mainImage(fragColor: vec4, fragCoord: vec2): vec4 =
        val t: Float  = env.iTime * 10.0f + env.iMouse.x
        val uv: vec2  = fragCoord.xy / env.iResolution.xy
        val p0: vec2  = (uv - 0.5f) * vp
        val hvp: vec2 = vp * 0.5f
        val p1d: vec2 = vec2(cos(t / 98.0f), sin(t / 178.0f)) * hvp - p0
        val p2d: vec2 = vec2(sin(-t / 124.0f), cos(-t / 104.0f)) * hvp - p0
        val p3d: vec2 = vec2(cos(-t / 165.0f), cos(t / 45.0f)) * hvp - p0
        val sum: Float = 0.25f + 0.5f * (cos(length(p1d) / 30.0f) +
          cos(length(p2d) / 20.0f) +
          sin(length(p3d) / 25.0f) * sin(p3d.x / 20.0f) * sin(p3d.y / 15.0f))

        texture2D(env.iChannel0, vec2(fract(sum), 0))
    }

  os.makeDir.all(os.pwd / "glsl")
  os.write.over(os.pwd / "glsl" / "plasma-buffer-a.frag", bufferA.toGLSL[ShaderToy].toOutput.code)
  os.write.over(os.pwd / "glsl" / "plasma-image.frag", image.toGLSL[ShaderToy].toOutput.code)
