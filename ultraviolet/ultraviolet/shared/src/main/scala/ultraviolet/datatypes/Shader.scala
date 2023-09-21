package ultraviolet.datatypes

import ultraviolet.macros.ShaderMacros

import scala.annotation.nowarn

/** A `Shader` is a program that can be run on a graphics card as part of the rendering pipeline.
  */
opaque type Shader[In, Out] = In => Out

object Shader:
  inline def apply[In, Out](f: In => Out): Shader[In, Out] = f
  inline def apply[In](f: In => Unit): Shader[In, Unit]    = f

  @nowarn("msg=discarded")
  inline def apply(body: => Any): Shader[Unit, Unit]       = (_: Unit) => body

  /** `fromFile` allows you to load raw GLSL code from a file at compile time to produce a shader.
    */
  inline def fromFile(inline projectRelativePath: String): Shader[Unit, Unit] =
    Shader {
      ShaderMacros.fromFile(projectRelativePath)
    }

  extension [In, Out](inline ctx: Shader[In, Out])

    inline private def toGLSLWithHeaders[T](headers: List[ShaderHeader], useValidation: Boolean)(using
        p: ShaderPrinter[T]
    ): ShaderResult =
      try
        if useValidation then ShaderMacros.toAST(ctx).render(headers)
        else ShaderMacros.toASTNoValidation(ctx).render(headers)
      catch {
        case e: ShaderError =>
          ShaderResult.Error(e.message)
      }

    inline def toGLSL[T](using ShaderPrinter[T]): ShaderResult =
      toGLSLWithHeaders(Nil, true)
    inline def toGLSL[T](useValidation: Boolean)(using ShaderPrinter[T]): ShaderResult =
      toGLSLWithHeaders(Nil, useValidation)
    inline def toGLSL[T](headers: ShaderHeader*)(using ShaderPrinter[T]): ShaderResult =
      toGLSLWithHeaders(headers.toList, true)
    inline def toGLSL[T](useValidation: Boolean)(headers: ShaderHeader*)(using ShaderPrinter[T]): ShaderResult =
      toGLSLWithHeaders(headers.toList, useValidation)

    inline def run(in: In): Out = ctx(in)

    inline def map[B](f: Out => B): Shader[In, B]                 = (in: In) => f(ctx.run(in))
    inline def flatMap[B](f: Out => Shader[In, B]): Shader[In, B] = (in: In) => f(ctx.run(in)).run(in)
