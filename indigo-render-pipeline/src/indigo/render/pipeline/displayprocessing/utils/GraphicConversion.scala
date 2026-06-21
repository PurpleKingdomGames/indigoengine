package indigo.render.pipeline.displayprocessing.utils

import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Vector2
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.TextureRefAndOffset
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.render.pipeline.datatypes.SpriteSheetFrame
import indigo.render.pipeline.displayprocessing.utils.*
import indigo.render.pipeline.displayprocessing.utils.ToCacheKeySyntax.*
import indigo.scenegraph.Graphic
import indigoengine.shared.collections.Batch

object GraphicConversion:

  def graphicToDisplayObject(leaf: Graphic[?], assetMapping: AssetMapping)(using
      QuickCache[Vector2],
      QuickCache[TextureRefAndOffset],
      QuickCache[SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets],
      QuickCache[Batch[Float]]
  ): DisplayObject = {
    val shaderData     = leaf.material.toShaderData
    val shaderDataHash = toCacheKey(shaderData)
    val materialName   = shaderData.channel0.get

    val emissiveOffset = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
    val normalOffset   = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
    val specularOffset = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

    val texture =
      val t = TextureLookups.lookupTexture(assetMapping, materialName)

      leaf.crop match
        case None =>
          t

        case Some(c) =>
          t.copy(
            offset = t.offset + c.position.toVector2,
            size = c.size.toVector2
          )

    val frameInfo =
      QuickCache(
        s"${leaf.size.hashCode().toString}_${leaf.crop.map(_.hashCode().toString).getOrElse("-")}_$shaderDataHash"
      ) {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = texture.atlasSize,
          frameCrop = Rectangle(leaf.size),
          textureOffset = texture.offset
        )
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
      width = leaf.size.width,
      height = leaf.size.height,
      atlasName = Some(texture.atlasName),
      frame = frameInfo,
      channelOffset1 = frameInfo.offsetToCoords(emissiveOffset),
      channelOffset2 = frameInfo.offsetToCoords(normalOffset),
      channelOffset3 = frameInfo.offsetToCoords(specularOffset),
      texturePosition = texture.offset,
      textureSize = texture.size,
      atlasSize = texture.atlasSize,
      shaderId = shaderId,
      shaderUniformData = uniformData
    )
  }
