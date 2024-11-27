package ultraviolet

import ultraviolet.datatypes.ShaderDSLOps
import ultraviolet.macros.UBOReader

import scala.annotation.StaticAnnotation
import scala.annotation.nowarn
import scala.deriving.Mirror
import scala.util.matching.Regex

object syntax extends ShaderDSLOps:
  type WebGL1 = ultraviolet.datatypes.ShaderPrinter.WebGL1
  type WebGL2 = ultraviolet.datatypes.ShaderPrinter.WebGL2

  type highp[A]   = A
  type mediump[A] = A
  type lowp[A]    = A

  type RawGLSL = ultraviolet.datatypes.RawGLSL
  val RawGLSL: ultraviolet.datatypes.RawGLSL.type = ultraviolet.datatypes.RawGLSL

  type Shader[In, Out] = ultraviolet.datatypes.Shader[In, Out]
  val Shader: ultraviolet.datatypes.Shader.type = ultraviolet.datatypes.Shader

  type ShaderAST = ultraviolet.datatypes.ShaderAST
  val ShaderAST: ultraviolet.datatypes.ShaderAST.type = ultraviolet.datatypes.ShaderAST

  type ShaderPrinter[T] = ultraviolet.datatypes.ShaderPrinter[T]
  val ShaderPrinter: ultraviolet.datatypes.ShaderPrinter.type = ultraviolet.datatypes.ShaderPrinter

  type ShaderHeader = ultraviolet.datatypes.ShaderHeader
  val ShaderHeader: ultraviolet.datatypes.ShaderHeader.type = ultraviolet.datatypes.ShaderHeader

  type ShaderValid = ultraviolet.datatypes.ShaderValid
  val ShaderValid: ultraviolet.datatypes.ShaderValid.type = ultraviolet.datatypes.ShaderValid

  type UBODef = ultraviolet.datatypes.UBODef
  val UBODef: ultraviolet.datatypes.UBODef.type = ultraviolet.datatypes.UBODef

  type ShaderResult = ultraviolet.datatypes.ShaderResult
  val ShaderResult: ultraviolet.datatypes.ShaderResult.type = ultraviolet.datatypes.ShaderResult

  type ShaderMetadata = ultraviolet.datatypes.ShaderMetadata
  val ShaderMetadata: ultraviolet.datatypes.ShaderMetadata.type = ultraviolet.datatypes.ShaderMetadata

  type ShaderField = ultraviolet.datatypes.ShaderField
  val ShaderField: ultraviolet.datatypes.ShaderField.type = ultraviolet.datatypes.ShaderField

  type ShaderTypeOf[A] = ultraviolet.macros.ShaderTypeOf[A]
  val ShaderTypeOf: ultraviolet.macros.ShaderTypeOf.type = ultraviolet.macros.ShaderTypeOf

  final class attribute extends StaticAnnotation
  final class const     extends StaticAnnotation
  final class define    extends StaticAnnotation
  final class flat      extends StaticAnnotation
  final class in        extends StaticAnnotation

  @nowarn("msg=unused")
  final class layout(location: Int) extends StaticAnnotation
  final class noPerspective         extends StaticAnnotation
  final class out                   extends StaticAnnotation
  final class smooth                extends StaticAnnotation
  final class uniform               extends StaticAnnotation
  final class global                extends StaticAnnotation

  inline def ubo[A](using Mirror.ProductOf[A]) = UBOReader.readUBO[A]

  inline def raw(body: String): RawGLSL =
    RawGLSL(body)

  inline def _for[A](init: A, cond: A => Boolean, next: A => A)(f: A => Unit) =
    cfor(init, cond, next)(f)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  inline def cfor[A](init: A, cond: A => Boolean, next: A => A)(f: A => Unit) =
    var a = init
    while cond(a) do
      f(a)
      a = next(a)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  sealed trait WebGLEnv:
    var gl_FragColor: vec4
    var gl_Position: vec4
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  final case class WebGL1Env(var gl_FragColor: vec4, var gl_Position: vec4) extends WebGLEnv
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  final case class WebGL2Env(var gl_FragColor: vec4, var gl_Position: vec4) extends WebGLEnv

  def Version300ES: ShaderHeader          = ShaderHeader.Version300ES
  def PrecisionHighPFloat: ShaderHeader   = ShaderHeader.PrecisionHighPFloat
  def PrecisionMediumPFloat: ShaderHeader = ShaderHeader.PrecisionMediumPFloat
  def PrecisionLowPFloat: ShaderHeader    = ShaderHeader.PrecisionLowPFloat

  private[ultraviolet] object interpolators:

    object hex:
      private val hexGroup: String = "([0-9A-F]{2})"
      private val hex3: Regex      = List.fill(3)(hexGroup).mkString("(?i)#", "", "").r
      private val hex4: Regex      = List.fill(4)(hexGroup).mkString("(?i)#", "", "").r

      private def toScaledFloat(string: String): Float = Integer.parseInt(string, 16) / 255f

      extension (string: String) {
        def toVec3: Option[vec3] = Option(string).collect { case hex3(r, g, b) =>
          vec3(toScaledFloat(r), toScaledFloat(g), toScaledFloat(b))
        }

        def toVec4: Option[vec4] = Option(string).collect { case hex4(r, g, b, a) =>
          vec4(toScaledFloat(r), toScaledFloat(g), toScaledFloat(b), toScaledFloat(a))
        }
      }

    object rgb:
      private def is8bit(i: Int): Boolean                      = i >= 0 && i < 256
      private def toScaledFloat(string: String): Option[Float] = string.toIntOption.filter(is8bit).map(_ / 255f)

      extension (string: String) {
        def toVec3: Option[vec3] = Option(string.split(",").toList.map(toScaledFloat)).collect {
          case Some(r) :: Some(g) :: Some(b) :: Nil => vec3(r, g, b)
        }

        def toVec4: Option[vec4] = Option(string.split(",").toList.map(toScaledFloat)).collect {
          case Some(r) :: Some(g) :: Some(b) :: Some(a) :: Nil => vec4(r, g, b, a)
        }
      }

  extension (sc: StringContext) {

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def hex(args: Any*): vec3 =
      import interpolators.hex.*
      sc.s(args*).toVec3.getOrElse {
        throw IllegalArgumentException(
          s"Invalid hex values ${args.mkString}. Supported formats are #00ff00 and #00ff00ff (case insensitive), using the 'hex' and 'hexa' interpolators, respectively"
        )
      }

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def hexa(args: Any*): vec4 =
      import interpolators.hex.*
      sc.s(args*).toVec4.getOrElse {
        throw IllegalArgumentException(
          s"Invalid hexa values ${args.mkString}. Supported formats are #00ff00 and #00ff00ff (case insensitive), using the 'hex' and 'hexa' interpolators, respectively"
        )
      }

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def rgb(args: Int*): vec3 =
      import interpolators.rgb.*
      sc.s(args*).toVec3.getOrElse {
        throw IllegalArgumentException(
          s"Invalid rgb values ${args.mkString}. Supported formats are 0,255,0 and 0,255,0,255, using the 'rgb' and 'rgba' interpolators, respectively"
        )
      }

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def rgba(args: Int*): vec4 =
      import interpolators.rgb.*
      sc.s(args*).toVec4.getOrElse {
        throw IllegalArgumentException(
          s"Invalid rgba values ${args.mkString}. Supported formats are 0,255,0 and 0,255,0,255, using the 'rgb' and 'rgba' interpolators, respectively"
        )
      }
  }

end syntax
