package ultraviolet.datatypes

import scala.quoted.*

final case class ShaderHeader(value: String)
object ShaderHeader:

  given FromExpr[ShaderHeader] with
    def unapply(x: Expr[ShaderHeader])(using Quotes): Option[ShaderHeader] =
      import quotes.reflect.*

      def unwrap(term: Term): Term =
        term match
          case Inlined(_, _, inner) => unwrap(inner)
          case Typed(inner, _)      => unwrap(inner)
          case other                => other

      unwrap(x.asTerm).asExprOf[ShaderHeader] match
        case '{ ShaderHeader(${ Expr(header) }) } =>
          Some(ShaderHeader(header))

        case e =>
          report.errorAndAbort(s"[Ultraviolet macro error, please report.] ShaderHeader after unwrap expr:\n${e.asTerm
              .show(using Printer.TreeStructure)}")
          None

  inline def Version300ES: ShaderHeader          = ShaderHeader("#version 300 es")
  inline def PrecisionHighPFloat: ShaderHeader   = ShaderHeader("precision highp float;")
  inline def PrecisionMediumPFloat: ShaderHeader = ShaderHeader("precision mediump float;")
  inline def PrecisionLowPFloat: ShaderHeader    = ShaderHeader("precision lowp float;")
