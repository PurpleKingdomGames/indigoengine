package ultraviolet.datatypes

import scala.quoted.*

final case class ProceduralShader(
    defs: List[ShaderAST],
    ubos: List[ShaderAST.UBO],
    annotations: List[ShaderAST],
    main: ShaderAST
):

  def applyTransformers(transformers: List[ShaderTransformer]): ProceduralShader =
    // Convert each transformer to a partial function
    val transformFunctions: List[PartialFunction[ShaderAST, ShaderAST]] =
      transformers.map {
        case ShaderTransformer.RenameAnnotation(from, to) => {
          case ShaderAST.Annotated(ShaderAST.DataTypes.ident(id), param, v @ ShaderAST.Val(_, _, _)) if id == from =>
            ShaderAST.Annotated(ShaderAST.DataTypes.ident(to), param, v)
        }

        case ShaderTransformer.RenameFunctionAtCallSite(from, to) => {
          case ShaderAST.CallFunction(id, args, returnType) if id == from =>
            ShaderAST.CallFunction(to, args, returnType)
        }
      }

    // Combine them all together
    val transform: PartialFunction[ShaderAST, ShaderAST] =
      transformFunctions.foldLeft(PartialFunction.empty)(_.orElse(_))

    // Apply to all the AST parts, and return
    this.copy(
      defs = defs.map(_.traverse(transform)),
      annotations = annotations.map(_.traverse(transform)),
      main = main.traverse(transform)
    )

object ProceduralShader:
  given ToExpr[ProceduralShader] with {
    def apply(x: ProceduralShader)(using Quotes): Expr[ProceduralShader] =
      '{ ProceduralShader(${ Expr(x.defs) }, ${ Expr(x.ubos) }, ${ Expr(x.annotations) }, ${ Expr(x.main) }) }
  }

  def render[T](p: ProceduralShader, headers: List[ShaderHeader]): ShaderResult.Output = {
    import ShaderAST.*

    val renderedUBOs = p.ubos.map(u => ShaderPrinter.print(u).mkString("\n"))
    val renderedAnnotations = p.annotations
      .map(u => ShaderPrinter.print(u).mkString("\n"))
      .map(s => if s.startsWith("#") then s else s + ";")
    val renderedDefs = p.defs.map(d => ShaderPrinter.print(d).mkString("\n"))
    val renderedBody = ShaderPrinter.print(p.main)

    val code =
      (headers.map(_.value) ++ renderedUBOs ++ renderedAnnotations ++ renderedDefs ++ renderedBody)
        .mkString("\n")
        .trim

    val extractedUniforms: List[ShaderField] =
      p.annotations
        .filter {
          case ShaderAST.Annotated(ShaderAST.DataTypes.ident("uniform"), _, ShaderAST.Val(_, _, _)) => true
          case _                                                                                    => false
        }
        .flatMap {
          case ShaderAST.Annotated(name, param, ShaderAST.Val(id, value, typeOf)) =>
            List(
              ShaderField(
                id,
                ShaderPrinter
                  .print(typeOf)
                  .headOption
                  .getOrElse(throw ShaderError.Metadata("Uniform declaration missing return type."))
              )
            )

          case _ => Nil
        }

    val extractedVaryings: List[ShaderField] =
      p.annotations
        .filter {
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
                ShaderPrinter
                  .print(typeOf)
                  .headOption
                  .getOrElse(throw ShaderError.Metadata("Varying declaration missing return type."))
              )
            )

          case _ =>
            Nil
        }

    ShaderResult.Output(
      code,
      ShaderMetadata(
        extractedUniforms,
        p.ubos.map(_.uboDef),
        extractedVaryings
      )
    )
  }

  extension (p: ProceduralShader)
    def exists(q: ShaderAST): Boolean =
      p.main.exists(q) || p.defs.exists(_.exists(q))

    def find(q: ShaderAST => Boolean): Option[ShaderAST] =
      p.main.find(q).orElse(p.defs.find(_.find(q).isDefined))

    def findAll(q: ShaderAST => Boolean): List[ShaderAST] =
      p.main.findAll(q) ++ p.defs.flatMap(_.findAll(q))
