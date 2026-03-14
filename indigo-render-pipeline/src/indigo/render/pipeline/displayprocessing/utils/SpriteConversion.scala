package indigo.render.pipeline.displayprocessing.utils

import indigo.core.animation.AnimationRef
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
import indigo.scenegraph.Sprite
import indigo.scenegraph.registers.BoundaryLocator
import indigoengine.shared.collections.Batch

object SpriteConversion:

  def spriteToDisplayObject(
      boundaryLocator: BoundaryLocator,
      leaf: Sprite[?],
      assetMapping: AssetMapping,
      anim: AnimationRef
  )(using
      QuickCache[Vector2],
      QuickCache[TextureRefAndOffset],
      QuickCache[SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets],
      QuickCache[Batch[Float]]
  ): DisplayObject = {
    val material       = leaf.material
    val shaderData     = material.toShaderData
    val shaderDataHash = toCacheKey(shaderData)
    val materialName   = shaderData.channel0.get

    val emissiveOffset = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
    val normalOffset   = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
    val specularOffset = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

    val texture = TextureLookups.lookupTexture(assetMapping, materialName)

    val frameInfo =
      QuickCache(anim.frameHash + shaderDataHash) {
        SpriteSheetFrame.calculateFrameOffset(
          atlasSize = texture.atlasSize,
          frameCrop = anim.currentFrame.crop,
          textureOffset = texture.offset
        )
      }

    val bounds = boundaryLocator.spriteFrameBounds(leaf).getOrElse(Rectangle.zero)

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
      width = bounds.width,
      height = bounds.height,
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
