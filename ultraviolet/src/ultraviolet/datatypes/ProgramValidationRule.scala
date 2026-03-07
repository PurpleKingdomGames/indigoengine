package ultraviolet.datatypes

import scala.quoted.*

final case class ProgramValidationRule()

object ProgramValidationRule:

  given ToExpr[ProgramValidationRule] with {
    def apply(x: ProgramValidationRule)(using Quotes): Expr[ProgramValidationRule] =
      '{ ProgramValidationRule() }
  }

  val GLSL_100: List[ProgramValidationRule] =
    Nil

  val GLSL_300: List[ProgramValidationRule] =
    Nil
