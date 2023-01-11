package ultraviolet.datatypes

import ultraviolet.macros.ShaderMacros

import scala.deriving.Mirror

/** A `Shader` is a program that can be run on a graphics card as part of the rendering pipeline.
  */
opaque type Shader[In, Out] = In => Out

object Shader:
  inline def apply[In, Out](f: In => Out): Shader[In, Out] = f
  inline def apply[In](f: In => Unit): Shader[In, Unit]    = f
  inline def apply(body: => Any): Shader[Unit, Unit]       = (_: Unit) => body

  /** `fromFile` allows you to load raw GLSL code from a file at compile time to produce a shader.
    */
  inline def fromFile(inline projectRelativePath: String): Shader[Unit, Unit] =
    Shader {
      ShaderMacros.fromFile(projectRelativePath)
    }

  extension [In, Out](inline ctx: Shader[In, Out])

    inline def toGLSLDefaultHeaders[T](using p: ShaderPrinter[T]): ShaderResult =
      try ShaderMacros.toAST(ctx).render(p.defaultConfig)
      catch {
        case e: ShaderError =>
          ShaderResult.Error(e.message)
      }
    inline def toGLSLWithHeaders[T](headers: List[PrinterHeader])(using p: ShaderPrinter[T]): ShaderResult =
      try ShaderMacros.toAST(ctx).render(ShaderPrinterConfig(headers))
      catch {
        case e: ShaderError =>
          ShaderResult.Error(e.message)
      }
    inline def toGLSLNoHeaders[T](using ShaderPrinter[T]): ShaderResult =
      try ShaderMacros.toAST(ctx).render(ShaderPrinterConfig.default)
      catch {
        case e: ShaderError =>
          ShaderResult.Error(e.message)
      }
    inline def toGLSL[T](using ShaderPrinter[T]): ShaderResult =
      toGLSLNoHeaders
    inline def toGLSL[T](headers: PrinterHeader*)(using ShaderPrinter[T]): ShaderResult =
      toGLSLWithHeaders(headers.toList)

    inline def run(in: In): Out = ctx(in)

    inline def map[B](f: Out => B): Shader[In, B]                 = (in: In) => f(ctx.run(in))
    inline def flatMap[B](f: Out => Shader[In, B]): Shader[In, B] = (in: In) => f(ctx.run(in)).run(in)
