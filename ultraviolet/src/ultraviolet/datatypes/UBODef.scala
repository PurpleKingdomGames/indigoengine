package ultraviolet.datatypes

import scala.quoted.*

final case class UBODef(name: String, fields: List[UBOField]):
  def render: String =
    s"""
    |layout (std140) uniform $name {
    |${fields.map(f => "  " + f.render).mkString("\n")}
    |};
    |""".stripMargin.trim
object UBODef:
  given ToExpr[UBODef] with {
    def apply(x: UBODef)(using Quotes): Expr[UBODef] =
      '{ UBODef(${ Expr(x.name) }, ${ Expr(x.fields) }) }
  }

final case class UBOField(precision: Option[String], typeOf: String, name: String):
  def render: String =
    val p = precision.map(_ + " ").getOrElse("")
    s"$p$typeOf $name;"
object UBOField:
  given ToExpr[UBOField] with {
    def apply(x: UBOField)(using Quotes): Expr[UBOField] =
      '{ UBOField(${ Expr(x.precision) }, ${ Expr(x.typeOf) }, ${ Expr(x.name) }) }
  }
