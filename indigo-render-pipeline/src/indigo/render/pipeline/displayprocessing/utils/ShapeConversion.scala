package indigo.render.pipeline.displayprocessing.utils

import indigo.core.datatypes.Point
import indigo.core.datatypes.Vector2
import indigo.core.utils.QuickCache
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.render.pipeline.datatypes.SpriteSheetFrame
import indigo.render.pipeline.displayprocessing.utils.*
import indigo.scenegraph.Shape
import indigo.scenegraph.registers.BoundaryLocator
import indigo.shaders.ShaderData
import indigoengine.shared.collections.Batch

object ShapeConversion:

  private val pointZero: Point = Point.zero

  def shapeToDisplayObject(leaf: Shape[?])(using
      QuickCache[Batch[Float]]
  ): DisplayObject = {

    val offset = leaf match
      case s: Shape.Box =>
        val size = s.dimensions.size

        if size.width == size.height then pointZero
        else if size.width < size.height then
          Point(-Math.round((size.height.toDouble - size.width.toDouble) / 2).toInt, 0)
        else Point(0, -Math.round((size.width.toDouble - size.height.toDouble) / 2).toInt)

      case _ =>
        pointZero

    val boundsActual = BoundaryLocator.untransformedShapeBounds(leaf)

    val shaderData: ShaderData = Shape.toShaderData(leaf, boundsActual)
    val bounds                 = boundsActual.toSquare

    val vec2Zero = Vector2.zero
    val uniformData: Batch[DisplayObjectUniformData] =
      ConversionHelpers.toDisplayObjectUniformData(shaderData)

    val offsetRef = leaf.ref - offset

    DisplayObject(
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = offsetRef.x.toFloat,
      refY = offsetRef.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      width = bounds.size.width,
      height = bounds.size.height,
      atlasName = None,
      frame = SpriteSheetFrame.defaultOffset,
      channelOffset1 = vec2Zero,
      channelOffset2 = vec2Zero,
      channelOffset3 = vec2Zero,
      texturePosition = vec2Zero,
      textureSize = vec2Zero,
      atlasSize = vec2Zero,
      shaderId = shaderData.shaderId,
      shaderUniformData = uniformData
    )
  }
