package ultraviolet.predef

import ultraviolet.datatypes.ProgramTransformer
import ultraviolet.datatypes.ProgramValidationRule
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

  private val rules: List[ProgramValidationRule] =
    ProgramValidationRule.GLSL_300
  //   def isValid(
  //       inType: Option[String],
  //       outType: Option[String],
  //       functions: List[ShaderAST],
  //       body: ShaderAST
  //   ): ShaderValid =
  //     val inTypeValid: ShaderValid =
  //       if inType.contains("ShaderToyEnv") then ShaderValid.Valid
  //       else
  //         ShaderValid.Invalid(
  //           List(
  //             "ShaderToy Shader instances must be of type Shader[ShaderToyEnv, Unit], environment type was: " +
  //               inType.getOrElse("<missing>")
  //           )
  //         )

  //     val outTypeValid: ShaderValid =
  //       if outType.contains("Unit") then ShaderValid.Valid
  //       else
  //         ShaderValid.Invalid(
  //           List(
  //             "ShaderToy Shader instances must be of type Shader[ShaderToyEnv, Unit], return type was: " +
  //               outType.getOrElse("<missing>")
  //           )
  //         )

  //     val hasMainImageFunction: ShaderValid =
  //       val main =
  //         body.find {
  //           case ShaderAST.Function(
  //                 "mainImage",
  //                 List(
  //                   (ShaderAST.DataTypes.ident("vec4") -> "fragColor"),
  //                   (ShaderAST.DataTypes.ident("vec2") -> "fragCoord")
  //                 ),
  //                 body,
  //                 ShaderAST.DataTypes.ident("vec4")
  //               ) =>
  //             true

  //           case _ => false
  //         }

  //       main match
  //         case Some(_) =>
  //           ShaderValid.Valid

  //         case None =>
  //           ShaderValid.Invalid(
  //             List(
  //               "ShaderToy Shader instances must declare a 'mainImage' function: `def mainImage(fragColor: vec4, fragCoord: vec2): vec4 = ???`"
  //             )
  //           )

  //     webGL2Printer.isValid(inType, outType, functions, body) |+|
  //       (inTypeValid |+| outTypeValid |+| hasMainImageFunction)

  private val transformers: List[ProgramTransformer] =
    List(
      ProgramTransformer.ConvertPureFunctionToAssignment("mainImage", "fragColor"),
      ProgramTransformer.AnnotateFunctionArgument("mainImage", "fragColor", "out")
    ) ++
      ProgramTransformer.GLSL_300

  val ShaderToyProgram: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("ShaderToy"),
      rules,
      transformers
    )
