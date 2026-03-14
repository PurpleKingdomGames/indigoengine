package indigo.render.pipeline.displayprocessing.utils

import indigo.core.datatypes.FontChar
import indigo.core.datatypes.FontInfo
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Vector2
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.TextureRefAndOffset
import indigo.render.pipeline.datatypes.DisplayEntity
import indigo.render.pipeline.datatypes.DisplayObject
import indigo.render.pipeline.datatypes.DisplayObjectUniformData
import indigo.render.pipeline.datatypes.SpriteSheetFrame
import indigo.render.pipeline.displayprocessing.utils.*
import indigo.render.pipeline.displayprocessing.utils.ToCacheKeySyntax.*
import indigo.scenegraph.CloneId
import indigo.scenegraph.CloneTileData
import indigo.scenegraph.Text
import indigo.scenegraph.TextLine
import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.mutable
import indigoengine.shared.datatypes.Radians

import scala.annotation.tailrec

object TextConversion:

  def textLineToDisplayObjects(
      leaf: Text[?],
      assetMapping: AssetMapping,
      fontInfo: FontInfo
  )(using
      QuickCache[Vector2],
      QuickCache[TextureRefAndOffset],
      QuickCache[SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets],
      QuickCache[Batch[Float]],
      QuickCache[Batch[DisplayObject]]
  ): (TextLine, Int, Int) => Batch[DisplayEntity] =
    (line, alignmentOffsetX, yOffset) => {

      val material       = leaf.material
      val shaderData     = material.toShaderData
      val shaderDataHash = toCacheKey(shaderData)
      val materialName   = shaderData.channel0.get

      val lineHash: String =
        "[indigo_txt]" +
          leaf.material.hashCode.toString +
          leaf.position.hashCode.toString +
          leaf.scale.hashCode.toString +
          leaf.rotation.hashCode.toString +
          leaf.ref.hashCode.toString +
          leaf.flip.horizontal.toString +
          leaf.flip.vertical.toString +
          leaf.fontKey.toString +
          line.hashCode.toString

      val emissiveOffset = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
      val normalOffset   = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
      val specularOffset = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")

      val texture = TextureLookups.lookupTexture(assetMapping, materialName)

      val shaderId = shaderData.shaderId

      val uniformData: Batch[DisplayObjectUniformData] =
        ConversionHelpers.toDisplayObjectUniformData(shaderData)

      QuickCache(lineHash) {
        zipWithCharDetails(Batch.fromArray(line.text.toCharArray), fontInfo, leaf.letterSpacing).map {
          case (fontChar, xPosition) =>
            val frameInfo =
              QuickCache(fontChar.bounds.hashCode().toString + "_" + shaderDataHash) {
                SpriteSheetFrame.calculateFrameOffset(
                  atlasSize = texture.atlasSize,
                  frameCrop = fontChar.bounds,
                  textureOffset = texture.offset
                )
              }

            DisplayObject(
              x = leaf.position.x.toFloat,
              y = leaf.position.y.toFloat,
              scaleX = leaf.scale.x.toFloat,
              scaleY = leaf.scale.y.toFloat,
              refX = (leaf.ref.x + -(xPosition + alignmentOffsetX)).toFloat,
              refY = (leaf.ref.y - yOffset).toFloat,
              flipX = if leaf.flip.horizontal then -1.0 else 1.0,
              flipY = if leaf.flip.vertical then -1.0 else 1.0,
              rotation = leaf.rotation,
              width = fontChar.bounds.width,
              height = fontChar.bounds.height,
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
      }
    }

  def makeTextCloneDisplayObject(
      leaf: Text[?],
      assetMapping: AssetMapping
  )(using
      QuickCache[Vector2],
      QuickCache[TextureRefAndOffset],
      QuickCache[Batch[Float]],
      QuickCache[DisplayObject]
  ): (CloneId, DisplayObject) = {

    val cloneId: CloneId =
      CloneId(
        "[indigo_txt_clone]" +
          leaf.material.hashCode.toString +
          leaf.position.hashCode.toString +
          leaf.scale.hashCode.toString +
          leaf.rotation.hashCode.toString +
          leaf.ref.hashCode.toString +
          leaf.flip.horizontal.toString +
          leaf.flip.vertical.toString +
          leaf.fontKey.toString
      )

    val clone =
      QuickCache(s"[indigo_text_clone_ref][${cloneId.toString}]") {
        val material       = leaf.material
        val shaderData     = material.toShaderData
        val shaderDataHash = toCacheKey(shaderData)
        val materialName   = shaderData.channel0.get
        val emissiveOffset =
          TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel1, shaderDataHash, "_e")
        val normalOffset = TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel2, shaderDataHash, "_n")
        val specularOffset =
          TextureLookups.findAssetOffsetValues(assetMapping, shaderData.channel3, shaderDataHash, "_s")
        val texture  = TextureLookups.lookupTexture(assetMapping, materialName)
        val shaderId = shaderData.shaderId

        val uniformData: Batch[DisplayObjectUniformData] =
          shaderData.uniformBlocks.map { ub =>
            DisplayObjectUniformData(
              uniformHash = ub.uniformHash,
              blockName = ub.blockName.toString,
              data = PackUBOs.packUBO(ub.uniforms, ub.uniformHash, false)
            )
          }

        val frameInfo =
          SpriteSheetFrame.calculateFrameOffset(
            atlasSize = texture.atlasSize,
            frameCrop = Rectangle.one,
            textureOffset = texture.offset
          )

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
          width = 1,
          height = 1,
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

    (
      cloneId,
      clone
    )
  }

  def textLineToDisplayCloneTileData(
      leaf: Text[?],
      fontInfo: FontInfo
  )(using QuickCache[Batch[CloneTileData]]): (TextLine, Int, Int) => Batch[CloneTileData] =
    (line, alignmentOffsetX, yOffset) => {
      val lineHash: String =
        "[indigo_tln]" +
          leaf.position.hashCode.toString +
          leaf.ref.hashCode.toString +
          leaf.scale.hashCode.toString +
          line.hashCode.toString +
          leaf.fontKey.toString

      QuickCache(lineHash) {
        zipWithCharDetails(Batch.fromArray(line.text.toArray), fontInfo, leaf.letterSpacing).map {
          case (fontChar, xPosition) =>
            CloneTileData(
              x = leaf.position.x + leaf.ref.x + xPosition + alignmentOffsetX,
              y = leaf.position.y + leaf.ref.y + yOffset,
              rotation = Radians.zero,
              scaleX = leaf.scale.x.toFloat,
              scaleY = leaf.scale.y.toFloat,
              cropX = fontChar.bounds.x,
              cropY = fontChar.bounds.y,
              cropWidth = fontChar.bounds.width,
              cropHeight = fontChar.bounds.height
            )
        }
      }
    }

  private def zipWithCharDetails(
      charList: Batch[Char],
      fontInfo: FontInfo,
      letterSpacing: Int
  ): Batch[(FontChar, Int)] = {
    @tailrec
    def rec(
        remaining: Batch[(Char, FontChar)],
        nextX: Int,
        acc: mutable.Batch[(FontChar, Int)]
    ): mutable.Batch[(FontChar, Int)] =
      if remaining.isEmpty then acc
      else
        val x  = remaining.head
        val xs = remaining.tail
        acc += ((x._2, nextX))
        val ls = if xs.isEmpty then 0 else letterSpacing
        rec(xs, nextX + x._2.bounds.width + ls, acc)

    rec(charList.map(c => (c, fontInfo.findByCharacter(c))), 0, mutable.Batch.empty).toBatch
  }
