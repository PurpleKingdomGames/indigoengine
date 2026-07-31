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

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var textureLocations: scalajs.js.Array[TextureLookupResult] = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var vertexAndTextureCoordsBuffer: WebGLBuffer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var projectionUBOBuffer: WebGLBuffer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var frameDataUBOBuffer: WebGLBuffer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var cloneReferenceUBOBuffer: WebGLBuffer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var lightDataUBOBuffer: WebGLBuffer = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
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
  var orthographicProjectionMatrixNoMag: scalajs.js.Array[Float] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrixNoMagFlipped: scalajs.js.Array[Float] = null

  def screenWidth: Int  = lastWidth
  def screenHeight: Int = lastHeight

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var layerRenderInstance: LayerRenderer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var layerMergeRenderInstance: LayerMergeRenderer = null

  // One buffer per magnification. A layer at magnification `m` only covers width/m x height/m game pixels, so it is
  // drawn into a buffer of that size and magnified by the sampler when it is merged onto the accumulator.
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var layerEntityFrameBuffers: Array[FrameBufferComponents.SingleOutput] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var greenDstFrameBuffer: FrameBufferComponents.SingleOutput = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var blueDstFrameBuffer: FrameBufferComponents.SingleOutput = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var emptyFrameBuffer: FrameBufferComponents.SingleOutput = null

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
    layerEntityFrameBuffers = FrameBufferFunctions.createFrameBufferArray(ctx.context, ctx.width, ctx.height)
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
      if !customShaders.contains(shader.id.toString) then
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
    if blend.op != currentBlendEq then {
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

    if currentBlendFactors != nextBlendPair then {
      currentBlendFactors = nextBlendPair
      WebGLHelper.setBlendFunc(gl2, blend.src, blend.dst)
    }
  }

  def drawScene(ctx: ContextAndSize, sceneData: ProcessedSceneData, runningTime: Seconds): Unit = {
    val gl2 = ctx.context

    gl2.bindVertexArray(vao)

    // VIEWPORT_SIZE is the size of whatever is currently being drawn into. For a magnified layer that is the layer's
    // own buffer, which keeps SCREEN_COORDS in game units and so in step with light positions. Merges are always
    // full screen. Only re-uploaded when the size actually changes, so unmagnified scenes still upload once a frame.
    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    var frameDataWidth: Int = 0
    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    var frameDataHeight: Int = 0

    def attachFrameData(width: Int, height: Int): Unit =
      if frameDataWidth != width || frameDataHeight != height then
        frameDataWidth = width
        frameDataHeight = height
        WebGLHelper.attachUBOData(
          gl2,
          scalajs.js.Array[Float](runningTime.toFloat, 0.0f, width.toFloat, height.toFloat),
          frameDataUBOBuffer
        )

    attachFrameData(lastWidth, lastHeight)

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    var currentBlend: Blend = Blend.Normal

    // The buffer holding the scene composited so far. Both accumulators are cleared at the end of the previous
    // frame, so starting on either is valid; blue matches the original first-iteration behaviour.
    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    var currentAccumulator: FrameBufferComponents.SingleOutput = blueDstFrameBuffer
    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    var otherAccumulator: FrameBufferComponents.SingleOutput = greenDstFrameBuffer

    sceneData.layers.foreach { layer =>
      WebGLHelper.attachUBOData(gl2, layer.lightsData.toJSArray, lightDataUBOBuffer)

      val entityFrameBuffer =
        FrameBufferFunctions.selectBufferByMagnification(layer.magnification, layerEntityFrameBuffers)

      // Entities are drawn into a buffer sized to the layer's magnification, so the projection covers the layer's
      // own game space rather than the whole screen, and one game pixel is one texel. The Y flip lives here because
      // the merge onto the accumulator is a straight copy.
      val layerProjection: scalajs.js.Array[Float] =
        layer.camera.orElse(sceneData.camera) match
          case None =>
            QuickCache("layer" + entityFrameBuffer.width.toString + "x" + entityFrameBuffer.height.toString) {
              CheapMatrix4
                .orthographic(entityFrameBuffer.width.toFloat, entityFrameBuffer.height.toFloat)
                .scale(1.0, -1.0, 1.0)
                .toJSArray
            }

          case Some(c) =>
            CameraHelper
              .calculateCameraMatrix(
                entityFrameBuffer.width.toDouble,
                entityFrameBuffer.height.toDouble,
                1.0d, // The buffer is already sized to the magnification.
                1.0d,
                c.position.x.toDouble,
                c.position.y.toDouble,
                c.zoom.toDouble,
                true,
                c.rotation,
                c.isLookAt
              )
              .toJSArray

      WebGLHelper.attachUBOData(gl2, layerProjection, projectionUBOBuffer)

      // Set the entity blend mode
      if currentBlend != layer.entityBlend then {
        currentBlend = layer.entityBlend
        setBlendMode(gl2, currentBlend)
      }

      // Draw the entities onto the layer buffer
      attachFrameData(entityFrameBuffer.width, entityFrameBuffer.height)

      layerRenderInstance.drawLayer(
        sceneData.cloneBlankDisplayObjects.toDictionary,
        layer.entities.toJSArray,
        entityFrameBuffer,
        layer.bgColor,
        customShaders
      )

      attachFrameData(lastWidth, lastHeight)

      // Set the layer blend mode
      if currentBlend != layer.layerBlend then {
        currentBlend = layer.layerBlend
        setBlendMode(gl2, currentBlend)
      }

      // Merge the layer buffer onto the back buffer. The layer buffer is smaller than the back buffer by exactly the
      // layer's magnification, so drawing it over the full screen is what applies the magnification.
      if layer.blendReadsDestination then {
        // A blend material that samples the destination needs a separate, sampleable
        // copy of the accumulated scene, so we ping-pong to the other buffer first.
        blitBuffers(gl2, currentAccumulator.frameBuffer, otherAccumulator.frameBuffer)

        layerMergeRenderInstance.mergeToBackBuffer(
          orthographicProjectionMatrixNoMag,
          entityFrameBuffer,
          currentAccumulator,
          otherAccumulator,
          lastWidth,
          lastHeight,
          customShaders,
          layer.shaderId,
          layer.shaderUniformData.toJSArray
        )

        val previous = currentAccumulator
        currentAccumulator = otherAccumulator
        otherAccumulator = previous
      } else
        // A blend material that never samples the destination can be blended
        // straight onto the accumulator in place.
        layerMergeRenderInstance.mergeToBackBufferInPlace(
          orthographicProjectionMatrixNoMag,
          entityFrameBuffer,
          currentAccumulator,
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
      currentAccumulator,
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

    if !resizeRun || (lastWidth != width) || (lastHeight != height) then {
      resizeRun = true
      lastWidth = width
      lastHeight = height

      orthographicProjectionMatrix = CheapMatrix4.orthographic(width.toFloat, height.toFloat)
      orthographicProjectionMatrixNoMag = CheapMatrix4.orthographic(width.toFloat, height.toFloat).toJSArray
      orthographicProjectionMatrixNoMagFlipped =
        CheapMatrix4.orthographic(width.toFloat, height.toFloat).scale(1.0, -1.0, 1.0).toJSArray

      layerEntityFrameBuffers.foreach(b => FrameBufferFunctions.deleteFrameBufferSingle(gl2, b))
      FrameBufferFunctions.deleteFrameBufferSingle(gl2, greenDstFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl2, blueDstFrameBuffer)
      FrameBufferFunctions.deleteFrameBufferSingle(gl2, emptyFrameBuffer)

      layerEntityFrameBuffers = FrameBufferFunctions.createFrameBufferArray(gl2, width, height)
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
    layerEntityFrameBuffers.foreach(b => FrameBufferFunctions.deleteFrameBufferSingle(gl2, b))
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
