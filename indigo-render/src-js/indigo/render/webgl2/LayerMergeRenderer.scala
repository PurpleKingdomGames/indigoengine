package indigo.render.webgl2

import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.shaders.ShaderId
import indigoengine.shared.datatypes.RGBA
import indigoengine.webgl2.facades.WebGL2RenderingContext
import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext.*

import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.typedarray.Float32Array

class LayerMergeRenderer(gl2: WebGL2RenderingContext, frameDataUBOBuffer: => WebGLBuffer):

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val customDataUBOBuffers: scalajs.js.Dictionary[WebGLBuffer] =
    scalajs.js.Dictionary.empty[WebGLBuffer]

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  private val displayObjectUBODataSize: Int = 16

  private val uboData: scalajs.js.Array[Float] =
    Array.fill(displayObjectUBODataSize)(0.0f).toJSArray

  /** Blends `srcFrameBuffer` onto `targetFrameBuffer`, which is bound without clearing so that whatever has been
    * composited so far survives. `dstFrameBuffer` is what the blend shader samples as its destination - the caller
    * decides whether that is a sampleable copy of the accumulated scene or the background buffer, depending on whether
    * the material reads the destination. It must never be `targetFrameBuffer`, or the draw forms a feedback loop.
    */
  def mergeToBackBuffer(
      projection: Float32Array,
      srcFrameBuffer: FrameBufferComponents.SingleOutput,
      dstFrameBuffer: FrameBufferComponents.SingleOutput,
      targetFrameBuffer: FrameBufferComponents.SingleOutput,
      width: Int,
      height: Int,
      customShaders: scalajs.js.Dictionary[WebGLProgram],
      shaderId: ShaderId,
      shaderUniformData: scalajs.js.Array[DisplayObjectUniformData]
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, targetFrameBuffer, RGBA.Zero, false)

    drawMerge(projection, srcFrameBuffer, dstFrameBuffer, width, height, customShaders, shaderId, shaderUniformData)
  }

  /** As `mergeToBackBuffer`, but onto the default framebuffer, which is cleared to `clearColor` first. That makes
    * `clearColor` what the scene is genuinely composited onto, so a buffer holding it is the correct `dstFrameBuffer`
    * for a scene level blend material that samples the destination. Here `width` and `height` set the viewport as well
    * as the quad, which is right because the source is always full screen.
    */
  def mergeToDefaultFramebuffer(
      projection: Float32Array,
      srcFrameBuffer: FrameBufferComponents.SingleOutput,
      dstFrameBuffer: FrameBufferComponents.SingleOutput,
      width: Int,
      height: Int,
      clearColor: RGBA,
      customShaders: scalajs.js.Dictionary[WebGLProgram],
      shaderId: ShaderId,
      shaderUniformData: scalajs.js.Array[DisplayObjectUniformData]
  ): Unit = {

    FrameBufferFunctions.switchToDefaultFramebuffer(gl2, width, height, clearColor)

    drawMerge(projection, srcFrameBuffer, dstFrameBuffer, width, height, customShaders, shaderId, shaderUniformData)
  }

  // Draws the merge quad, at `width` x `height`, into whatever framebuffer is currently bound.
  private def drawMerge(
      projection: Float32Array,
      srcFrameBuffer: FrameBufferComponents.SingleOutput,
      dstFrameBuffer: FrameBufferComponents.SingleOutput,
      width: Int,
      height: Int,
      customShaders: scalajs.js.Dictionary[WebGLProgram],
      shaderId: ShaderId,
      shaderUniformData: scalajs.js.Array[DisplayObjectUniformData]
  ): Unit = {

    // Switch and reference shader
    val activeShader: WebGLProgram =
      setupActiveShader(projection, width, height, customShaders, shaderId)

    // UBO data
    setupMergeUBOData(activeShader, shaderUniformData)

    // Assign src and dst channels
    WebGLHelper.attach(gl2, activeShader, 0, "SRC_CHANNEL", srcFrameBuffer.diffuse)
    WebGLHelper.attach(gl2, activeShader, 1, "DST_CHANNEL", dstFrameBuffer.diffuse)

    draw()
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def draw(): Unit =
    gl2.drawArrays(TRIANGLE_STRIP, 0, 4)
    gl2.bindBuffer(gl2.UNIFORM_BUFFER, null)

    // The layer buffer we just sampled is one the next layer may be drawn straight back into, so the channels have
    // to be released or that draw forms a feedback loop between the framebuffer and an active texture.
    gl2.activeTexture(TEXTURE1)
    gl2.bindTexture(TEXTURE_2D, null)
    gl2.activeTexture(TEXTURE0)
    gl2.bindTexture(TEXTURE_2D, null)

  def dispose(): Unit =
    gl2.deleteBuffer(displayObjectUBOBuffer)
    customDataUBOBuffers.values.foreach(gl2.deleteBuffer)
    customDataUBOBuffers.clear()

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def setupActiveShader(
      projection: Float32Array,
      width: Int,
      height: Int,
      customShaders: scalajs.js.Dictionary[WebGLProgram],
      shaderId: ShaderId
  ): WebGLProgram =
    customShaders.get(shaderId.show) match {
      case Some(s) =>
        setupShader(s, projection, width, height)
        s

      case None =>
        throw new Exception(
          s"Missing blend shader '${shaderId}'. Have you remembered to add the shader to the boot sequence or disabled auto-loading of default shaders?"
        )
    }

  private def setupMergeUBOData(
      activeShader: WebGLProgram,
      shaderUniformData: scalajs.js.Array[DisplayObjectUniformData]
  ): Unit =
    shaderUniformData.zipWithIndex.foreach { case (ud, i) =>
      if (ud.uniformHash.nonEmpty) {
        val buff = customDataUBOBuffers.getOrElseUpdate(ud.uniformHash, gl2.createBuffer())
        WebGLHelper.attachUBOData(gl2, new Float32Array(ud.data.toJSArray), buff)
        WebGLHelper.bindUBO(
          gl2,
          activeShader,
          RendererWebGL2Constants.customDataBlockOffsetPointer + i,
          buff,
          gl2.getUniformBlockIndex(activeShader, ud.blockName)
        )
      }
    }

  private val projectionAndUBOData: Float32Array =
    new Float32Array(16 + displayObjectUBODataSize)

  private def setupShader(program: WebGLProgram, projection: Float32Array, width: Int, height: Int): Unit = {

    gl2.useProgram(program)

    uboData(0) = width.toFloat
    uboData(1) = height.toFloat

    projectionAndUBOData.set(projection, 0)
    projectionAndUBOData.set(uboData, projection.length)

    WebGLHelper.attachUBOData(gl2, projectionAndUBOData, displayObjectUBOBuffer)
    WebGLHelper.bindUBO(
      gl2,
      program,
      RendererWebGL2Constants.mergeObjectBlockPointer,
      displayObjectUBOBuffer,
      gl2.getUniformBlockIndex(program, "IndigoMergeData")
    )
    WebGLHelper.bindUBO(
      gl2,
      program,
      RendererWebGL2Constants.frameDataBlockPointer,
      frameDataUBOBuffer,
      gl2.getUniformBlockIndex(program, "IndigoFrameData")
    )
  }
