package indigo.core.events

/** The unique identifier for a finger input
  */
opaque type FingerId = Double

object FingerId:
  inline def apply(id: Double): FingerId = id
  val unknown                            = FingerId(0)
  given CanEqual[FingerId, FingerId]     = CanEqual.derived

  extension (m: FingerId) inline def toDouble: Double = m
