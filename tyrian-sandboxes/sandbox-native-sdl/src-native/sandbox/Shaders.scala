package sandbox

import indigoengine.sdl.facades.gl.GL.*
import indigoengine.sdl.facades.gl.GLConstants.*
import ultraviolet.syntax.*

import scala.annotation.nowarn
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@nowarn("msg=unused")
@nowarn("msg=unset")
@nowarn("msg=mutated")
object Shaders:

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  final case class VertEnv(gl_VertexID: Int, var gl_Position: vec4)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  inline def vertex: Shader[VertEnv, Unit] =
    Shader[VertEnv, Unit] { env =>
      @out var vUV: vec2 = null

      def main: Unit =
        val pos: array[4, vec2] = array[4, vec2](
          vec2(-1.0f, -1.0f),
          vec2(1.0f, -1.0f),
          vec2(-1.0f, 1.0f),
          vec2(1.0f, 1.0f)
        )
        vUV = pos(env.gl_VertexID) * 0.5f + 0.5f
        env.gl_Position = vec4(pos(env.gl_VertexID), 0.0f, 1.0f)
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  inline def fragment: Shader[Unit, Unit] =
    Shader {
      @in val vUV: vec2                   = null
      @layout(0) @out var fragColor: vec4 = null

      def main: Unit =
        fragColor = vec4(vUV.x, vUV.y, 0.0f, 1.0f)
    }

  val vertSrc: String = vertex.toGLSL410(List(ShaderHeader.Version410Core)).code
  val fragSrc: String = fragment.toGLSL410(List(ShaderHeader.Version410Core)).code

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.null"))
  def compileShader(shaderType: UInt, src: String)(using Zone): UInt =
    val shader = glCreateShader(shaderType)
    val srcPtr = stackalloc[CString]()

    !srcPtr = toCString(src)
    glShaderSource(shader, 1, srcPtr, null)
    glCompileShader(shader)

    val status = stackalloc[CInt]()
    glGetShaderiv(shader, GL_COMPILE_STATUS, status)

    if !status == 0 then
      val logBuf = stackalloc[Byte](512)
      glGetShaderInfoLog(shader, 512, null, logBuf)
      val msg = fromCString(logBuf)
      throw new RuntimeException(
        s"Shader compile error (type ${shaderType.toInt}): $msg"
      )

    shader

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.null"))
  def createProgram(vs: String, fs: String)(using Zone): UInt =
    val vert    = compileShader(GL_VERTEX_SHADER, vs)
    val frag    = compileShader(GL_FRAGMENT_SHADER, fs)
    val program = glCreateProgram()

    glAttachShader(program, vert)
    glAttachShader(program, frag)
    glLinkProgram(program)
    glDeleteShader(vert)
    glDeleteShader(frag)

    val linkStatus = stackalloc[CInt]()
    glGetProgramiv(program, GL_LINK_STATUS, linkStatus)

    if !linkStatus == 0 then
      val logBuf = stackalloc[Byte](512)
      glGetProgramInfoLog(program, 512, null, logBuf)

      val msg = fromCString(logBuf)

      throw new RuntimeException(s"Program link error: $msg")

    program
