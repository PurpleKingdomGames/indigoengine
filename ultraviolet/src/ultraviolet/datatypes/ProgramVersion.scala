package ultraviolet.datatypes

import scala.annotation.nowarn
import scala.quoted.*

/** Represents a particular flavour of GLSL program. The programs could vary in terms of which GLSL version they target,
  * but also vary in terms of the program structure requirements. For example, to make a shader compatible Indigo, you
  * need to provide certain required functions.
  *
  * ProgramVersions aid this tuning process by providing requirements and transformers. If they user's shader meets this
  * version's requirements, then the expectation is that we should be able to transform it into a usable shader.
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
    @nowarn("msg=cannot be checked")
    def unapply(x: Expr[ProgramVersion])(using Quotes): Option[ProgramVersion] =
      import quotes.reflect.*

      def unwrap(term: Term): Term =
        term match
          case Inlined(_, _, inner) => unwrap(inner)
          case Typed(inner, _)      => unwrap(inner)
          case other                => other

      def extractList[T](term: Term)(extract: Term => Option[T]): Option[List[T]] =
        unwrap(term) match
          case a @ Apply(
                TypeApply(Select(Ident("List"), "apply"), _),
                List(
                  Typed(
                    Repeated(
                      elems,
                      _
                    ),
                    _
                  )
                )
              ) =>
            val results =
              elems.map(e => extract(unwrap(e)))

            // If any fail to extract, fail.
            if results.forall(_.isDefined) then Some(results.map(_.toList).flatten)
            else
              report.errorAndAbort(
                s"[Ultraviolet macro error, please report.] Some elements failed during List[T] extract:\n${a
                    .show(using Printer.TreeStructure)}"
              )

          case Apply(TypeApply(Select(Ident("List"), "empty"), _), Nil) =>
            Some(Nil)

          case TypeApply(Select(Ident("List"), "empty"), _) =>
            Some(Nil)

          case Ident("Nil") =>
            Some(Nil)

          case other =>
            report.errorAndAbort(s"[Ultraviolet macro error, please report.] Could not extract List[T]:\n${other
                .show(using Printer.TreeStructure)}")

      unwrap(x.asTerm) match
        case Apply(fun, List(idTerm, reqsTerm, transTerm)) =>
          val id: ProgramVersionId =
            summon[FromExpr[ProgramVersionId]]
              .unapply(unwrap(idTerm).asExprOf[ProgramVersionId]) match
              case Some(value) => value
              case None =>
                report.errorAndAbort(
                  s"[Ultraviolet macro error, please report.] Could not extract ProgramVersionId:\n${idTerm
                      .show(using Printer.TreeStructure)}"
                )

          val reqs: List[ProgramRequirement] =
            extractList(reqsTerm) { t =>
              summon[FromExpr[ProgramRequirement]].unapply(unwrap(t).asExprOf[ProgramRequirement])
            } match
              case Some(value) => value
              case None =>
                report.errorAndAbort(
                  s"[Ultraviolet macro error, please report.] Could not extract List[ProgramRequirement]:\n${reqsTerm
                      .show(using Printer.TreeStructure)}"
                )

          val trans: List[ProgramTransformer] =
            extractList(transTerm) { t =>
              summon[FromExpr[ProgramTransformer]].unapply(unwrap(t).asExprOf[ProgramTransformer])
            } match
              case Some(value) => value
              case None =>
                report.errorAndAbort(
                  s"[Ultraviolet macro error, please report.] Could not extract List[ProgramTransformer]:\n${transTerm
                      .show(using Printer.TreeStructure)}"
                )

          Some(ProgramVersion(id, reqs, trans))

        case e =>
          report.errorAndAbort(s"[Ultraviolet macro error, please report.] ProgramVersion after unwrap expr:\n${e
              .show(using Printer.TreeStructure)}")
          None

  inline def GLSL_100: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("GLSL 100"),
      List.empty[ProgramRequirement],
      ProgramTransformer.GLSL_100
    )

  inline def GLSL_300: ProgramVersion =
    ProgramVersion(
      ProgramVersionId("GLSL 300"),
      List.empty[ProgramRequirement],
      ProgramTransformer.GLSL_300
    )
