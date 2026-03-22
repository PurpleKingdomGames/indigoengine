package indigo.internal.assets

import indigo.core.assets.AssetType
import indigo.platform.assets.AssetCollection

import scala.annotation.nowarn
import scala.concurrent.Future
// import scala.concurrent.Promise

object AssetLoader:

  @nowarn("msg=unused")
  def loadAssets(assets: Set[AssetType]): Future[AssetCollection] =
    // val assetList: List[AssetType] =
    //   assets.toList.flatMap(_.toList)

    // IndigoLogger.info(s"Loading ${assetList.length.toString()} assets")

    // for {
    //   t <- loadTextAssets(filterOutTextAssets(assetList))
    //   i <- loadImageAssets(filterOutImageAssets(assetList))
    //   a <- loadAudioAssets(filterOutAudioAssets(assetList))
    // } yield new AssetCollection(i.toSet, t.toSet, a.toSet)
    Future.successful(new AssetCollection(Set(), Set(), Set()))

  // def filterOutTextAssets(l: List[AssetType]): List[AssetType.Text] =
  //   l.flatMap { at =>
  //     at match {
  //       case t: AssetType.Text => List(t)
  //       case _                 => Nil
  //     }
  //   }

  // def filterOutImageAssets(l: List[AssetType]): List[AssetType.Image] =
  //   l.flatMap { at =>
  //     at match {
  //       case t: AssetType.Image => List(t)
  //       case _                  => Nil
  //     }
  //   }

  // def filterOutAudioAssets(l: List[AssetType]): List[AssetType.Audio] =
  //   l.flatMap { at =>
  //     at match {
  //       case t: AssetType.Audio => List(t)
  //       case _                  => Nil
  //     }
  //   }

  // val loadImageAssets: List[AssetType.Image] => Future[List[LoadedImageAsset]] =
  //   imageAssets => Future.sequence(imageAssets.map(loadImageAsset))

  // def onLoadImageFuture(image: HTMLImageElement): Future[HTMLImageElement] =
  //   if (image.complete) Future.successful(image)
  //   else {
  //     val p = Promise[HTMLImageElement]()
  //     image.onload = { (_: Event) =>
  //       p.success(image)
  //     }
  //     image.addEventListener(
  //       "error",
  //       (_: Event) => p.failure(new Exception("Image load error")),
  //       false
  //     )
  //     p.future
  //   }

  // // Images

  // def loadImageAsset(imageAsset: AssetType.Image): Future[LoadedImageAsset] = {
  //   IndigoLogger.info(s"[Image] Loading ${imageAsset.path}")

  //   val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
  //   image.src = imageAsset.path.toString

  //   onLoadImageFuture(image).map { i =>
  //     IndigoLogger.info(s"[Image] Success ${imageAsset.path}")
  //     new LoadedImageAsset(imageAsset.name, i, imageAsset.tag)
  //   }
  // }

  // // Text

  // val loadTextAssets: List[AssetType.Text] => Future[List[LoadedTextAsset]] =
  //   textAssets => Future.sequence(textAssets.map(loadTextAsset))

  // def loadTextAsset(textAsset: AssetType.Text): Future[LoadedTextAsset] = {
  //   IndigoLogger.info(s"[Text] Loading ${textAsset.path}")

  //   fetch(textAsset.path.toString).toFuture.flatMap { response =>
  //     IndigoLogger.info(s"[Text] Success ${textAsset.path}")
  //     response.text().toFuture.map(txt => new LoadedTextAsset(textAsset.name, txt))
  //   }
  // }

  // // Audio

  // val loadAudioAssets: List[AssetType.Audio] => Future[List[LoadedAudioAsset]] =
  //   audioAssets => Future.sequence(audioAssets.map(loadAudioAsset))

  // def loadAudioAsset(audioAsset: AssetType.Audio): Future[LoadedAudioAsset] = {
  //   IndigoLogger.info(s"[Audio] Loading ${audioAsset.path}")

  //   fetch(audioAsset.path.toString).toFuture.flatMap { response =>
  //     IndigoLogger.info(s"[Audio] Success ${audioAsset.path}")
  //     val context = AudioPlayer.giveAudioContext()

  //     response.arrayBuffer().toFuture.flatMap { ab =>
  //       context
  //         .decodeAudioData(
  //           ab,
  //           (audioBuffer: AudioBuffer) => audioBuffer,
  //           () => IndigoLogger.info("Error decoding audio from: " + audioAsset.path)
  //         )
  //         .toFuture
  //         .map(audioBuffer => new LoadedAudioAsset(audioAsset.name, audioBuffer))
  //     }
  //   }
  // }
