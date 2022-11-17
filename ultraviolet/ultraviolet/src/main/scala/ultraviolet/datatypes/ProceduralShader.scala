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
    inline def render(using template: ShaderTemplate): String =
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

      val renderedHeaders = stripOutEnvName(p.main.renderHeaders)
      val renderedDefs    = p.defs.map(_.render).map(stripOutEnvName)
      val renderedBody    = stripOutEnvName(p.main.render)

      template.render(renderedHeaders, renderedDefs, renderedBody)

    def exists(q: ShaderAST => Boolean): Boolean =
      p.main.exists(q) || p.defs.exists(_.exists(q))
