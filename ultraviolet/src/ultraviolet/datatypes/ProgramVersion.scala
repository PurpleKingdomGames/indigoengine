package ultraviolet.datatypes

import scala.quoted.*

/** Represents a particular flavour of GLSL program. The programs could vary in terms of which GLSL version they target,
  * but also vary in terms of the program structure requirements. For example, to make a shader compatible Indigo, you
  * need to provide certain required functions.
  *
  * ProgramVersions aid this tuning process by providing requiements and transformers. If they user's shader meets this
  * version's requirements, then the expectation is that we should be able to transform it into a useable shader.
  */
final case class ProgramVersion(
    id: ProgramVersionId,
    requirements: List[ProgramRequirement],
    transformers: List[ProgramTransformer]
)

object ProgramVersion:

  given ToExpr[ProgramVersion] with {
    def apply(x: ProgramVersion)(using Quotes): Expr[ProgramVersion] =
      x match
        case ProgramVersion(id, reqs, transformers) =>
          '{ ProgramVersion(${ Expr(id) }, ${ Expr(reqs) }, ${ Expr(transformers) }) }
  }

  given FromExpr[ProgramVersion] with
    def unapply(x: Expr[ProgramVersion])(using Quotes): Option[ProgramVersion] =
      x match
        case '{ ProgramVersion(${ Expr(id) }, ${ Expr(reqs) }, ${ Expr(transformers) }) } =>
          Some(ProgramVersion(id, reqs, transformers))

        case _ =>
          None

  val GLSL_100: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("GLSL 100"),
      ProgramRequirement.GLSL_100,
      ProgramTransformer.GLSL_100
    )

  val GLSL_300: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("GLSL 300"),
      ProgramRequirement.GLSL_300,
      ProgramTransformer.GLSL_300
    )
