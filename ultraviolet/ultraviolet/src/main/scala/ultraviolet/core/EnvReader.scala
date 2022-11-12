package ultraviolet.core

import ultraviolet.syntax.*

import scala.compiletime.constValue
import scala.compiletime.erasedValue
import scala.compiletime.summonInline
import scala.deriving.Mirror

object EnvReader:

  inline private def summonPrecision[T <: Tuple]: List[String] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Nil

      case _: (t *: ts) =>
        FindPrecision.findPrecision[t].getOrElse("") :: summonPrecision[ts]

  inline private def summonLabels[T <: Tuple]: List[String] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Nil

      case _: (t *: ts) =>
        summonInline[ValueOf[t]].value.asInstanceOf[String] :: summonLabels[ts]

  inline private def summonTypeName[T <: Tuple]: List[ShaderTypeOf[_]] =
    inline erasedValue[T] match
      case _: EmptyTuple =>
        Nil

      case _: (t *: ts) =>
        summonInline[ShaderTypeOf[t]] :: summonTypeName[ts]

  // TODO: Write a macro to disect Env 'T' and call this function foreach part
  inline def readUBO[T](using m: Mirror.ProductOf[T]): UBODef =
    val precisions = summonPrecision[m.MirroredElemTypes]
    val labels     = summonLabels[m.MirroredElemLabels]
    val typeOfs    = summonTypeName[m.MirroredElemTypes]

    UBODef(
      constValue[m.MirroredLabel],
      precisions
        .zip(labels.zip(typeOfs.map(_.typeOf)))
        .map(p => UBOField(p._1, p._2._1, p._2._2))
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

  final case class UBODef(name: String, fields: List[UBOField])
  final case class UBOField(precision: String, name: String, typeOf: String)
