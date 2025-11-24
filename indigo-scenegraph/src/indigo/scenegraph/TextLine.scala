package indigo.scenegraph

import indigo.core.datatypes.Point
import indigo.core.datatypes.Rectangle

/** Represents a single line of text.
  */
final case class TextLine(text: String, lineBounds: Rectangle):
  def moveTo(x: Int, y: Int): TextLine =
    moveTo(Point(x, y))
  def moveTo(newPosition: Point): TextLine =
    this.copy(lineBounds = lineBounds.moveTo(newPosition))
