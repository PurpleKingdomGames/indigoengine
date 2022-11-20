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
    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    inline def render[T](using printer: ShaderPrinter[T]): String =
      import ShaderAST.*

      def stripOutEnvName(content: String): String =
        p.main.envVarName match
          case None       => content
          case Some(name) => content.replace(name + ".", "").replace(name, "")

      val inType    = p.main.inType
      val outType   = p.main.outType
      val headers   = p.main.headers
      val functions = p.defs
      val body      = p.main

      printer.isValid(inType, outType, headers, functions, body) match
        case ShaderValid.Invalid(reasons) =>
          // throw new Exception(reason)
          throw ShaderError.ValidationError("Shader failed to validate because: " + reasons.mkString("[", ", ", "]"))

        case ShaderValid.Valid =>
          val renderedHeaders = headers.flatMap(ShaderPrinter.print).map(stripOutEnvName)
          val renderedDefs    = functions.map(d => ShaderPrinter.print(d).mkString("\n")).map(stripOutEnvName)
          val renderedBody    = ShaderPrinter.print(body).map(stripOutEnvName)

          (renderedHeaders ++ renderedDefs ++ renderedBody).mkString("\n").trim

    def exists(q: ShaderAST): Boolean =
      p.main.exists(q) || p.defs.exists(_.exists(q))
