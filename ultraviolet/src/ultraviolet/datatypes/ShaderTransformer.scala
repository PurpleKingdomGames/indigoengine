package ultraviolet.datatypes

import scala.quoted.*

// TODO: Rename? GLSLTransformer?
final case class ShaderTransformer()

object ShaderTransformer:

  given ToExpr[ShaderTransformer] with {
    def apply(x: ShaderTransformer)(using Quotes): Expr[ShaderTransformer] =
      '{ ShaderTransformer() }
  }
