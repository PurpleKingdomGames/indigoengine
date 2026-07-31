package indigo.render.webgl2

import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.shaders.ShaderId
import indigoengine.shared.datatypes.RGBA
import indigoengine.webgl2.facades.WebGL2RenderingContext
import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext.*

import scala.scalajs.js.JSConverters.*

class LayerMergeRenderer(gl2: WebGL2RenderingContext, frameDataUBOBuffer: => WebGLBuffer):

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val customDataUBOBuffers: scalajs.js.Dictionary[WebGLBuffer] =
    scalajs.js.Dictionary.empty[WebGLBuffer]

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  private val displayObjectUBODataSize: Int = 16

  private val uboData: scalajs.js.Array[Float] =
    Array.fill(displayObjectUBODataSize)(0.0f).toJSArray

  // Blends `srcFrameBuffer` onto `targetFrameBuffer` for blend materials that sample the destination. The accumulated
  // scene has already been copied into `targetFrameBuffer`, and `dstFrameBuffer` is the sampleable copy of it.
  def mergeToBackBuffer(
      projection: scalajs.js.Array[Float],
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

    // Switch and reference shader
    val activeShader: WebGLProgram =
      setupActiveShader(projection, width, height, customShaders, shaderId)

    // UBO data
    setupMergeUBOData(activeShader, shaderUniformData)

    // Assign src and dst channels
    WebGLHelper.attach(gl2, activeShader, 0, "SRC_CHANNEL", srcFrameBuffer.diffuse)
    WebGLHelper.attach(gl2, activeShader, 1, "DST_CHANNEL", dstFrameBuffer.diffuse)

    // Draw to framebuffer
    draw()
  }

  // As `mergeToBackBuffer`, but for blend materials that never sample the destination. We bind the accumulation
  // buffer as the target *without clearing* and blend the layer straight onto it via the hardware blend mode, so
  // no destination copy (and no ping-pong blit) is needed. Only SRC_CHANNEL is attached.
  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def mergeToBackBufferInPlace(
      projection: scalajs.js.Array[Float],
      srcFrameBuffer: FrameBufferComponents.SingleOutput,
      targetFrameBuffer: FrameBufferComponents.SingleOutput,
      width: Int,
      height: Int,
      customShaders: scalajs.js.Dictionary[WebGLProgram],
      shaderId: ShaderId,
      shaderUniformData: scalajs.js.Array[DisplayObjectUniformData]
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, targetFrameBuffer, RGBA.Zero, false)

    // Switch and reference shader
    val activeShader: WebGLProgram =
      setupActiveShader(projection, width, height, customShaders, shaderId)

    // UBO data
    setupMergeUBOData(activeShader, shaderUniformData)

    // Assign src channel. To avoid GL Feedback loop errors, we also explicitly unset
    // the DST_CHANNEL.
    WebGLHelper.attach(gl2, activeShader, 0, "SRC_CHANNEL", srcFrameBuffer.diffuse)
    WebGLHelper.attach(gl2, activeShader, 1, "DST_CHANNEL", null)

    // Draw to framebuffer
    draw()
  }

  def mergeToDefaultFramebuffer(
      projection: scalajs.js.Array[Float],
      srcFrameBuffer: FrameBufferComponents.SingleOutput,
      width: Int,
      height: Int,
      clearColor: RGBA,
      customShaders: scalajs.js.Dictionary[WebGLProgram],
      shaderId: ShaderId,
      shaderUniformData: scalajs.js.Array[DisplayObjectUniformData]
  ): Unit = {

    FrameBufferFunctions.switchToDefaultFramebuffer(gl2, width, height, clearColor)

    // Switch and reference shader
    val activeShader: WebGLProgram =
      setupActiveShader(projection, width, height, customShaders, shaderId)

    // UBO data
    setupMergeUBOData(activeShader, shaderUniformData)

    // Assign src and dst channels
    WebGLHelper.attach(gl2, activeShader, 0, "SRC_CHANNEL", srcFrameBuffer.diffuse)

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
      projection: scalajs.js.Array[Float],
      width: Int,
      height: Int,
      customShaders: scalajs.js.Dictionary[WebGLProgram],
      shaderId: ShaderId
  ): WebGLProgram =
    customShaders.get(shaderId.toString) match {
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
        WebGLHelper.attachUBOData(gl2, ud.data.toJSArray, buff)
        WebGLHelper.bindUBO(
          gl2,
          activeShader,
          RendererWebGL2Constants.customDataBlockOffsetPointer + i,
          buff,
          gl2.getUniformBlockIndex(activeShader, ud.blockName)
        )
      }
    }

  private def setupShader(program: WebGLProgram, projection: scalajs.js.Array[Float], width: Int, height: Int): Unit = {

    gl2.useProgram(program)

    uboData(0) = width.toFloat
    uboData(1) = height.toFloat

    WebGLHelper.attachUBOData(gl2, projection ++ uboData, displayObjectUBOBuffer)
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
