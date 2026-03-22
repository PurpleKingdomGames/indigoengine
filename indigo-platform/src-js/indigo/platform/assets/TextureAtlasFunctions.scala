package indigo.platform.assets

import indigo.core.assets.AssetName
import indigo.core.datatypes.Point
import indigo.platform.imaging.BlitInstruction
import indigo.platform.imaging.ImageService
import indigo.render.pipeline.assets.AtlasId
import indigoengine.shared.collections.KVP
import org.scalajs.dom.ImageData
import org.scalajs.dom.html

object TextureAtlasFunctions:

  def createAtlasData(
      imageService: ImageService[html.Image, ImageData]
  ): (TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas =
    (textureMap, lookupByName) =>
      val blits =
        textureMap.textureCoords.flatMap { tex =>
          lookupByName(tex.imageRef.name).map { img =>
            BlitInstruction(
              img.data,
              tex.coords.x,
              tex.coords.y,
              tex.imageRef.width,
              tex.imageRef.height
            )
          }
        }

      val imageData: ImageData =
        imageService.composeImage(textureMap.size.value, textureMap.size.value, blits)

      new Atlas(textureMap.size, Option(imageData))

  lazy val convertToTextureAtlas: ((TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas) => (
      AssetName => Option[LoadedImageAsset]
  ) => (AtlasId, List[TextureDetails]) => TextureAtlas = createAtlasFunc =>
    lookupByName =>
      (atlasId, list) =>
        list
          .map(TextureAtlasFunctionsShared.convertTextureDetailsToTree)
          .foldLeft(AtlasQuadTree.identity)(_ + _) match {
          case AtlasQuadEmpty(_) => TextureAtlas.identity
          case n: AtlasQuadNode =>
            val textureMap = n.toTextureMap

            val legend: KVP[AtlasIndex] =
              textureMap.textureCoords.foldLeft(KVP.empty[AtlasIndex]) { (m, t) =>
                val name = t.imageRef.name
                val size = lookupByName(name).map(img => Point(img.data.width, img.data.height)).getOrElse(Point.zero)

                m ++ KVP.empty.add(name.toString -> new AtlasIndex(atlasId, t.coords, size))
              }

            val atlas = createAtlasFunc(textureMap, lookupByName)

            TextureAtlas(
              atlases = KVP.empty.add(
                atlasId.toString -> atlas
              ),
              legend = legend
            )
        }

  lazy val combineTextureAtlases: List[TextureAtlas] => TextureAtlas =
    list => list.foldLeft(TextureAtlas.identity)(_ + _)

  lazy val convertToAtlas: ((TextureMap, AssetName => Option[LoadedImageAsset]) => Atlas) => (
      AssetName => Option[LoadedImageAsset]
  ) => List[List[TextureDetails]] => TextureAtlas = createAtlasFunc =>
    lookupByName =>
      list =>
        combineTextureAtlases(
          list.zipWithIndex
            .map(p =>
              convertToTextureAtlas(createAtlasFunc)(lookupByName)(
                AtlasId(TextureAtlas.IdPrefix + p._2.toString),
                p._1
              )
            )
        )
