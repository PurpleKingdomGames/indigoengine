package ultraviolet.datatypes

import scala.quoted.*

final case class ProgramVersion(
    id: ProgramVersionId,
    rules: List[ProgramValidationRule],
    transformers: List[ProgramTransformer]
)

object ProgramVersion:

  given ToExpr[ProgramVersion] with {
    def apply(x: ProgramVersion)(using Quotes): Expr[ProgramVersion] =
      x match
        case ProgramVersion(id, rules, transformers) =>
          '{ ProgramVersion(${ Expr(id) }, ${ Expr(rules) }, ${ Expr(transformers) }) }
  }

  given FromExpr[ProgramVersion] with
    def unapply(x: Expr[ProgramVersion])(using Quotes): Option[ProgramVersion] =
      x match
        case '{ ProgramVersion(${ Expr(id) }, ${ Expr(rules) }, ${ Expr(transformers) }) } =>
          Some(ProgramVersion(id, rules, transformers))

        case _ =>
          None

  val GLSL_100: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("GLSL 100"),
      ProgramValidationRule.GLSL_100,
      ProgramTransformer.GLSL_100
    )

  val GLSL_300: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("GLSL 300"),
      ProgramValidationRule.GLSL_300,
      ProgramTransformer.GLSL_300
    )
