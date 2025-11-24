package indigo.physics.simulation

import indigo.core.geometry.BoundingBox
import indigo.physics.Collider

final case class IndexedCollider[Tag](index: Int, previous: Collider[Tag], proposed: Collider[Tag]) derives CanEqual:

  val safeMove: Boolean = previous.boundingBox.overlaps(proposed.boundingBox)

  val movementBounds: BoundingBox =
    if safeMove then proposed.boundingBox
    else BoundingBox.expandToInclude(previous.boundingBox, proposed.boundingBox)
