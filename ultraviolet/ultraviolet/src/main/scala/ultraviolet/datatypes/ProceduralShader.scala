package ultraviolet.datatypes

import scala.annotation.tailrec
import scala.deriving.Mirror
import scala.quoted.*

final case class ProceduralShader(defs: List[ShaderAST], main: ShaderAST)

object ProceduralShader:
  given ToExpr[ProceduralShader] with {
    def apply(x: ProceduralShader)(using Quotes): Expr[ProceduralShader] =
      '{ ProceduralShader(${ Expr(x.defs) }, ${ Expr(x.main) }) }
  }

  extension (p: ProceduralShader)
    inline def render(using template: ShaderTemplate, printer: ShaderPrinter): String =
      import ShaderAST.*
      def envName(ast: ShaderAST): Option[String] =
        ast
          .find {
            case ShaderBlock(_, _, _) => true
            case _                    => false
          }
          .flatMap {
            case ShaderBlock(name, _, _) => name
            case _                       => None
          }

      def stripOutEnvName(content: String): String =
        envName(p.main) match
          case None       => content
          case Some(name) => content.replace(name + ".", "").replace(name, "")

      val renderedHeaders = p.main.headers.flatMap(printer.print).map(stripOutEnvName)
      val renderedDefs    = p.defs.map(d => printer.print(d).mkString("\n")).map(stripOutEnvName)
      val renderedBody    = printer.print(p.main).map(stripOutEnvName)

      template.print(renderedHeaders, renderedDefs, renderedBody)

    def exists(q: ShaderAST => Boolean): Boolean =
      p.main.exists(q) || p.defs.exists(_.exists(q))
