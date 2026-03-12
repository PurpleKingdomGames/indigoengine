package indigo.render.pipeline.displayprocessing.utils

import indigo.core.assets.AssetName
import indigo.core.datatypes.Vector2
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.TextureRefAndOffset

object TextureLookups:

  def findAssetOffsetValues(
      assetMapping: AssetMapping,
      maybeAssetName: Option[AssetName],
      cacheKey: String,
      cacheSuffix: String
  )(using QuickCache[Vector2], QuickCache[TextureRefAndOffset]): Vector2 =
    QuickCache[Vector2](cacheKey + cacheSuffix) {
      maybeAssetName
        .map { t =>
          lookupTexture(assetMapping, t).offset
        }
        .getOrElse(Vector2.zero)
    }

  def optionalAssetToOffset(assetMapping: AssetMapping, maybeAssetName: Option[AssetName])(using
      QuickCache[TextureRefAndOffset]
  ): Vector2 =
    maybeAssetName match {
      case None =>
        Vector2.zero

      case Some(assetName) =>
        lookupTexture(assetMapping, assetName).offset
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def lookupTexture(assetMapping: AssetMapping, name: AssetName)(using
      QuickCache[TextureRefAndOffset]
  ): TextureRefAndOffset =
    QuickCache("tex-" + name.toString) {
      assetMapping.mappings
        .get(name.toString)
        .getOrElse {
          throw new Exception("Failed to find texture ref + offset for: " + name)
        }
    }
