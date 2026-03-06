package ultraviolet.datatypes

import scala.quoted.*

// TODO: Rename? GLSLTransformer?
enum ShaderTransformer:
  case RenameFunctionAtCallSite(from: String, to: String)
  case RenameAnnotation(from: String, to: String)

object ShaderTransformer:

  given ToExpr[ShaderTransformer] with {
    def apply(x: ShaderTransformer)(using Quotes): Expr[ShaderTransformer] =
      x match
        case ShaderTransformer.RenameFunctionAtCallSite(from, to) =>
          '{ ShaderTransformer.RenameFunctionAtCallSite(${ Expr(from) }, ${ Expr(to) }) }

        case ShaderTransformer.RenameAnnotation(from, to) =>
          '{ ShaderTransformer.RenameAnnotation(${ Expr(from) }, ${ Expr(to) }) }
  }
