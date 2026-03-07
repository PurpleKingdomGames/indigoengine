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
  inline def apply(body: => Any): Shader[Unit, Unit] = (_: Unit) => body

  /** `fromFile` allows you to load raw GLSL code from a file at compile time to produce a shader.
    */
  inline def fromFile(inline projectRelativePath: String): Shader[Unit, Unit] =
    Shader {
      ShaderMacros.fromFile(projectRelativePath)
    }

  extension [In, Out](inline ctx: Shader[In, Out])

    inline def toGLSL100: ShaderResult =
      toGLSL(List(ProgramVersion.GLSL_100), Nil)
        .get(ProgramVersion.GLSL_100.id)
        .getOrElse(
          ShaderResult.Error(
            "GLSL Shader of version 100 was not found, meaning something went wrong during the transpile."
          )
        )

    inline def toGLSL100(headers: ShaderHeader*): ShaderResult =
      toGLSL(List(ProgramVersion.GLSL_100), headers.toList)
        .get(ProgramVersion.GLSL_100.id)
        .getOrElse(
          ShaderResult.Error(
            "GLSL Shader of version 100 was not found, meaning something went wrong during the transpile."
          )
        )

    inline def toGLSL300: ShaderResult =
      toGLSL(List(ProgramVersion.GLSL_300), Nil)
        .get(ProgramVersion.GLSL_300.id)
        .getOrElse(
          ShaderResult.Error(
            "GLSL Shader of version 300 was not found, meaning something went wrong during the transpile."
          )
        )

    inline def toGLSL300(headers: ShaderHeader*): ShaderResult =
      toGLSL(List(ProgramVersion.GLSL_300), headers.toList)
        .get(ProgramVersion.GLSL_300.id)
        .getOrElse(
          ShaderResult.Error(
            "GLSL Shader of version 300 was not found, meaning something went wrong during the transpile."
          )
        )

    inline def toGLSL(versions: List[ProgramVersion]): Map[ProgramVersionId, ShaderResult] =
      toGLSL(versions, Nil)

    inline def toGLSL(
        versions: List[ProgramVersion],
        headers: List[ShaderHeader]
    ): Map[ProgramVersionId, ShaderResult] =
      ShaderMacros.toGLSL(ctx, headers, versions)

    inline def run(in: In): Out = ctx(in)

    inline def map[B](f: Out => B): Shader[In, B]                 = (in: In) => f(ctx.run(in))
    inline def flatMap[B](f: Out => Shader[In, B]): Shader[In, B] = (in: In) => f(ctx.run(in)).run(in)
