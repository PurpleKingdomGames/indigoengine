package ultraviolet.macros

import ultraviolet.datatypes.ProceduralShader
import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderDSLOps
import ultraviolet.datatypes.ShaderError
import ultraviolet.syntax.*

import java.io.File
import scala.io.Source
import scala.quoted.*

object ShaderMacros:

  inline def toAST[In, Out](inline expr: Shader[In, Out]): ProceduralShader = ${ toASTImpl('{ expr }, true) }

  inline def toASTNoValidation[In, Out](inline expr: Shader[In, Out]): ProceduralShader = ${
    toASTImpl('{ expr }, false)
  }

  private[macros] def toASTImpl[In, Out: Type](expr: Expr[Shader[In, Out]], useValidation: Boolean)(using
      q: Quotes
  ): Expr[ProceduralShader] = {
    import q.reflect.*
    import ShaderProgramValidation.*

    val createAST = new CreateShaderAST[q.type](using q)

    val main =
      createAST.walkTerm(expr.asTerm, None)

    val defs =
      createAST.shaderDefs.toList.filterNot(_.userDefined).map(_.fn)

    val annotations =
      createAST.annotationRegister.toList

    val defRefs = defs.map(_.id)
    val annotationRefs =
      annotations.flatMap {
        case ShaderAST.Annotated(_, _, ShaderAST.Val(id, _, _)) =>
          List(id)

        case ShaderAST.Annotated(_, _, ShaderAST.Annotated(_, _, ShaderAST.Val(id, _, _))) =>
          List(id)

        case _ =>
          Nil
      }

    val additionalKeyword =
      List(
        "sampler2D"
      )

    Expr(
      ProceduralShader(
        if useValidation then validateFunctionList(defs, ShaderDSLOps.allKeywords ++ additionalKeyword)
        else defs,
        createAST.uboRegister.toList,
        annotations,
        if useValidation then
          validate(0, ShaderDSLOps.allKeywords ++ additionalKeyword ++ defRefs ++ annotationRefs)(main)
        else main
      )
    )
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
