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
      toGLSL(ProgramVersion.GLSL_100)

    inline def toGLSL100(inline headers: List[ShaderHeader]): ShaderResult =
      toGLSL(ProgramVersion.GLSL_100, headers)

    inline def toGLSL300: ShaderResult =
      toGLSL(ProgramVersion.GLSL_300)

    inline def toGLSL300(inline headers: List[ShaderHeader]): ShaderResult =
      toGLSL(ProgramVersion.GLSL_300, headers)

    inline def toGLSL(inline version: ProgramVersion): ShaderResult =
      ShaderMacros.toGLSL(ctx, Nil, version)

    inline def toGLSL(inline version: ProgramVersion, inline headers: List[ShaderHeader]): ShaderResult =
      ShaderMacros.toGLSL(ctx, headers, version)

    inline def run(in: In): Out = ctx(in)

    inline def map[B](f: Out => B): Shader[In, B]                 = (in: In) => f(ctx.run(in))
    inline def flatMap[B](f: Out => Shader[In, B]): Shader[In, B] = (in: In) => f(ctx.run(in)).run(in)
