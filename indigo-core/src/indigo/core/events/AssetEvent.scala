package indigo.core.events

import indigo.core.assets.AssetType
import indigo.core.datatypes.BindingKey

/** Events relating to dynamically loading assets after the game has started.
  *
  * These events are the underlying events used by the `AssetBundleLoader` `SubSystem`, which makes loading assets a
  * slightly more pleasant experience.
  */
enum AssetEvent extends GlobalEvent:

  /** Load a batch of assets
    *
    * You can load assets without a key if you just want them in the asset pool for future use, or you can specify a key
    * so that you can track them as they come in.
    *
    * You can also decide whether to force the assets to be available or not. If they are available, then on load
    * they're immediately added to the asset registers. If not then they are downloaded locally cached, but not added.
    *
    * @param assets
    *   a set of `AssetType`s to load
    * @param key
    *   A tracking key.
    * @param makeAvailable
    *   Make the asset available to the game, or just download it to local cache.
    */
  case LoadAssets(assets: Set[AssetType], key: BindingKey, makeAvailable: Boolean)

  /** The response event to `LoadAsset` or `LoadAssetBatch`.
    *
    * @param key
    *   The requested tracking key
    * @param assets
    *   The assets that were loaded
    * @param available
    *   Whether or not the asset has been made available for the game to use.
    */
  case AssetBatchLoaded(key: BindingKey, assets: Set[AssetType], available: Boolean)

  /** If an error occurs during load, the game will be sent this event.
    *
    * @param key
    *   The requested tracking key so you know which event failed.
    * @param message
    *   Asset load error message
    */
  case AssetBatchLoadError(key: BindingKey, message: String)
