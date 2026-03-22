package indigo.platform.assets

import indigo.core.assets.AssetType
import indigo.core.datatypes.BindingKey
import indigo.core.events.AssetEvent
import indigo.core.utils.IndigoLogger
import indigo.platform.events.GlobalEventStream
import indigo.shared.IndigoSystemEvent

object AssetLoader:

  def backgroundLoadAssets(
      globalEventStream: GlobalEventStream,
      assets: Set[AssetType],
      key: BindingKey
      // makeAvailable: Boolean // TODO: What did we lose here from the JS version?
  ): Unit = {
    val assetList: List[AssetType] =
      assets.toList.flatMap(_.toList)

    IndigoLogger.info(s"Background loading ${assetList.length.toString()} assets with key: $key")

    val ac = loadAssets(assets)

    globalEventStream.pushGlobalEvent(
      IndigoSystemEvent.Rebuild(ac, AssetEvent.AssetBatchLoaded(key, assets, true))
    )

    ()
  }

  def loadAssets(assets: Set[AssetType]): AssetCollection = {
    val assetList: List[AssetType] =
      assets.toList.flatMap(_.toList)

    IndigoLogger.info(s"Loading ${assetList.length.toString()} assets")

    new AssetCollection(
      loadImageAssets(filterOutImageAssets(assetList)).toSet,
      loadTextAssets(filterOutTextAssets(assetList)).toSet,
      loadAudioAssets(filterOutAudioAssets(assetList)).toSet
    )
  }

  def filterOutTextAssets(l: List[AssetType]): List[AssetType.Text] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Text => List(t)
        case _                 => Nil
      }
    }

  def filterOutImageAssets(l: List[AssetType]): List[AssetType.Image] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Image => List(t)
        case _                  => Nil
      }
    }

  def filterOutAudioAssets(l: List[AssetType]): List[AssetType.Audio] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Audio => List(t)
        case _                  => Nil
      }
    }

  val loadImageAssets: List[AssetType.Image] => List[LoadedImageAsset] =
    imageAssets => imageAssets.map(loadImageAsset)

  // Images

  def loadImageAsset(imageAsset: AssetType.Image): LoadedImageAsset = {
    IndigoLogger.info(s"[Image] Loading ${imageAsset.path}")
    val p = os.Path(imageAsset.path.show)

    if os.exists(p) then
      val bytes = os.read.bytes(p)
      val res   = new LoadedImageAsset(imageAsset.name, bytes, imageAsset.tag)
      println("Done")
      res
    else throw new RuntimeException(s"Could not load asset at path: ${p}")
  }

  // Text

  val loadTextAssets: List[AssetType.Text] => List[LoadedTextAsset] =
    textAssets => textAssets.map(t => loadTextAsset(t))

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def loadTextAsset(textAsset: AssetType.Text): LoadedTextAsset = {
    IndigoLogger.info(s"[Text] Loading ${textAsset}")
    val p = os.Path(textAsset.path.show)

    if os.exists(p) then
      val txt = os.read(p)
      val res = new LoadedTextAsset(textAsset.name, txt)
      println("Done")
      res
    else throw new RuntimeException(s"Could not load asset at path: ${p}")
  }

  // Audio

  val loadAudioAssets: List[AssetType.Audio] => List[LoadedAudioAsset] =
    audioAssets => audioAssets.map(loadAudioAsset)

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def loadAudioAsset(audioAsset: AssetType.Audio): LoadedAudioAsset = {
    IndigoLogger.info(s"[Audio] Loading ${audioAsset.path}")
    val p = os.Path(audioAsset.path.show)

    if os.exists(p) then
      val bytes = os.read.bytes(p)
      val res   = new LoadedAudioAsset(audioAsset.name, bytes)
      println("Done")
      res
    else throw new RuntimeException(s"Could not load asset at path: ${p}")
  }
