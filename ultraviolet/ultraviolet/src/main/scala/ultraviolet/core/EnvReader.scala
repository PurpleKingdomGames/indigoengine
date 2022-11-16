package ultraviolet.core

import ultraviolet.syntax.*

import scala.compiletime.constValue
import scala.compiletime.erasedValue
import scala.compiletime.summonInline
import scala.deriving.Mirror

object EnvReader:

  inline private def summonPrecision[T <: Tuple]: List[Option[String]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => FindPrecision.findPrecision[t] :: summonPrecision[ts]

  inline private def summonLabels[T <: Tuple]: List[String] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[ValueOf[t]].value.asInstanceOf[String] :: summonLabels[ts]

  inline private def summonTypeName[T <: Tuple]: List[ShaderTypeOf[_]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[ShaderTypeOf[t]] :: summonTypeName[ts]

  inline def readUBO[T](using m: Mirror.ProductOf[T]): UBODef =
    UBODef(
      constValue[m.MirroredLabel],
      summonPrecision[m.MirroredElemTypes]
        .zip(
          summonLabels[m.MirroredElemLabels]
            .zip(summonTypeName[m.MirroredElemTypes].map(_.typeOf))
        )
        .map(p => UBOField(p._1, p._2._2, p._2._1))
    )

  trait ShaderTypeOf[A]:
    def typeOf: String

  object ShaderTypeOf:

    given ShaderTypeOf[Int] with
      def typeOf: String = "int"

    given ShaderTypeOf[Float] with
      def typeOf: String = "float"

    given ShaderTypeOf[vec2] with
      def typeOf: String = "vec2"

    given ShaderTypeOf[vec3] with
      def typeOf: String = "vec3"

    given ShaderTypeOf[vec4] with
      def typeOf: String = "vec4"
