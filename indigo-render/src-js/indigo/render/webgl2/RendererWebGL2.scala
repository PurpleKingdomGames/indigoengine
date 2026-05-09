package indigo.render.webgl2

import indigo.core.config.EngineConfig
import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.core.utils.QuickCache
import indigo.render.Renderer
import indigo.render.facades.WebGL2RenderingContext
import indigo.render.pipeline.datatypes.ProcessedSceneData
import indigo.scenegraph.Blend
import indigo.scenegraph.BlendFactor
import indigo.shaders.RawShaderCode
import indigoengine.shared.datatypes.RGBA
import indigoengine.shared.datatypes.Radians
import indigoengine.shared.datatypes.Seconds
import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLFramebuffer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLRenderingContext.*

import scala.scalajs.js.typedarray.Float32Array

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
final class RendererWebGL2(
    config: EngineConfig,
    loadedTextureAssets: scalajs.js.Array[LoadedTextureAsset],
    cNc: ContextAndSize
) extends Renderer {

  implicit private val projectionsCache: QuickCache[scalajs.js.Array[Float]] = QuickCache.empty

  private val gl: WebGLRenderingContext =
    cNc.context

  private val gl2: WebGL2RenderingContext =
    gl.asInstanceOf[WebGL2RenderingContext]

  private val textureLocations: scalajs.js.Array[TextureLookupResult] =
    gl.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, 1);
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, WebGLHelper.organiseImage(gl, li.data))
    }

  private val vertexAndTextureCoordsBuffer: WebGLBuffer =
    gl.createBuffer()
  private val projectionUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val frameDataUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val cloneReferenceUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val lightDataUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  private val vao = gl2.createVertexArray()

  private val customShaders: scalajs.js.Dictionary[WebGLProgram] =
    scalajs.js.Dictionary.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastWidth: Int = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastHeight: Int = 0

  // This is the default project, using global magnification
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var defaultLayerProjectionMatrix: scalajs.js.Array[Float] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrixNoMag: scalajs.js.Array[Float] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrixNoMagFlipped: scalajs.js.Array[Float] = null

  def screenWidth: Int  = lastWidth
  def screenHeight: Int = lastHeight

  private val layerRenderInstance: LayerRenderer =
    new LayerRenderer(
      gl2,
      textureLocations,
      config.batchSize,
      projectionUBOBuffer,
      frameDataUBOBuffer,
      cloneReferenceUBOBuffer,
      lightDataUBOBuffer
    ).init()
  private val layerMergeRenderInstance: LayerMergeRenderer =
    new LayerMergeRenderer(gl2, frameDataUBOBuffer)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var layerEntityFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.width, cNc.height)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var scalingFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.width, cNc.height)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var greenDstFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.width, cNc.height)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var blueDstFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.width, cNc.height)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var emptyFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.width, cNc.height)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var greenIsTarget: Boolean = true

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentBlendEq: String = "add"
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentBlendFactors: (BlendFactor, BlendFactor) = (Blend.Normal.src, Blend.Normal.dst)

  private val transparentBlack: RGBA = RGBA.Black.makeTransparent
  private val clearColor: RGBA       = if config.transparentBackground then transparentBlack else config.clearColor

  def init(shaders: Set[RawShaderCode]): Unit = {

    shaders.foreach { shader =>
      if (!customShaders.contains(shader.id.toString))
        customShaders.update(
          shader.id.toString,
          WebGLHelper.shaderProgramSetup(gl, shader.id.toString, shader)
        )
    }

    val verticesAndTextureCoords: scalajs.js.Array[Float] = {
      val vert0 = scalajs.js.Array[Float](-0.5f, -0.5f, 0.0f, 1.0f)
      val vert1 = scalajs.js.Array[Float](-0.5f, 0.5f, 0.0f, 0.0f)
      val vert2 = scalajs.js.Array[Float](0.5f, -0.5f, 1.0f, 1.0f)
      val vert3 = scalajs.js.Array[Float](0.5f, 0.5f, 1.0f, 0.0f)

      vert0 ++ vert1 ++ vert2 ++ vert3
    }

    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, gl.drawingBufferWidth.toDouble, gl.drawingBufferHeight.toDouble)
    gl.enable(BLEND)

    gl2.bindVertexArray(vao)

    // Vertex
    gl.bindBuffer(ARRAY_BUFFER, vertexAndTextureCoordsBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(verticesAndTextureCoords), STATIC_DRAW)
    gl.enableVertexAttribArray(0)
    gl.vertexAttribPointer(
      indx = 0,
      size = 4,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )

    gl2.bindVertexArray(null)
  }

  def setBlendMode(blend: Blend): Unit = {
    if (blend.op != currentBlendEq) {
      currentBlendEq = blend.op

      blend match {
        case Blend.Add(_, _) =>
          WebGLHelper.setBlendAdd(gl)

        case Blend.Subtract(_, _) =>
          WebGLHelper.setBlendSubtract(gl)

        case Blend.ReverseSubtract(_, _) =>
          WebGLHelper.setBlendReverseSubtract(gl)

        case Blend.Min(_, _) =>
          WebGLHelper.setBlendMin(gl2)

        case Blend.Max(_, _) =>
          WebGLHelper.setBlendMax(gl2)

        case Blend.Lighten(_, _) =>
          WebGLHelper.setBlendLighten(gl2)

        case Blend.Darken(_, _) =>
          WebGLHelper.setBlendDarken(gl2)
      }
    }

    val nextBlendPair = (blend.src, blend.dst)
    if (currentBlendFactors != nextBlendPair) {
      currentBlendFactors = nextBlendPair
      WebGLHelper.setBlendFunc(gl, blend.src, blend.dst)
    }
  }

  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = {

    gl2.bindVertexArray(vao)

    val frameData = scalajs.js.Array[Float](runningTime.toFloat, 0.0f, lastWidth.toFloat, lastHeight.toFloat)
    WebGLHelper.attachUBOData(gl2, frameData, frameDataUBOBuffer)

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    var currentBlend: Blend = Blend.Normal

    sceneData.layers.foreach { layer =>
      WebGLHelper.attachUBOData(gl2, layer.lightsData.toJSArray, lightDataUBOBuffer)

      val layerProjection: scalajs.js.Array[Float] =
        layer.camera.orElse(sceneData.camera) match
          case None =>
            orthographicProjectionMatrixNoMag

          case Some(c) =>
            CameraHelper
              .calculateCameraMatrix(
                lastWidth.toDouble,
                lastHeight.toDouble,
                1.0d, // Layers aren't magnified during rendering.
                layer.magnification.map(_.toDouble).getOrElse(1),
                c.position.x.toDouble,
                c.position.y.toDouble,
                c.zoom.toDouble,
                false, // layers aren't flipped
                c.rotation,
                c.isLookAt
              )
              .toJSArray

      WebGLHelper.attachUBOData(gl2, layerProjection, projectionUBOBuffer)

      // Set the entity blend mode
      if (currentBlend != layer.entityBlend) {
        currentBlend = layer.entityBlend
        setBlendMode(currentBlend)
      }

      // Draw the entities onto the layer buffer
      layerRenderInstance.drawLayer(
        sceneData.cloneBlankDisplayObjects.toDictionary,
        layer.entities.toJSArray,
        layerEntityFrameBuffer,
        layer.bgColor,
        customShaders
      )

      val projection =
        layer.magnification match
          case None =>
            defaultLayerProjectionMatrix

          case Some(m) =>
            QuickCache(m.toString + lastWidth.toString + lastHeight.toString) {
              CameraHelper
                .calculateCameraMatrix(
                  lastWidth.toDouble,
                  lastHeight.toDouble,
                  m.toDouble,
                  1.0d, // During merge, we always used a fixed camera, so irrelevant.
                  0,
                  0,
                  1,
                  true,
                  Radians.zero,
                  false
                )
                .toJSArray
            }

      // Clear the blend mode
      if (currentBlend != Blend.Normal) {
        currentBlend = Blend.Normal
        setBlendMode(currentBlend)
      }

      // Merge the layer buffer onto the staging buffer, this clears the magnification
      layerMergeRenderInstance.mergeToStagingBuffer(
        projection,
        layerEntityFrameBuffer,
        scalingFrameBuffer,
        lastWidth,
        lastHeight,
        customShaders
      )

      // Set the layer blend mode
      if (currentBlend != layer.layerBlend) {
        currentBlend = layer.layerBlend
        setBlendMode(currentBlend)
      }

      // Flip which buffer is the target.
      if (greenIsTarget) {
        greenIsTarget = false
        blitBuffers(blueDstFrameBuffer.frameBuffer, greenDstFrameBuffer.frameBuffer)
      } else {
        greenIsTarget = true
        blitBuffers(greenDstFrameBuffer.frameBuffer, blueDstFrameBuffer.frameBuffer)
      }

      // Merge the layer buffer onto the back buffer
      layerMergeRenderInstance.mergeToBackBuffer(
        orthographicProjectionMatrixNoMag,
        scalingFrameBuffer,
        if (!greenIsTarget) blueDstFrameBuffer
        else greenDstFrameBuffer, // Inverted condition, because by now it's flipped.
        lastWidth,
        lastHeight,
        customShaders,
        layer.shaderId,
        layer.shaderUniformData.toJSArray
      )
    }

    // transfer the back buffer to the default framebuffer
    WebGLHelper.setNormalBlend(gl)

    layerMergeRenderInstance.mergeToDefaultFramebuffer(
      orthographicProjectionMatrixNoMagFlipped,
      if (!greenIsTarget) greenDstFrameBuffer else blueDstFrameBuffer, // Inverted condition, because outside the loop.
      lastWidth,
      lastHeight,
      clearColor,
      customShaders,
      sceneData.shaderId,
      sceneData.shaderUniformData.toJSArray
    )

    clearBuffer(blueDstFrameBuffer.frameBuffer)
    clearBuffer(greenDstFrameBuffer.frameBuffer)
    clearBuffer(emptyFrameBuffer.frameBuffer)
  }

  def blitBuffers(from: WebGLFramebuffer, to: WebGLFramebuffer): Unit = {
    gl2.bindFramebuffer(WebGL2RenderingContext.READ_FRAMEBUFFER, from)
    gl2.bindFramebuffer(WebGL2RenderingContext.DRAW_FRAMEBUFFER, to)
    gl2.blitFramebuffer(0, lastHeight, lastWidth, 0, 0, lastHeight, lastWidth, 0, COLOR_BUFFER_BIT, NEAREST)
  }

  def clearBuffer(buffer: WebGLFramebuffer): Unit = {
    gl2.bindFramebuffer(WebGL2RenderingContext.DRAW_FRAMEBUFFER, buffer)
    gl2.clear(COLOR_BUFFER_BIT)
  }

  def resize(width: Int, height: Int): Unit =
    if (!resizeRun || (lastWidth != width) || (lastHeight != height)) {
      resizeRun = true
      lastWidth = width
      lastHeight = height

      orthographicProjectionMatrix = CheapMatrix4.orthographic(width.toFloat, height.toFloat)
      defaultLayerProjectionMatrix = orthographicProjectionMatrix.scale(1.0, -1.0, 1.0).toJSArray
      orthographicProjectionMatrixNoMag = CheapMatrix4.orthographic(width.toFloat, height.toFloat).toJSArray
      orthographicProjectionMatrixNoMagFlipped =
        CheapMatrix4.orthographic(width.toFloat, height.toFloat).scale(1.0, -1.0, 1.0).toJSArray

      FrameBufferFunctions.deleteFrameBufferSingle(gl, layerEntityFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl, scalingFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl, greenDstFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl, blueDstFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl, emptyFrameBuffer)

      layerEntityFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, width, height)
      scalingFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, width, height)
      greenDstFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, width, height)
      blueDstFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, width, height)
      emptyFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, width, height)

      gl.viewport(0, 0, width.toDouble, height.toDouble)

      ()
    }

  def dispose(): Unit = {
    // Reset GL bindings on the shared context so nothing this renderer touched
    // remains live for whatever instance comes next.
    gl2.bindFramebuffer(WebGL2RenderingContext.READ_FRAMEBUFFER, null)
    gl2.bindFramebuffer(WebGL2RenderingContext.DRAW_FRAMEBUFFER, null)
    gl.bindFramebuffer(FRAMEBUFFER, null)

    // Unbind the texture units used by LayerRenderer / LayerMergeRenderer (0 and 1).
    gl.activeTexture(TEXTURE0)
    gl.bindTexture(TEXTURE_2D, null)
    gl.activeTexture(TEXTURE1)
    gl.bindTexture(TEXTURE_2D, null)
    gl.activeTexture(TEXTURE0)

    gl2.bindVertexArray(null)
    gl.bindBuffer(ARRAY_BUFFER, null)
    gl2.bindBuffer(gl2.UNIFORM_BUFFER, null)
    gl.useProgram(null)

    // Delete the GPU resources owned by this renderer.
    FrameBufferFunctions.deleteFrameBufferSingle(gl, layerEntityFrameBuffer)
    FrameBufferFunctions.deleteFrameBufferSingle(gl, scalingFrameBuffer)
    FrameBufferFunctions.deleteFrameBufferSingle(gl, greenDstFrameBuffer)
    FrameBufferFunctions.deleteFrameBufferSingle(gl, blueDstFrameBuffer)
    FrameBufferFunctions.deleteFrameBufferSingle(gl, emptyFrameBuffer)

    gl.deleteBuffer(vertexAndTextureCoordsBuffer)
    gl.deleteBuffer(projectionUBOBuffer)
    gl.deleteBuffer(frameDataUBOBuffer)
    gl.deleteBuffer(cloneReferenceUBOBuffer)
    gl.deleteBuffer(lightDataUBOBuffer)

    gl2.deleteVertexArray(vao)

    customShaders.values.foreach(gl.deleteProgram)
    customShaders.clear()

    textureLocations.foreach(t => gl.deleteTexture(t.texture))

    layerRenderInstance.dispose()
    layerMergeRenderInstance.dispose()
  }

}
