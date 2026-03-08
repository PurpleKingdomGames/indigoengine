package ultraviolet.predef

import ultraviolet.datatypes.ProgramRequirement
import ultraviolet.datatypes.ProgramTransformer
import ultraviolet.datatypes.ProgramVersion
import ultraviolet.datatypes.ProgramVersionId
import ultraviolet.syntax.*

object shadertoy:

  // Current doesn't support samplerCube types, Ultraviolet does, just not sure how to represent that here.
  final case class ShaderToyEnv(
      iResolution: vec3,                  // viewport resolution (in pixels)
      iTime: Float,                       // shader playback time (in seconds)
      iTimeDelta: Float,                  // render time (in seconds)
      iFrameRate: Float,                  // shader frame rate
      iFrame: Int,                        // shader playback frame
      iChannelTime: array[4, Float],      // channel playback time (in seconds)
      iChannelResolution: array[4, vec3], // channel resolution (in pixels)
      iMouse: vec4,                       // mouse pixel coords. xy: current (if MLB down) = null zw: click
      iChannel0: sampler2D.type,          // input channel. XX = 2D/Cube
      iChannel1: sampler2D.type,          // input channel. XX = 2D/Cube
      iChannel2: sampler2D.type,          // input channel. XX = 2D/Cube
      iChannel3: sampler2D.type,          // input channel. XX = 2D/Cube
      iDate: vec4,                        // (year = null month = null day = null time in seconds)
      iSampleRate: Float                  // sound sample rate (i.e. = null 44100)
  )
  object ShaderToyEnv:
    def Default: ShaderToyEnv =
      ShaderToyEnv(
        iResolution = vec3(640.0f, 480.0f, 0.0f),
        iTime = 0.0f,
        iTimeDelta = 0.0167,
        iFrameRate = 60,
        iFrame = 0,
        iChannelTime = array[4, Float],      // channel playback time (in seconds)
        iChannelResolution = array[4, vec3], // channel resolution (in pixels)
        iMouse = vec4(0.0f),
        iChannel0 = sampler2D,
        iChannel1 = sampler2D,
        iChannel2 = sampler2D,
        iChannel3 = sampler2D,
        iDate = vec4(0.0f),
        iSampleRate = 44100.0f
      )

  private val requirements: List[ProgramRequirement] =
    List(
      ProgramRequirement.UsesRequiredEnvironment("ShaderToyEnv"),
      ProgramRequirement.ReturnsRequiredType("Unit"),
      ProgramRequirement.Function2Exists("mainImage", "vec4", "vec2", "vec4")
    ) ++ ProgramRequirement.GLSL_300

  private val transformers: List[ProgramTransformer] =
    List(
      ProgramTransformer.ConvertPureFunctionToAssignment("mainImage", "fragColor"),
      ProgramTransformer.AnnotateFunctionArgument("mainImage", "fragColor", "out")
    ) ++
      ProgramTransformer.GLSL_300

  val ShaderToyProgram: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("ShaderToy"),
      requirements,
      transformers
    )
