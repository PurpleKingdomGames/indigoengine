package ultraviolet

import ultraviolet.datatypes.ShaderDSLOps
import ultraviolet.datatypes.ShaderHeader
import ultraviolet.macros.UBOReader

import scala.annotation.StaticAnnotation
import scala.deriving.Mirror

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

  type ShaderField = ultraviolet.datatypes.ShaderField
  val ShaderField: ultraviolet.datatypes.ShaderField.type = ultraviolet.datatypes.ShaderField

  final class attribute             extends StaticAnnotation
  final class const                 extends StaticAnnotation
  final class define                extends StaticAnnotation
  final class flat                  extends StaticAnnotation
  final class in                    extends StaticAnnotation
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

end syntax
