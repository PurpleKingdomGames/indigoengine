package indigo.platform.api

opaque type AtlasId = String
object AtlasId:
  inline def apply(id: String): AtlasId            = id
  extension (aid: AtlasId) inline def show: String = aid
  given CanEqual[AtlasId, AtlasId]                 = CanEqual.derived
  given CanEqual[Option[AtlasId], Option[AtlasId]] = CanEqual.derived
