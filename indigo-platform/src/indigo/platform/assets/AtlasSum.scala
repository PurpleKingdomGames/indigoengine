package indigo.platform.assets

import indigo.core.datatypes.PowerOfTwo

sealed trait AtlasSum:
  def canAccommodate(requiredSize: PowerOfTwo): Boolean

final case class AtlasTexture(imageRef: ImageRef) extends AtlasSum derives CanEqual:
  def canAccommodate(requiredSize: PowerOfTwo): Boolean = false

final case class AtlasQuadDivision(q1: AtlasQuadTree, q2: AtlasQuadTree, q3: AtlasQuadTree, q4: AtlasQuadTree)
    extends AtlasSum derives CanEqual:
  def canAccommodate(requiredSize: PowerOfTwo): Boolean =
    q1.canAccommodate(requiredSize) || q2.canAccommodate(requiredSize) || q3.canAccommodate(requiredSize) || q4
      .canAccommodate(
        requiredSize
      )

object AtlasQuadDivision:
  def empty(size: PowerOfTwo): AtlasQuadDivision =
    AtlasQuadDivision(AtlasQuadEmpty(size), AtlasQuadEmpty(size), AtlasQuadEmpty(size), AtlasQuadEmpty(size))
