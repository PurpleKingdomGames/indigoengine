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
      x match
        case Expr(id) =>
          Some(ProgramVersionId(id))

        case _ =>
          None

  def apply(id: String): ProgramVersionId = id

  def unapply(id: ProgramVersionId): Option[String] = Some(id)

  extension (id: ProgramVersionId) def value: String = id
