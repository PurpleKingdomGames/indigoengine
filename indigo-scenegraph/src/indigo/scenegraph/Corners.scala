package indigo.scenegraph

import indigo.core.datatypes.Size

/** Represents the radii of each corner of a Quad, so that they can be set independently.
  */
final case class Corners(topLeft: Int, topRight: Int, bottomRight: Int, bottomLeft: Int) derives CanEqual:

  def withTopLeft(value: Int): Corners =
    this.copy(topLeft = value)
  def withTopRight(value: Int): Corners =
    this.copy(topRight = value)
  def withBottomRight(value: Int): Corners =
    this.copy(bottomRight = value)
  def withBottomLeft(value: Int): Corners =
    this.copy(bottomLeft = value)

  private def clampCorner(max: Int, corner: Int): Int =
    Math.min(max, Math.max(0, corner))

  def clampTo(size: Size): Corners =
    val maxCornerSize = size.halfSize.shortestSide

    Corners(
      clampCorner(maxCornerSize, topLeft),
      clampCorner(maxCornerSize, topRight),
      clampCorner(maxCornerSize, bottomRight),
      clampCorner(maxCornerSize, bottomLeft)
    )

object Corners:

  def zero: Corners =
    Corners(0)

  def apply(all: Int): Corners =
    Corners(all, all, all, all)

  def topLeft(radius: Int): Corners =
    Corners(radius, 0, 0, 0)

  def topRight(radius: Int): Corners =
    Corners(0, radius, 0, 0)

  def bottomRight(radius: Int): Corners =
    Corners(0, 0, radius, 0)

  def bottomLeft(radius: Int): Corners =
    Corners(0, 0, 0, radius)
