package ultraviolet.macros

import ultraviolet.datatypes.ProceduralShader
import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderError
import ultraviolet.datatypes.UBODef
import ultraviolet.datatypes.UBOField
import ultraviolet.syntax.*

import java.io.File
import scala.annotation.tailrec
import scala.io.Source
import scala.quoted.*

object ShaderMacros:

  inline def toAST[In, Out](inline expr: Shader[In, Out]): ProceduralShader = ${ toASTImpl('{ expr }) }

  private[macros] def toASTImpl[In, Out: Type](expr: Expr[Shader[In, Out]])(using q: Quotes): Expr[ProceduralShader] = {
    import q.reflect.*

    val createAST = new CreateShaderAST[q.type](using q)

    val main =
      // Skip the initial noise... if possible
      expr.asTerm match
        // inline def has no arguments
        case Inlined(_, _, Inlined(_, _, Inlined(_, _, term))) =>
          createAST.walkTerm(term, None)

        // inline def has arguments (which we ignore)
        case Inlined(_, _, Inlined(_, _, Typed(term, _))) =>
          createAST.walkTerm(term, None)

        case term =>
          createAST.walkTerm(term, None)

    val shaderDefList = createAST.shaderDefs.toList

    Expr(ProceduralShader(shaderDefList.filterNot(_.userDefined).map(_.fn), main))
  }

  inline def fromFile(inline expr: String): RawGLSL = ${ fromFileImpl('{ expr }) }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private[macros] def fromFileImpl[In, Out: Type](expr: Expr[String])(using q: Quotes): Expr[RawGLSL] =
    expr.value match
      case None =>
        throw ShaderError.OnFileLoad("Unexpected error loading a shader from a file.")

      case Some(path) =>
        val f = File(path)
        if f.exists() then
          val glsl = Source.fromFile(f).getLines().toList.mkString("\n")
          Expr(RawGLSL(glsl))
        else throw ShaderError.OnFileLoad("Could not find shader file on given path: " + f.getAbsolutePath())

  given ToExpr[RawGLSL] with {
    def apply(x: RawGLSL)(using Quotes): Expr[RawGLSL] =
      '{ RawGLSL(${ Expr(x.glsl) }) }
  }
