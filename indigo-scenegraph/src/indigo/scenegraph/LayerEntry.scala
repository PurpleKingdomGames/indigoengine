package indigo.scenegraph

import indigo.core.datatypes.LayerKey
import indigo.core.render.Magnification
import indigoengine.shared.collections.Batch

/** Layer entries are holders for Layers. Layer entries are tagged with a `LayerKey`, the purpose of which is that when
  * two SceneUpdateFragements are merged together, two entries will the same layerKey will be combined at the depth of
  * the original. Layer entries also hold the configuration for the layers it looks after.
  */
final case class LayerEntry(key: LayerKey, layer: Layer, config: LayerEntry.Config):
  def withKey(newKey: LayerKey): LayerEntry =
    this.copy(key = newKey)

  def hasKey(layerKey: LayerKey): Boolean =
    key == layerKey

  def withLayer(newLayer: Layer): LayerEntry =
    this.copy(layer = newLayer)

  def withConfig(newConfig: LayerEntry.Config): LayerEntry =
    this.copy(config = newConfig)

  def withMagnification(newMagnification: Magnification): LayerEntry =
    withConfig(config.withMagnification(newMagnification))
  def clearMagnification: LayerEntry =
    withMagnification(Magnification.x1)

  def withVisibility(isVisible: Boolean): LayerEntry =
    withConfig(config.withVisibility(isVisible))
  def show: LayerEntry =
    withVisibility(true)
  def hide: LayerEntry =
    withVisibility(false)

  def modify(f: LayerEntry => LayerEntry): LayerEntry =
    f(this)
  def modifyLayer(pf: PartialFunction[Layer, Layer]): LayerEntry =
    this.copy(layer = layer.modify(pf))

  def toBatch: Batch[Layer.Content] =
    layer.toBatch

object LayerEntry:

  def apply(key: LayerKey, layer: Layer): LayerEntry =
    LayerEntry(key, layer, Config.default)

  def apply(key: LayerKey, layer: Layer, magnification: Magnification): LayerEntry =
    LayerEntry(key, layer, Config.default.withMagnification(magnification))

  /** Contains the configuration for this layer entry.
    *
    * Fields are optional to support left-bias monoidal merging.
    */
  final case class Config(
      magnification: Option[Magnification],
      visible: Option[Boolean]
  ):

    def |+|(other: Config): Config =
      Config(
        this.magnification.orElse(other.magnification),
        this.visible.orElse(other.visible)
      )

    def withMagnification(newMagnification: Magnification): Config =
      this.copy(magnification = Some(newMagnification))
    def clearMagnification: Config =
      this.copy(magnification = None)

    def giveMagnification: Magnification =
      magnification.getOrElse(Magnification.x1)

    def withVisibility(isVisible: Boolean): Config =
      this.copy(visible = Some(isVisible))
    def show: Config =
      withVisibility(true)
    def hide: Config =
      withVisibility(false)

    def isVisible: Boolean =
      visible.getOrElse(true)
    def isHidden: Boolean =
      !isVisible

  object Config:
    val default: Config =
      Config(None, None)
