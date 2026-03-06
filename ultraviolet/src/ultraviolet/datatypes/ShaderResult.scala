package ultraviolet.datatypes

import scala.quoted.*

enum ShaderResult:
  case Error(reason: String)
  case Output(code: String, metadata: ShaderMetadata)

object ShaderResult:

  given ToExpr[ShaderResult] with
    def apply(x: ShaderResult)(using Quotes): Expr[ShaderResult] =
      x match
        case Error(reason) =>
          '{ ShaderResult.Error(${ Expr(reason) }) }

        case Output(code, metadata) =>
          '{ ShaderResult.Output(${ Expr(code) }, ${ Expr(metadata) }) }

  extension (r: ShaderResult)
    def toOutput: ShaderResult.Output =
      r match
        case Error(reason)    => ShaderResult.Output(reason, ShaderMetadata.empty)
        case o @ Output(_, _) => o
