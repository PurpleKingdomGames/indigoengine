package indigo.render.webgl2

import indigo.core.config.EngineConfig
import indigo.core.datatypes.mutable.CheapMatrix4
import indigo.core.utils.QuickCache
import indigo.render.Renderer
import indigo.render.pipeline.datatypes.ProcessedSceneData
import indigo.scenegraph.Blend
import indigo.scenegraph.BlendFactor
import indigo.shaders.RawShaderCode
import indigoengine.shared.datatypes.RGBA
import indigoengine.shared.datatypes.Radians
import indigoengine.shared.datatypes.Seconds
import indigoengine.webgl2.facades.WebGL2RenderingContext
import indigoengine.webgl2.facades.WebGLVertexArrayObject
import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLFramebuffer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLRenderingContext.*

import scala.scalajs.js.typedarray.Float32Array

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
final class RendererWebGL2(
    config: EngineConfig,
    loadedTextureAssets: scalajs.js.Array[LoadedTextureAsset]
) extends Renderer[ContextAndSize] {

  implicit private val projectionsCache: QuickCache[scalajs.js.Array[Float]] = QuickCache.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var textureLocations: scalajs.js.Array[TextureLookupResult] = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var vertexAndTextureCoordsBuffer: WebGLBuffer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var projectionUBOBuffer: WebGLBuffer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var frameDataUBOBuffer: WebGLBuffer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var cloneReferenceUBOBuffer: WebGLBuffer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var lightDataUBOBuffer: WebGLBuffer = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var vao: WebGLVertexArrayObject = null

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

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var layerRenderInstance: LayerRenderer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var layerMergeRenderInstance: LayerMergeRenderer = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var layerEntityFrameBuffer: FrameBufferComponents.SingleOutput = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var scalingFrameBuffer: FrameBufferComponents.SingleOutput = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var greenDstFrameBuffer: FrameBufferComponents.SingleOutput = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var blueDstFrameBuffer: FrameBufferComponents.SingleOutput = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var emptyFrameBuffer: FrameBufferComponents.SingleOutput = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var greenIsTarget: Boolean = true

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentBlendEq: String = "add"
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentBlendFactors: (BlendFactor, BlendFactor) = (Blend.Normal.src, Blend.Normal.dst)

  private val transparentBlack: RGBA = RGBA.Black.makeTransparent
  private val clearColor: RGBA       = if config.transparentBackground then transparentBlack else config.clearColor

  def initialiseTextureLocations(gl2: WebGL2RenderingContext): Unit =
    textureLocations =
      gl2.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, 1);
      loadedTextureAssets.map { li =>
        new TextureLookupResult(li.name, WebGLHelper.organiseImage(gl2, li.data))
      }

  def initialiseBuffers(gl2: WebGLRenderingContext): Unit =
    vertexAndTextureCoordsBuffer = gl2.createBuffer()
    projectionUBOBuffer = gl2.createBuffer()
    frameDataUBOBuffer = gl2.createBuffer()
    cloneReferenceUBOBuffer = gl2.createBuffer()
    lightDataUBOBuffer = gl2.createBuffer()

  def initialiseVAO(gl2: WebGL2RenderingContext): Unit =
    vao = gl2.createVertexArray()

  def initialiseLayerRenderers(gl2: WebGL2RenderingContext): Unit =
    layerRenderInstance = new LayerRenderer(
      gl2,
      textureLocations,
      config.batchSize,
      projectionUBOBuffer,
      frameDataUBOBuffer,
      cloneReferenceUBOBuffer,
      lightDataUBOBuffer
    ).init()

    layerMergeRenderInstance = new LayerMergeRenderer(gl2, frameDataUBOBuffer)

  def initialiseFrameBuffers(ctx: ContextAndSize): Unit =
    layerEntityFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(ctx.context, ctx.width, ctx.height)
    scalingFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(ctx.context, ctx.width, ctx.height)
    greenDstFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(ctx.context, ctx.width, ctx.height)
    blueDstFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(ctx.context, ctx.width, ctx.height)
    emptyFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(ctx.context, ctx.width, ctx.height)

  def init(ctx: ContextAndSize, shaders: Set[RawShaderCode]): Unit = {
    val gl2 = ctx.context

    initialiseTextureLocations(gl2)
    initialiseBuffers(gl2)
    initialiseVAO(gl2)
    initialiseLayerRenderers(gl2)
    initialiseFrameBuffers(ctx)

    shaders.foreach { shader =>
      if (!customShaders.contains(shader.id.toString))
        customShaders.update(
          shader.id.toString,
          WebGLHelper.shaderProgramSetup(gl2, shader.id.toString, shader)
        )
    }

    val verticesAndTextureCoords: scalajs.js.Array[Float] = {
      val vert0 = scalajs.js.Array[Float](-0.5f, -0.5f, 0.0f, 1.0f)
      val vert1 = scalajs.js.Array[Float](-0.5f, 0.5f, 0.0f, 0.0f)
      val vert2 = scalajs.js.Array[Float](0.5f, -0.5f, 1.0f, 1.0f)
      val vert3 = scalajs.js.Array[Float](0.5f, 0.5f, 1.0f, 0.0f)

      vert0 ++ vert1 ++ vert2 ++ vert3
    }

    gl2.disable(DEPTH_TEST)
    gl2.viewport(0, 0, gl2.drawingBufferWidth.toDouble, gl2.drawingBufferHeight.toDouble)
    gl2.enable(BLEND)

    gl2.bindVertexArray(vao)

    // Vertex
    gl2.bindBuffer(ARRAY_BUFFER, vertexAndTextureCoordsBuffer)
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(verticesAndTextureCoords), STATIC_DRAW)
    gl2.enableVertexAttribArray(0)
    gl2.vertexAttribPointer(
      indx = 0,
      size = 4,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )

    gl2.bindVertexArray(null)
  }

  def setBlendMode(gl2: WebGL2RenderingContext, blend: Blend): Unit = {
    if (blend.op != currentBlendEq) {
      currentBlendEq = blend.op

      blend match {
        case Blend.Add(_, _) =>
          WebGLHelper.setBlendAdd(gl2)

        case Blend.Subtract(_, _) =>
          WebGLHelper.setBlendSubtract(gl2)

        case Blend.ReverseSubtract(_, _) =>
          WebGLHelper.setBlendReverseSubtract(gl2)

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
      WebGLHelper.setBlendFunc(gl2, blend.src, blend.dst)
    }
  }

  def drawScene(ctx: ContextAndSize, sceneData: ProcessedSceneData, runningTime: Seconds): Unit = {
    val gl2 = ctx.context

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
        setBlendMode(gl2, currentBlend)
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
        setBlendMode(gl2, currentBlend)
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
        setBlendMode(gl2, currentBlend)
      }

      // Flip which buffer is the target.
      if (greenIsTarget) {
        greenIsTarget = false
        blitBuffers(gl2, blueDstFrameBuffer.frameBuffer, greenDstFrameBuffer.frameBuffer)
      } else {
        greenIsTarget = true
        blitBuffers(gl2, greenDstFrameBuffer.frameBuffer, blueDstFrameBuffer.frameBuffer)
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
    WebGLHelper.setNormalBlend(gl2)

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

    clearBuffer(gl2, blueDstFrameBuffer.frameBuffer)
    clearBuffer(gl2, greenDstFrameBuffer.frameBuffer)
    clearBuffer(gl2, emptyFrameBuffer.frameBuffer)
  }

  def blitBuffers(gl2: WebGL2RenderingContext, from: WebGLFramebuffer, to: WebGLFramebuffer): Unit = {
    gl2.bindFramebuffer(WebGL2RenderingContext.READ_FRAMEBUFFER, from)
    gl2.bindFramebuffer(WebGL2RenderingContext.DRAW_FRAMEBUFFER, to)
    gl2.blitFramebuffer(0, lastHeight, lastWidth, 0, 0, lastHeight, lastWidth, 0, COLOR_BUFFER_BIT, NEAREST)
  }

  def clearBuffer(gl2: WebGL2RenderingContext, buffer: WebGLFramebuffer): Unit = {
    gl2.bindFramebuffer(WebGL2RenderingContext.DRAW_FRAMEBUFFER, buffer)
    gl2.clear(COLOR_BUFFER_BIT)
  }

  def resize(ctx: ContextAndSize): Unit =
    val gl2 = ctx.context

    val width  = ctx.width
    val height = ctx.height

    if (!resizeRun || (lastWidth != width) || (lastHeight != height)) {
      resizeRun = true
      lastWidth = width
      lastHeight = height

      orthographicProjectionMatrix = CheapMatrix4.orthographic(width.toFloat, height.toFloat)
      defaultLayerProjectionMatrix = orthographicProjectionMatrix.scale(1.0, -1.0, 1.0).toJSArray
      orthographicProjectionMatrixNoMag = CheapMatrix4.orthographic(width.toFloat, height.toFloat).toJSArray
      orthographicProjectionMatrixNoMagFlipped =
        CheapMatrix4.orthographic(width.toFloat, height.toFloat).scale(1.0, -1.0, 1.0).toJSArray

      FrameBufferFunctions.deleteFrameBufferSingle(gl2, layerEntityFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl2, scalingFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl2, greenDstFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl2, blueDstFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl2, emptyFrameBuffer)

      layerEntityFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl2, width, height)
      scalingFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl2, width, height)
      greenDstFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl2, width, height)
      blueDstFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl2, width, height)
      emptyFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl2, width, height)

      gl2.viewport(0, 0, width.toDouble, height.toDouble)

      ()
    }

  def dispose(ctx: ContextAndSize): Unit = {
    val gl2 = ctx.context

    // Reset GL bindings on the shared context so nothing this renderer touched
    // remains live for whatever instance comes next.
    gl2.bindFramebuffer(WebGL2RenderingContext.READ_FRAMEBUFFER, null)
    gl2.bindFramebuffer(WebGL2RenderingContext.DRAW_FRAMEBUFFER, null)
    gl2.bindFramebuffer(FRAMEBUFFER, null)

    // Unbind the texture units used by LayerRenderer / LayerMergeRenderer (0 and 1).
    gl2.activeTexture(TEXTURE0)
    gl2.bindTexture(TEXTURE_2D, null)
    gl2.activeTexture(TEXTURE1)
    gl2.bindTexture(TEXTURE_2D, null)
    gl2.activeTexture(TEXTURE0)

    gl2.bindVertexArray(null)
    gl2.bindBuffer(ARRAY_BUFFER, null)
    gl2.bindBuffer(gl2.UNIFORM_BUFFER, null)
    gl2.useProgram(null)

    // Delete the GPU resources owned by this renderer.
    FrameBufferFunctions.deleteFrameBufferSingle(gl2, layerEntityFrameBuffer)
    FrameBufferFunctions.deleteFrameBufferSingle(gl2, scalingFrameBuffer)
    FrameBufferFunctions.deleteFrameBufferSingle(gl2, greenDstFrameBuffer)
    FrameBufferFunctions.deleteFrameBufferSingle(gl2, blueDstFrameBuffer)
    FrameBufferFunctions.deleteFrameBufferSingle(gl2, emptyFrameBuffer)

    gl2.deleteBuffer(vertexAndTextureCoordsBuffer)
    gl2.deleteBuffer(projectionUBOBuffer)
    gl2.deleteBuffer(frameDataUBOBuffer)
    gl2.deleteBuffer(cloneReferenceUBOBuffer)
    gl2.deleteBuffer(lightDataUBOBuffer)

    gl2.deleteVertexArray(vao)

    customShaders.values.foreach(gl2.deleteProgram)
    customShaders.clear()

    textureLocations.foreach(t => gl2.deleteTexture(t.texture))

    layerRenderInstance.dispose()
    layerMergeRenderInstance.dispose()
  }

}
