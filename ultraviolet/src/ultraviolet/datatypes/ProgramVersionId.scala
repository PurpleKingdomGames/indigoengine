package ultraviolet.datatypes

import scala.quoted.*

opaque type ProgramVersionId = String
object ProgramVersionId:

  given ToExpr[ProgramVersionId] with {
    def apply(x: ProgramVersionId)(using Quotes): Expr[ProgramVersionId] =
      '{ ProgramVersionId(${ Expr(x) }) }
  }

  given FromExpr[ProgramVersionId] with
    def unapply(x: Expr[ProgramVersionId])(using Quotes): Option[ProgramVersionId] =
      import quotes.reflect.*

      def unwrap(term: Term): Term =
        term match
          case Inlined(_, _, inner) => unwrap(inner)
          case Typed(inner, _)      => unwrap(inner)
          case other                => other

      unwrap(x.asTerm) match
        case Apply(_, List(Literal(StringConstant(s)))) =>
          Some(ProgramVersionId(s))

        case e =>
          report.errorAndAbort(s"[Ultraviolet macro error, please report.] ProgramVersionId after unwrap expr:\n${e
              .show(using Printer.TreeStructure)}")
          None

  def apply(id: String): ProgramVersionId = id

  def unapply(id: ProgramVersionId): Option[String] = Some(id)

  extension (id: ProgramVersionId) def value: String = id
