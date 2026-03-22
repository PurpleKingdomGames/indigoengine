package indigo.platform.assets

import indigo.core.assets.AssetName
import indigo.core.datatypes.Point
import indigo.render.pipeline.assets.AtlasId

final case class AtlasLookupResult(name: AssetName, atlasId: AtlasId, atlas: Atlas, offset: Point) derives CanEqual
