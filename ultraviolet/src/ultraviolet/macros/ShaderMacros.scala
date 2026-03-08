package ultraviolet.macros

import ultraviolet.datatypes.ProceduralShader
import ultraviolet.datatypes.ProgramVersion
import ultraviolet.datatypes.ProgramVersionId
import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderDSLOps
import ultraviolet.datatypes.ShaderError
import ultraviolet.datatypes.ShaderResult
import ultraviolet.syntax.*

import java.io.File
import scala.annotation.nowarn
import scala.io.Source
import scala.quoted.*

object ShaderMacros:

  inline def toGLSL[In, Out](
      inline shader: Shader[In, Out],
      inline headers: List[ShaderHeader],
      inline versions: List[ProgramVersion]
  ): Map[ProgramVersionId, ShaderResult] =
    ${ toGLSLImpl('{ shader }, '{ headers }, '{ versions }) }

  @nowarn("msg=unused")
  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private[macros] def toGLSLImpl[In, Out: Type](
      shader: Expr[Shader[In, Out]],
      headers: Expr[List[ShaderHeader]],
      versions: Expr[List[ProgramVersion]]
  )(using q: Quotes): Expr[Map[ProgramVersionId, ShaderResult]] =
    import quotes.reflect.*

    try
      val p: Expr[ProceduralShader] =
        toASTImpl(shader)

      // Note to my future self:
      // This process is broken into stages to avoid the 'method too large' limit.
      // So while it looks like these three methods should be trivial to collaspe,
      // please do not.

      def validateVersions(): Expr[Unit] =
        '{
          ${ versions }.foreach { version =>
            ${ p }.validate(version.requirements) match
              case ShaderValid.Valid =>
                ()

              case ShaderValid.Invalid(reasons) =>
                throw ShaderError.Validation(
                  s"Shader version '${version.id}' failed requirements checks, because: ${reasons.mkString(", ")}"
                )
          }
        }

      validateVersions()

      def transformedVersions: Expr[List[(ProgramVersionId, ProceduralShader)]] =
        '{
          ${ versions }.map { version =>
            (
              version.id,
              ${ p }.applyTransformers(version.transformers)
            )
          }
        }

      def results: Expr[List[(ProgramVersionId, ShaderResult)]] =
        '{
          ${ transformedVersions }.map { (id, proc) =>
            id -> ProceduralShader.render(proc, ${ headers })
          }
        }

      '{ ${ results }.toMap }
    catch {
      case e: ShaderError =>
        report.errorAndAbort(e.message)
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
