//> using scala "3.3.1"
//> using lib "io.indigoengine::ultraviolet:0.1.3"
//> using lib "com.lihaoyi::os-lib:0.9.1"

import ultraviolet.shadertoy.*
import ultraviolet.syntax.*
import os.*

object ShaderToy extends App:

  inline def image =
    Shader[ShaderToyEnv, Unit] { env =>
      def mainImage(fragColor: vec4, fragCoord: vec2): vec4 = {
        // Normalized pixel coordinates (from 0 to 1)
        val uv: vec2 = fragCoord / env.iResolution.xy

        // Time varying pixel color
        val col: vec3 = 0.5f + 0.5f * cos(env.iTime + uv.xyx + vec3(0.0f, 2.0f, 4.0f))

        // Output to screen
        vec4(col, 1.0f)
      }
    }

  os.makeDir.all(os.pwd / "glsl")
  os.write.over(os.pwd / "glsl" / "default.frag", image.toGLSL[ShaderToy].toOutput.code)
