package ultraviolet.datatypes

import ultraviolet.macros.ShaderMacros

import scala.deriving.Mirror

/** A `Shader` is a program that can be run on a graphics card as part of the rendering pipeline.
  */
final case class Shader[In, Out](body: In => Out):
  /** `run` is useful for testing shaders as it allows you to treat the shader like an ordinary function call.
    */
  def run(in: In): Out = body(in)

object Shader:
  inline def apply[In, Out](f: In => Out): Shader[In, Out] =
    new Shader[In, Out](f)

  inline def apply[In](f: In => Unit): Shader[In, Unit] =
    new Shader[In, Unit](f)

  inline def apply(body: => Any): Shader[Unit, Unit] =
    new Shader[Unit, Unit]((_: Unit) => body)

  /** `fromFile` allows you to load raw GLSL code from a file at compile time to produce a shader.
    */
  inline def fromFile(inline projectRelativePath: String): Shader[Unit, Unit] =
    Shader {
      ShaderMacros.fromFile(projectRelativePath)
    }

  extension [In, Out](inline ctx: Shader[In, Out])
    inline def toGLSL[T](using ShaderPrinter[T]): ShaderOutput =
      ShaderMacros.toAST(ctx).render
