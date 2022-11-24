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
      val headers   = p.main.headers
      val functions = p.defs
      val body      = p.main

      printer.isValid(inType, outType, headers, functions, body) match
        case ShaderValid.Invalid(reasons) =>
          throw ShaderError.Validation("Shader failed to validate because: " + reasons.mkString("[", ", ", "]"))

        case ShaderValid.Valid =>
          val renderedHeaders = headers.flatMap(ShaderPrinter.print)
          val renderedDefs    = functions.map(d => ShaderPrinter.print(d).mkString("\n"))
          val renderedBody    = ShaderPrinter.print(body)

          val transformedBody: ShaderAST =
            body.traverse(printer.transformer.orElse(n => n))

          val code =
            (renderedHeaders ++ renderedDefs ++ renderedBody).mkString("\n").trim

          ShaderOutput(
            code,
            ShaderMetadata(
              uniforms = transformedBody
                .findAll {
                  case ShaderAST.Annotated(ShaderAST.DataTypes.ident("uniform"), _, ShaderAST.Val(_, _, _)) => true
                  case _                                                                                    => false
                }
                .flatMap {
                  case ShaderAST.Annotated(name, param, ShaderAST.Val(id, value, typeOf)) =>
                    List(
                      ShaderField(
                        id,
                        typeOf.getOrElse(throw ShaderError.Metadata("Uniform declaration missing return type."))
                      )
                    )

                  case _ => Nil
                },
              ubos = transformedBody
                .findAll {
                  case ShaderAST.UBO(_) => true
                  case _                => false
                }
                .flatMap {
                  case ShaderAST.UBO(ubo) => List(ubo)
                  case _                  => Nil
                },
              varyings = transformedBody
                .findAll {
                  case ShaderAST.Annotated(ShaderAST.DataTypes.ident("varying"), _, ShaderAST.Val(_, _, _)) => true
                  case ShaderAST.Annotated(ShaderAST.DataTypes.ident("in"), _, ShaderAST.Val(_, _, _))      => true
                  case ShaderAST.Annotated(ShaderAST.DataTypes.ident("out"), _, ShaderAST.Val(_, _, _))     => true
                  case _                                                                                    => false
                }
                .flatMap {
                  case ShaderAST.Annotated(name, param, ShaderAST.Val(id, value, typeOf)) =>
                    List(
                      ShaderField(
                        id,
                        typeOf.getOrElse(throw ShaderError.Metadata("Varying declaration missing return type."))
                      )
                    )

                  case _ =>
                    Nil
                }
            )
          )

    def exists(q: ShaderAST): Boolean =
      p.main.exists(q) || p.defs.exists(_.exists(q))

    def find(q: ShaderAST => Boolean): Option[ShaderAST] =
      p.main.find(q).orElse(p.defs.find(_.find(q).isDefined))

    def findAll(q: ShaderAST => Boolean): List[ShaderAST] =
      p.main.findAll(q) ++ p.defs.flatMap(_.findAll(q))
