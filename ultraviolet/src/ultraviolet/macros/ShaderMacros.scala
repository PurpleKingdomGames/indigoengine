package ultraviolet.macros

import ultraviolet.datatypes.GLSLVersion
import ultraviolet.datatypes.GLSLVersionId
import ultraviolet.datatypes.ProceduralShader
import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderDSLOps
import ultraviolet.datatypes.ShaderError
import ultraviolet.datatypes.ShaderResult
import ultraviolet.syntax.*

import java.io.File
import scala.io.Source
import scala.quoted.*

object ShaderMacros:

  // TODO: Do we also need custom printer rules?
  inline def toGLSL[In, Out](
      inline shader: Shader[In, Out],
      inline headers: List[ShaderHeader],
      inline versions: List[GLSLVersion]
  ): Map[GLSLVersionId, ShaderResult] =
    ${ toGLSLImpl('{ shader }, '{ headers }, '{ versions }) }

  private[macros] def toGLSLImpl[In, Out: Type](
      shader: Expr[Shader[In, Out]],
      headers: Expr[List[ShaderHeader]],
      versions: Expr[List[GLSLVersion]]
  )(using q: Quotes): Expr[Map[GLSLVersionId, ShaderResult]] =
    try
      val p: Expr[ProceduralShader] =
        toASTImpl(shader)

      val results: Expr[List[(GLSLVersionId, ShaderResult)]] =
        '{
          ${ versions }.map { version =>
            try
              // TODO: Apply transforms
              val transformed =
                ${ p }.applyTransformers(version.transformers)

              // TODO: Apply validation rules

              // Convert to GLSL.
              val res: ShaderResult =
                ProceduralShader.render(transformed, ${ headers })

              version.id -> res
            catch {
              case e: ShaderError =>
                GLSLVersionId("all") -> ShaderResult.Error(e.message)
            }
          }
        }

      '{ ${ results }.toMap }
    catch {
      case e: ShaderError =>
        val msg = e.message
        '{ Map(GLSLVersionId("all") -> ShaderResult.Error(${ Expr(msg) })) }
    }

  inline def toAST[In, Out](inline expr: Shader[In, Out]): ProceduralShader =
    ${ toASTImpl('{ expr }) }

  private[macros] def toASTImpl[In, Out: Type](expr: Expr[Shader[In, Out]])(using
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
        validateFunctionList(defs, ShaderDSLOps.allKeywords ++ additionalKeyword),
        createAST.uboRegister.toList,
        annotations,
        validate(0, ShaderDSLOps.allKeywords ++ additionalKeyword ++ defRefs ++ annotationRefs)(main)
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
