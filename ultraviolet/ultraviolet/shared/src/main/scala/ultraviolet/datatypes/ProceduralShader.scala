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
    inline def render[T](using printer: ShaderPrinter[T]): ShaderOutput =
      import ShaderAST.*

      val inType    = p.main.inType
      val outType   = p.main.outType
      val functions = p.defs
      val body      = p.main

      printer.isValid(inType, outType, functions, body) match
        case ShaderValid.Invalid(reasons) =>
          throw ShaderError.Validation("Shader failed to validate because: " + reasons.mkString("[", ", ", "]"))

        case ShaderValid.Valid =>
          val renderedDefs = functions.map(d => ShaderPrinter.print(d).mkString("\n"))
          val renderedBody = ShaderPrinter.print(body)

          val transformedBody: ShaderAST =
            body.traverse(printer.transformer.orElse(n => n))

          val code =
            (renderedDefs ++ renderedBody).mkString("\n").trim

          ShaderOutput(
            code,
            ShaderMetadata(
              printer.uniforms(transformedBody),
              printer.ubos(transformedBody),
              printer.varyings(transformedBody)
            )
          )

    def exists(q: ShaderAST): Boolean =
      p.main.exists(q) || p.defs.exists(_.exists(q))

    def find(q: ShaderAST => Boolean): Option[ShaderAST] =
      p.main.find(q).orElse(p.defs.find(_.find(q).isDefined))

    def findAll(q: ShaderAST => Boolean): List[ShaderAST] =
      p.main.findAll(q) ++ p.defs.flatMap(_.findAll(q))
