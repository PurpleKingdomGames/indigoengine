package indigo.platform

import indigo.core.events.GlobalEvent
import indigo.platform.assets.AssetCollection

enum IndigoSystemEvent extends GlobalEvent:
  case Rebuild(assetCollection: AssetCollection, nextEvent: GlobalEvent)
