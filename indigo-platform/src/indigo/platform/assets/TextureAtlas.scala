package indigo.platform.assets

import indigo.core.assets.AssetName
import indigo.core.datatypes.PowerOfTwo
import indigo.core.utils.IndigoLogger
import indigoengine.shared.collections.KVP

// Output
final case class TextureAtlas(atlases: KVP[Atlas], legend: KVP[AtlasIndex]) derives CanEqual:
  def +(other: TextureAtlas): TextureAtlas =
    TextureAtlas(
      atlases ++ other.atlases,
      legend ++ other.legend
    )

  def lookUpByName(name: AssetName): Option[AtlasLookupResult] =
    legend.get(name.toString).flatMap { i =>
      atlases.get(i.id.toString).map { a =>
        new AtlasLookupResult(name, i.id, a, i.offset)
      }
    }

  def report: String =
    def atlasRecordToString(atlas: (String, Atlas)): String =
      val relevant = legend.toBatch.filter { (k: (String, AtlasIndex)) =>
        k._2.id.toString == atlas._1
      }

      s"Atlas [${atlas._1}] [${atlas._2.size.value.toString()}] contains images: ${relevant.toList.map(_._1).mkString(", ")}"

    val details: String =
      atlases.toBatch.map(atlas => atlasRecordToString(atlas)).mkString("\n  ")

    s"""Atlas details:
    |Number of atlases: ${atlases.keys.toList.length.toString()}
    |Atlases: [
    |  ${details}
    |]
  """.stripMargin

object TextureAtlas:

  import TextureAtlasFunctionsShared._

  val IdPrefix: String = "atlas_"

  val MaxTextureSize: PowerOfTwo = PowerOfTwo._4096

  val supportedSizes: Set[PowerOfTwo] = PowerOfTwo.all

  def createWithMaxSize(
      max: PowerOfTwo,
      imageRefs: List[ImageRef],
      lookupByName: AssetName => Option[LoadedImageAsset],
      createAtlasFunc: (TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas
  ): TextureAtlas =
    (inflateAndSortByPowerOfTwo andThen groupTexturesIntoAtlasBuckets(max) andThen TextureAtlasFunctions.convertToAtlas(
      createAtlasFunc
    )(
      lookupByName
    ))(
      imageRefs
    )

  def create(
      imageRefs: List[ImageRef],
      lookupByName: AssetName => Option[LoadedImageAsset],
      createAtlasFunc: (TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas
  ): TextureAtlas = {
    IndigoLogger.info(
      s"Creating atlases. Max size: ${MaxTextureSize.value.toString()}x${MaxTextureSize.value.toString()}"
    )
    val textureAtlas =
      (inflateAndSortByPowerOfTwo andThen groupTexturesIntoAtlasBuckets(MaxTextureSize) andThen TextureAtlasFunctions
        .convertToAtlas(
          createAtlasFunc
        )(lookupByName))(imageRefs)

    IndigoLogger.info(textureAtlas.report)

    textureAtlas
  }

  val identity: TextureAtlas = TextureAtlas(KVP.empty[Atlas], KVP.empty[AtlasIndex])
