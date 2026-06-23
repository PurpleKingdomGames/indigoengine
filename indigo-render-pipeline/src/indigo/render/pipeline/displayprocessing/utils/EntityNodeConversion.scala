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

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def sceneEntityToDisplayObject(leaf: EntityNode[?], assetMapping: AssetMapping)(using
      QuickCache[TextureRefAndOffset],
      QuickCache[SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets],
      QuickCache[Batch[Float]]
  ): DisplayObject = {
    val shaderData: ShaderData = leaf.toShaderData

    val channel0 = shaderData.channel0.map(name => TextureLookups.lookupTexture(assetMapping, name))
    val channel1 = shaderData.channel1.map(name => TextureLookups.lookupTexture(assetMapping, name))
    val channel2 = shaderData.channel2.map(name => TextureLookups.lookupTexture(assetMapping, name))
    val channel3 = shaderData.channel3.map(name => TextureLookups.lookupTexture(assetMapping, name))

    TextureLookups.validateChannelAtlases(channel0, channel1, channel2, channel3).foreach { msg =>
      throw new Exception(msg)
    }

    val channelOffset1 = channel1.fold(Vector2.zero)(_.offset)
    val channelOffset2 = channel2.fold(Vector2.zero)(_.offset)
    val channelOffset3 = channel3.fold(Vector2.zero)(_.offset)

    val bounds = Rectangle(Point.zero, leaf.size)

    val frameInfo: SpriteSheetFrameCoordinateOffsets =
      channel0 match {
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

    val (txPos, txSize, aSize, atlasName) =
      channel0 match
        case None =>
          (Vector2.zero, Vector2.zero, Vector2.zero, None)

        case Some(tx) =>
          (tx.offset, tx.size, tx.atlasSize, Some(tx.atlasName))

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
      atlasName = atlasName,
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(channelOffset1),
      channelOffset2 = frameInfo.offsetToCoords(channelOffset2),
      channelOffset3 = frameInfo.offsetToCoords(channelOffset3),
      texturePosition = txPos,
      textureSize = txSize,
      atlasSize = aSize,
      shaderId = shaderId,
      shaderUniformData = uniformData
    )
  }
