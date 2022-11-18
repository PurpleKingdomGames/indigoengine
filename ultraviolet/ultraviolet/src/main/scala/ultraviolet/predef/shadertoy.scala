package ultraviolet.predef

import ultraviolet.datatypes.ShaderTemplate
import ultraviolet.syntax.*

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
object shadertoy:

  final case class ShaderToyEnv(
      iResolution: vec3, // viewport resolution (in pixels)
      iTime: Float,      // shader playback time (in seconds)
      iTimeDelta: Float, // render time (in seconds)
      iFrameRate: Float, // shader frame rate
      iFrame: Int,       // shader playback frame
      // iChannelTime[4]: Float, // channel playback time (in seconds)
      // iChannelResolution[4]: vec3, // channel resolution (in pixels)
      iMouse: vec4, // mouse pixel coords. xy: current (if MLB down) = null zw: click
      // iChannel0..3: samplerXX, // input channel. XX = 2D/Cube
      iDate: vec4,        // (year = null month = null day = null time in seconds)
      iSampleRate: Float, // sound sample rate (i.e. = null 44100)
      fragCoord: vec2,    // UV coordinates // Unoffical, from the main function definition
      var fragColor: vec4 // output variable // Unoffical, from the main function definition
  )
  object ShaderToyEnv:
    def Default: ShaderToyEnv =
      ShaderToyEnv(
        iResolution = vec3(640.0f, 480.0f, 0.0f),
        iTime = 0.0f,
        iTimeDelta = 0.0167,
        iFrameRate = 60,
        iFrame = 0,
        // iChannelTime[4]: Float, // channel playback time (in seconds)
        // iChannelResolution[4]: vec3, // channel resolution (in pixels)
        iMouse = vec4(0.0f),
        // iChannel0..3: samplerXX, // input channel. XX = 2D/Cube
        iDate = vec4(0.0f),
        iSampleRate = 44100.0f,
        fragCoord = vec2(0.0),
        fragColor = vec4(0.0f)
      )

  given ShaderTemplate with
    def print(headers: List[String], functions: List[String], body: List[String]): String =
      val (main, last) = body.splitAt(body.length - 1)
      s"""
      |${headers.mkString("\n")}
      |${functions.mkString("\n")}
      |void mainImage(out vec4 fragColor, in vec2 fragCoord){
      |${main.map(b => "  " + b).mkString("\n")}
      |  fragColor=${last.mkString}
      |}
      |""".stripMargin.trim
