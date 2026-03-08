package ultraviolet.datatypes

import scala.quoted.*

final case class ShaderResult(code: String, metadata: ShaderMetadata)

object ShaderResult:

  given ToExpr[ShaderResult] with
    def apply(x: ShaderResult)(using Quotes): Expr[ShaderResult] =
      '{ ShaderResult(${ Expr(x.code) }, ${ Expr(x.metadata) }) }
