package indigo.platform.assets

import indigo.core.datatypes.Point
import indigo.render.pipeline.assets.AtlasId

final case class AtlasIndex(id: AtlasId, offset: Point, size: Point) derives CanEqual
