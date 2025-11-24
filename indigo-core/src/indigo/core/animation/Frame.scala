package indigo.core.animation

import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle
import indigo.core.datatypes.Size
import indigoengine.shared.datatypes.Millis

final case class Frame(crop: Rectangle, duration: Millis) derives CanEqual:
  def position: Point = crop.position
  def size: Size      = crop.size

object Frame:

  def fromBounds(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Size(width, height)), Millis(1))

  def fromBoundsWithDuration(x: Int, y: Int, width: Int, height: Int, duration: Millis): Frame =
    Frame(Rectangle(Point(x, y), Size(width, height)), duration)
