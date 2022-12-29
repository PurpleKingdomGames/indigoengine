package ultraviolet.macros

import ultraviolet.datatypes.UBODef
import ultraviolet.datatypes.UBOField
import ultraviolet.syntax.*

import scala.compiletime.constValue
import scala.compiletime.erasedValue
import scala.compiletime.summonInline
import scala.deriving.Mirror

object UBOReader:

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

    given ShaderTypeOf[Boolean] with
      def typeOf: String = "bool"

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

    given ShaderTypeOf[bvec2] with
      def typeOf: String = "bvec2"

    given ShaderTypeOf[bvec3] with
      def typeOf: String = "bvec3"

    given ShaderTypeOf[bvec4] with
      def typeOf: String = "bvec4"

    given ShaderTypeOf[ivec2] with
      def typeOf: String = "ivec2"

    given ShaderTypeOf[ivec3] with
      def typeOf: String = "ivec3"

    given ShaderTypeOf[ivec4] with
      def typeOf: String = "ivec4"

  given array0[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[0, A]] with
    def typeOf: String = s"${sto.typeOf}[0]"
  given array1[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[1, A]] with
    def typeOf: String = s"${sto.typeOf}[1]"
  given array2[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[2, A]] with
    def typeOf: String = s"${sto.typeOf}[2]"
  given array3[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[3, A]] with
    def typeOf: String = s"${sto.typeOf}[3]"
  given array4[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[4, A]] with
    def typeOf: String = s"${sto.typeOf}[4]"
  given array5[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[5, A]] with
    def typeOf: String = s"${sto.typeOf}[5]"
  given array6[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[6, A]] with
    def typeOf: String = s"${sto.typeOf}[6]"
  given array7[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[7, A]] with
    def typeOf: String = s"${sto.typeOf}[7]"
  given array8[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[8, A]] with
    def typeOf: String = s"${sto.typeOf}[8]"
  given array9[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[9, A]] with
    def typeOf: String = s"${sto.typeOf}[9]"
  given array10[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[10, A]] with
    def typeOf: String = s"${sto.typeOf}[10]"
  given array11[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[11, A]] with
    def typeOf: String = s"${sto.typeOf}[11]"
  given array12[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[12, A]] with
    def typeOf: String = s"${sto.typeOf}[12]"
  given array13[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[13, A]] with
    def typeOf: String = s"${sto.typeOf}[13]"
  given array14[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[14, A]] with
    def typeOf: String = s"${sto.typeOf}[14]"
  given array15[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[15, A]] with
    def typeOf: String = s"${sto.typeOf}[15]"
  given array16[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[16, A]] with
    def typeOf: String = s"${sto.typeOf}[16]"
  given array17[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[17, A]] with
    def typeOf: String = s"${sto.typeOf}[17]"
  given array18[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[18, A]] with
    def typeOf: String = s"${sto.typeOf}[18]"
  given array19[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[19, A]] with
    def typeOf: String = s"${sto.typeOf}[19]"
  given array20[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[20, A]] with
    def typeOf: String = s"${sto.typeOf}[20]"
  given array21[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[21, A]] with
    def typeOf: String = s"${sto.typeOf}[21]"
  given array22[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[22, A]] with
    def typeOf: String = s"${sto.typeOf}[22]"
  given array23[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[23, A]] with
    def typeOf: String = s"${sto.typeOf}[23]"
  given array24[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[24, A]] with
    def typeOf: String = s"${sto.typeOf}[24]"
  given array25[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[25, A]] with
    def typeOf: String = s"${sto.typeOf}[25]"
  given array26[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[26, A]] with
    def typeOf: String = s"${sto.typeOf}[26]"
  given array27[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[27, A]] with
    def typeOf: String = s"${sto.typeOf}[27]"
  given array28[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[28, A]] with
    def typeOf: String = s"${sto.typeOf}[28]"
  given array29[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[29, A]] with
    def typeOf: String = s"${sto.typeOf}[29]"
  given array30[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[30, A]] with
    def typeOf: String = s"${sto.typeOf}[30]"
  given array31[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[31, A]] with
    def typeOf: String = s"${sto.typeOf}[31]"
  given array32[A](using sto: ShaderTypeOf[A]): ShaderTypeOf[array[32, A]] with
    def typeOf: String = s"${sto.typeOf}[32]"
