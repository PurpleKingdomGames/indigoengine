package indigo.render.webgl2

import indigo.core.render.Magnification
import indigoengine.shared.datatypes.RGBA
import indigoengine.webgl2.facades.ColorAttachments
import org.scalajs.dom.WebGLFramebuffer
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLRenderingContext.*
import org.scalajs.dom.WebGLTexture

object FrameBufferFunctions:

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def createAndSetupTexture(gl: WebGLRenderingContext, width: Int, height: Int): WebGLTexture = {
    val texture = WebGLHelper.createAndBindTexture(gl)

    gl.texImage2D(
      TEXTURE_2D,
      0,
      WebGLRenderingContext.RGBA,
      width,
      height,
      0,
      WebGLRenderingContext.RGBA,
      UNSIGNED_BYTE,
      null
    )

    texture
  }

  def createFrameBufferSingle(
      gl: WebGLRenderingContext,
      width: Int,
      height: Int
  ): FrameBufferComponents.SingleOutput = {
    import ColorAttachments._

    val frameBuffer: WebGLFramebuffer = gl.createFramebuffer()

    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)

    val diffuse = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, diffuse, 0)

    FrameBufferComponents.SingleOutput(
      frameBuffer,
      diffuse,
      width,
      height
    )
  }

  def createFrameBufferMulti(gl: WebGLRenderingContext, width: Int, height: Int): FrameBufferComponents.MultiOutput = {
    import ColorAttachments._
    // val minTextureCount: Int          = Math.max(0, textureCount)
    val frameBuffer: WebGLFramebuffer = gl.createFramebuffer()

    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)

    val albedo = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, albedo, 0)

    val emissive = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT1, TEXTURE_2D, emissive, 0)

    val normal = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT2, TEXTURE_2D, normal, 0)

    val specular = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT3, TEXTURE_2D, specular, 0)

    FrameBufferComponents.MultiOutput(
      frameBuffer,
      albedo,
      emissive,
      normal,
      specular,
      width,
      height
    )
  }

  private def ceilDiv(value: Int, divisor: Int): Int =
    (value + divisor - 1) / divisor

  /** The framebuffer size needed to hold a layer at each magnification, indexed from magnification 1. A layer at
    * magnification `m` only ever covers `width / m` x `height / m` game pixels, so that is all the space it needs.
    * Rounded up so that a magnification that does not divide the screen evenly is never cropped.
    */
  private[webgl2] def decideBufferSizes(fullWidth: Int, fullHeight: Int): List[(Int, Int)] =
    (1 to Magnification.Max.toInt).toList.map { m =>
      (Math.max(1, ceilDiv(fullWidth, m)), Math.max(1, ceilDiv(fullHeight, m)))
    }

  /** Creates an array of framebuffers, one per magnification, starting at 1 (not 0), magnification cannot be < 1 or >
    * [[Magnification.Max]], so:
    *   - 800x600 @ mag 1 = 800x600
    *   - 800x600 @ mag 2 = 400x300
    *   - 800x600 @ mag 3 = 267x200
    *   - 800x600 @ mag 4 = 200x150
    */
  def createFrameBufferArray(
      gl: WebGLRenderingContext,
      fullWidth: Int,
      fullHeight: Int
  ): Array[FrameBufferComponents.SingleOutput] =
    decideBufferSizes(fullWidth, fullHeight).map { case (w, h) =>
      FrameBufferFunctions.createFrameBufferSingle(gl, w, h)
    }.toArray

  private def clampToRange(value: Int, max: Int): Int =
    Math.min(max, Math.max(0, value))

  private[webgl2] def clampMagnification(magnification: Option[Int], max: Int): Int =
    // -1 to make it zero indexed...
    clampToRange(magnification.getOrElse(1) - 1, max)

  /** The magnification actually used for a layer, after clamping, i.e. the one that selected its buffer. */
  private[webgl2] def effectiveMagnification(magnification: Option[Int], max: Int): Int =
    clampMagnification(magnification, max) + 1

  /** The size the merge quad must be drawn at for one texel of the layer buffer to cover exactly `magnification` screen
    * pixels. Buffer sizes are rounded up, so this can exceed the screen by up to `magnification - 1` pixels, and the
    * overhang is clipped by the viewport.
    */
  private[webgl2] def mergeQuadSize(bufferWidth: Int, bufferHeight: Int, magnification: Int): (Int, Int) =
    (bufferWidth * magnification, bufferHeight * magnification)

  def selectBufferByMagnification(
      magnification: Option[Int],
      buffers: Array[FrameBufferComponents.SingleOutput]
  ): FrameBufferComponents.SingleOutput =
    buffers(clampMagnification(magnification, buffers.length - 1))

  def switchToFramebuffer(
      gl: WebGLRenderingContext,
      frameBuffer: FrameBufferComponents,
      clearColor: RGBA,
      clear: Boolean
  ): Unit = {
    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer.frameBuffer)
    gl.viewport(0, 0, frameBuffer.width.toDouble, frameBuffer.height.toDouble)

    if (clear) {
      gl.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
      gl.clear(COLOR_BUFFER_BIT)
    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def switchToDefaultFramebuffer(gl: WebGLRenderingContext, width: Int, height: Int, clearColor: RGBA): Unit = {
    gl.bindFramebuffer(FRAMEBUFFER, null)
    gl.viewport(0, 0, width.toDouble, height.toDouble)
    gl.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
    gl.clear(COLOR_BUFFER_BIT)
  }

  def deleteFrameBufferSingle(gl: WebGLRenderingContext, fb: FrameBufferComponents.SingleOutput): Unit = {
    gl.deleteTexture(fb.diffuse)
    gl.deleteFramebuffer(fb.frameBuffer)
  }

sealed trait FrameBufferComponents {
  val frameBuffer: WebGLFramebuffer
  val colorAttachments: scalajs.js.Array[Int]
  val width: Int
  val height: Int
}

object FrameBufferComponents:

  final class MultiOutput(
      val frameBuffer: WebGLFramebuffer,
      val albedo: WebGLTexture,
      val emissive: WebGLTexture,
      val normal: WebGLTexture,
      val specular: WebGLTexture,
      val width: Int,
      val height: Int
  ) extends FrameBufferComponents {
    val colorAttachments: scalajs.js.Array[Int] =
      scalajs.js.Array[Int](
        ColorAttachments.COLOR_ATTACHMENT0,
        ColorAttachments.COLOR_ATTACHMENT1,
        ColorAttachments.COLOR_ATTACHMENT2,
        ColorAttachments.COLOR_ATTACHMENT3
      )
  }
  object MultiOutput {
    def apply(
        frameBuffer: WebGLFramebuffer,
        albedo: WebGLTexture,
        emissive: WebGLTexture,
        normal: WebGLTexture,
        specular: WebGLTexture,
        width: Int,
        height: Int
    ): MultiOutput =
      new MultiOutput(frameBuffer, albedo, emissive, normal, specular, width, height)
  }

  final class SingleOutput(
      val frameBuffer: WebGLFramebuffer,
      val diffuse: WebGLTexture,
      val width: Int,
      val height: Int
  ) extends FrameBufferComponents {
    val colorAttachments: scalajs.js.Array[Int] =
      scalajs.js.Array[Int](ColorAttachments.COLOR_ATTACHMENT0)
  }
  object SingleOutput {
    def apply(frameBuffer: WebGLFramebuffer, diffuse: WebGLTexture, width: Int, height: Int): SingleOutput =
      new SingleOutput(frameBuffer, diffuse, width, height)
  }
