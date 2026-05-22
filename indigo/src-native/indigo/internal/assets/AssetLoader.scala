package indigo.internal.assets

import indigo.core.assets.AssetType
import indigo.platform.assets.AssetCollection

import scala.annotation.nowarn
import scala.concurrent.Future

object AssetLoader:

  @nowarn("msg=unused")
  def loadAssets(assets: Set[AssetType]): Future[AssetCollection] =
    Future.successful(new AssetCollection(Set(), Set(), Set()))
