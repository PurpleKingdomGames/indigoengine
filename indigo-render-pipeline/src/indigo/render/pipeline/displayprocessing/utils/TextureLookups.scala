package indigo.render.pipeline.displayprocessing.utils

import indigo.core.assets.AssetName
import indigo.core.datatypes.Vector2
import indigo.core.utils.QuickCache
import indigo.render.pipeline.assets.AssetMapping
import indigo.render.pipeline.assets.AtlasId
import indigo.render.pipeline.assets.TextureRefAndOffset

object TextureLookups:

  def findAssetOffsetValues(
      assetMapping: AssetMapping,
      maybeAssetName: Option[AssetName],
      cacheKey: String,
      cacheSuffix: String
  )(using QuickCache[Vector2], QuickCache[TextureRefAndOffset]): Vector2 =
    QuickCache[Vector2](cacheKey + cacheSuffix) {
      maybeAssetName match
        case Some(t) =>
          lookupTexture(assetMapping, t).offset

        case None =>
          Vector2.zero
    }

  private val imageTaggingGuidance: String =
    """Channels for a single material must be packed into the same texture atlas.
    |Tag the related image assets together using AssetType.Tagged(\"related-images\")(...) so Indigo keeps them together.
    |""".stripMargin.trim

  /** Validates that all the texture channels of a single material resolve to the same texture atlas.
    */
  def validateChannelAtlases(
      channel0: Option[TextureRefAndOffset],
      channel1: Option[TextureRefAndOffset],
      channel2: Option[TextureRefAndOffset],
      channel3: Option[TextureRefAndOffset]
  ): Option[String] =
    val all: List[AtlasId] =
      List(
        channel0.toList.map(_.atlasName),
        channel1.toList.map(_.atlasName),
        channel2.toList.map(_.atlasName),
        channel3.toList.map(_.atlasName)
      ).flatten

    all match
      case h :: t =>
        if t.forall(_ == h) then None
        else
          Some(
            s"Material channels are split across multiple texture atlases: ${all.map(_.toString()).mkString("[", ", ", "]")}\n$imageTaggingGuidance"
          )
      case Nil =>
        None

  /** Resolves the four material channels via the asset mapping and validates that they all share a single texture
    * atlas.
    */
  def validateChannelAtlases(
      assetMapping: AssetMapping,
      channel0: Option[AssetName],
      channel1: Option[AssetName],
      channel2: Option[AssetName],
      channel3: Option[AssetName]
  )(using QuickCache[TextureRefAndOffset]): Option[String] =
    def resolve(maybeName: Option[AssetName]): Option[TextureRefAndOffset] =
      maybeName.map(name => lookupTexture(assetMapping, name))

    validateChannelAtlases(resolve(channel0), resolve(channel1), resolve(channel2), resolve(channel3))

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
