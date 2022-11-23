package ultraviolet

import ultraviolet.datatypes.ShaderDSLOps
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

  type GLSLHeader[In, Out] = ultraviolet.datatypes.GLSLHeader
  val GLSLHeader: ultraviolet.datatypes.GLSLHeader.type = ultraviolet.datatypes.GLSLHeader

  final class attribute extends StaticAnnotation
  final class const     extends StaticAnnotation
  final class define    extends StaticAnnotation
  final class uniform   extends StaticAnnotation
  final class in        extends StaticAnnotation
  final class out       extends StaticAnnotation

  inline def ubo[A](using Mirror.ProductOf[A]) = UBOReader.readUBO[A]

  inline def raw(body: String): RawGLSL =
    RawGLSL(body)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  inline def cfor[A](init: A, cond: A => Boolean, next: A => A)(f: A => Unit) =
    var a = init
    while cond(a) do
      f(a)
      a = next(a)

end syntax
