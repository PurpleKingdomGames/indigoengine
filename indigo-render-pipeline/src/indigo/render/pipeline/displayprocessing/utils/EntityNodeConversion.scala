package indigo.render.pipeline.displayprocessing.utils

import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Vector2
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.TextureRefAndOffset
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.render.pipeline.datatypes.SpriteSheetFrame
import indigo.render.pipeline.datatypes.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.render.pipeline.displayprocessing.utils.*
import indigo.scenegraph.EntityNode
import indigo.shaders.ShaderData
import indigoengine.shared.collections.Batch

object EntityNodeConversion:

  def sceneEntityToDisplayObject(leaf: EntityNode[?], assetMapping: AssetMapping)(using
      QuickCache[TextureRefAndOffset],
      QuickCache[SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets],
      QuickCache[Batch[Float]]
  ): DisplayObject = {
    val shaderData: ShaderData = leaf.toShaderData

    val channelOffset1 = TextureLookups.optionalAssetToOffset(assetMapping, shaderData.channel1)
    val channelOffset2 = TextureLookups.optionalAssetToOffset(assetMapping, shaderData.channel2)
    val channelOffset3 = TextureLookups.optionalAssetToOffset(assetMapping, shaderData.channel3)

    val bounds = Rectangle(Point.zero, leaf.size)

    val texture =
      shaderData.channel0.map(assetName => TextureLookups.lookupTexture(assetMapping, assetName))

    val frameInfo: SpriteSheetFrameCoordinateOffsets =
      texture match {
        case None =>
          SpriteSheetFrame.defaultOffset

        case Some(texture) =>
          QuickCache(s"${bounds.hashCode().toString}_${shaderData.hashCode().toString}") {
            SpriteSheetFrame.calculateFrameOffset(
              atlasSize = texture.atlasSize,
              frameCrop = bounds,
              textureOffset = texture.offset
            )
          }
      }

    val shaderId = shaderData.shaderId

    val uniformData: Batch[DisplayObjectUniformData] =
      ConversionHelpers.toDisplayObjectUniformData(shaderData)

    DisplayObject(
      x = leaf.position.x.toFloat,
      y = leaf.position.y.toFloat,
      scaleX = leaf.scale.x.toFloat,
      scaleY = leaf.scale.y.toFloat,
      refX = leaf.ref.x.toFloat,
      refY = leaf.ref.y.toFloat,
      flipX = if leaf.flip.horizontal then -1.0 else 1.0,
      flipY = if leaf.flip.vertical then -1.0 else 1.0,
      rotation = leaf.rotation,
      width = bounds.size.width,
      height = bounds.size.height,
      atlasName = texture.map(_.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(channelOffset1),
      channelOffset2 = frameInfo.offsetToCoords(channelOffset2),
      channelOffset3 = frameInfo.offsetToCoords(channelOffset3),
      texturePosition = texture.map(_.offset).getOrElse(Vector2.zero),
      textureSize = texture.map(_.size).getOrElse(Vector2.zero),
      atlasSize = texture.map(_.atlasSize).getOrElse(Vector2.zero),
      shaderId = shaderId,
      shaderUniformData = uniformData
    )
  }
