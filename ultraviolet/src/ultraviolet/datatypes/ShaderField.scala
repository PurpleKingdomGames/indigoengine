package ultraviolet.datatypes

import scala.quoted.*

final case class ShaderField(name: String, typeOf: String)
object ShaderField:

  given ToExpr[ShaderField] with
    def apply(x: ShaderField)(using Quotes): Expr[ShaderField] =
      '{ ShaderField(${ Expr(x.name) }, ${ Expr(x.typeOf) }) }
