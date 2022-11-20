package ultraviolet.predef

import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderPrinter
import ultraviolet.datatypes.ShaderValid
import ultraviolet.syntax.*

object shadertoy:

  // Current doesn't support samplerCube types, Ultraviolet does, just not sure how to represent that here.
  final case class ShaderToyEnv(
      iResolution: vec3,                  // viewport resolution (in pixels)
      iTime: Float,                       // shader playback time (in seconds)
      iTimeDelta: Float,                  // render time (in seconds)
      iFrameRate: Float,                  // shader frame rate
      iFrame: Int,                        // shader playback frame
      iChannelTime: array[Float, 4],      // channel playback time (in seconds)
      iChannelResolution: array[vec3, 4], // channel resolution (in pixels)
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
        iChannelTime = array[Float, 4](4),      // channel playback time (in seconds)
        iChannelResolution = array[vec3, 4](4), // channel resolution (in pixels)
        iMouse = vec4(0.0f),
        iChannel0 = sampler2D,
        iChannel1 = sampler2D,
        iChannel2 = sampler2D,
        iChannel3 = sampler2D,
        iDate = vec4(0.0f),
        iSampleRate = 44100.0f
      )

  sealed trait ShaderToy

  given ShaderPrinter[ShaderToy] = new ShaderPrinter {
    val webGL2Printer = summon[ShaderPrinter[WebGL2]]

    def isValid(
        inType: Option[String],
        outType: Option[String],
        headers: List[ShaderAST],
        functions: List[ShaderAST],
        body: ShaderAST
    ): ShaderValid =
      val inTypeValid: ShaderValid =
        if inType.contains("ShaderToyEnv") then ShaderValid.Valid
        else
          ShaderValid.Invalid(
            List(
              "ShaderToy Shader instances must be of type Shader[ShaderToyEnv, Unit], environment type was: " +
                inType.getOrElse("<missing>")
            )
          )

      val outTypeValid: ShaderValid =
        if outType.contains("Unit") then ShaderValid.Valid
        else
          ShaderValid.Invalid(
            List(
              "ShaderToy Shader instances must be of type Shader[ShaderToyEnv, Unit], return type was: " +
                outType.getOrElse("<missing>")
            )
          )

      val hasMainImageMethod: ShaderValid =
        val main =
          body.find {
            case ShaderAST.Function(
                  "mainImage",
                  List(
                    (ShaderAST.DataTypes.ident("vec4") -> "fragColor"),
                    (ShaderAST.DataTypes.ident("vec2") -> "fragCoord")
                  ),
                  body,
                  Some(ShaderAST.DataTypes.ident("vec4"))
                ) =>
              true

            case _ => false
          }

        main match
          case Some(_) =>
            ShaderValid.Valid

          case None =>
            ShaderValid.Invalid(
              List(
                "ShaderToy Shader instances must declare a 'mainImage' method: `def mainImage(fragColor: vec4, fragCoord: vec2): vec4 = ???`"
              )
            )

      webGL2Printer.isValid(inType, outType, headers, functions, body) |+|
        (inTypeValid |+| outTypeValid |+| hasMainImageMethod)

    def transformer: PartialFunction[ShaderAST, ShaderAST] =
      val pf: PartialFunction[ShaderAST, ShaderAST] = {
        case ShaderAST.Function(
              "mainImage",
              List(typ1 -> fragColor, typ2 -> fragCoord),
              ShaderAST.Block(statements),
              Some(ShaderAST.DataTypes.ident("vec4"))
            ) =>
          val nonEmpty = statements
            .filterNot(_.isEmpty)

          val (init, last) =
            if nonEmpty.length > 1 then (nonEmpty.dropRight(1), nonEmpty.takeRight(1))
            else (Nil, nonEmpty)

          ShaderAST.Function(
            "mainImage",
            List(
              ShaderAST.Annotated(ShaderAST.DataTypes.ident("out"), ShaderAST.Empty(), typ1) -> fragColor,
              typ2                                                                           -> fragCoord
            ),
            ShaderAST.Block(
              init ++
                List(
                  ShaderAST.Assign(ShaderAST.DataTypes.ident("fragColor"), last.headOption.getOrElse(ShaderAST.Empty()))
                )
            ),
            None
          )

        case ShaderAST.Function(
              "mainImage",
              List(typ1 -> fragColor, typ2 -> fragCoord),
              body,
              Some(ShaderAST.DataTypes.ident("vec4"))
            ) =>
          ShaderAST.Function(
            "mainImage",
            List(
              ShaderAST.Annotated(ShaderAST.DataTypes.ident("out"), ShaderAST.Empty(), typ1) -> fragColor,
              typ2                                                                           -> fragCoord
            ),
            ShaderAST.Assign(ShaderAST.DataTypes.ident("fragColor"), body),
            None
          )
      }

      pf.orElse(webGL2Printer.transformer)

    def printer: PartialFunction[ShaderAST, List[String]] = webGL2Printer.printer
  }
