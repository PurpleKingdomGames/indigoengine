package indigoextras.performers

import indigo.core.events.GlobalEvent
import indigo.scenegraph.LayerKey
import indigoengine.shared.collections.Batch

enum PerformerEvent extends GlobalEvent:
  case Add(layerKey: LayerKey, actor: Performer[?])
  case AddAll(layerKey: LayerKey, actor: Batch[Performer[?]])
  case Remove(id: PerformerId)
  case RemoveAll(ids: Batch[PerformerId])
  case ChangeLayer(id: PerformerId, layerKey: LayerKey)
