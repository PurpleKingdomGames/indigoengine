package ultraviolet.core

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
    inline def render: String =
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

      val renderedHeaders = List(p.main.renderHeaders)
      val renderedDefs    = p.defs.map(_.render)
      val renderedBody    = List(p.main.render)

      val res = (renderedHeaders ++ renderedDefs ++ renderedBody).mkString("\n").trim

      val shaderBody =
        envName(p.main) match
          case None       => res
          case Some(name) => res.replace(name + ".", "").replace(name, "")

      shaderBody

    def exists(q: ShaderAST => Boolean): Boolean =
      p.main.exists(q) || p.defs.exists(_.exists(q))
