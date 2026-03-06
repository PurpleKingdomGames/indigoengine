package ultraviolet.datatypes

import scala.quoted.*

// TODO: Rename?
final case class ShaderValidationRule()

object ShaderValidationRule:

  given ToExpr[ShaderValidationRule] with {
    def apply(x: ShaderValidationRule)(using Quotes): Expr[ShaderValidationRule] =
      '{ ShaderValidationRule() }
  }
