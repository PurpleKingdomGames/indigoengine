package tyrian.bridge

opaque type IndigoGameId = String

object IndigoGameId:
  inline def apply(id: String): IndigoGameId    = id
  def unapply(id: IndigoGameId): Option[String] = Some(id)

  given CanEqual[IndigoGameId, IndigoGameId]                 = CanEqual.derived
  given CanEqual[Option[IndigoGameId], Option[IndigoGameId]] = CanEqual.derived

  extension (id: IndigoGameId) inline def value: String = id
