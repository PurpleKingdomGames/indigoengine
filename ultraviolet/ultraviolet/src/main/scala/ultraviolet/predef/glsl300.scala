package ultraviolet.predef

import ultraviolet.macros.UBOReader

import scala.annotation.StaticAnnotation
import scala.deriving.Mirror

object glsl300:

  final class in  extends StaticAnnotation
  final class out extends StaticAnnotation

  inline def ubo[A](using Mirror.ProductOf[A]) = UBOReader.readUBO[A]
