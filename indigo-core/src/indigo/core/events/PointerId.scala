package indigo.core.events

/** The unique identifier for a pointer input
  */
opaque type PointerId = Double

object PointerId:
  inline def apply(id: Double): PointerId = id
  val unknown                             = PointerId(0)
  given CanEqual[PointerId, PointerId]    = CanEqual.derived

  extension (m: PointerId) inline def toDouble: Double = m
