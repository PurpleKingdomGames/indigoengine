package indigoextras.ui.datatypes

import indigo.*

/** Represents a position on the ui grid, rather than a position on the screen.
  */
opaque type Coords = Point

object Coords:

  inline def apply(value: Int): Coords     = Point(value)
  inline def apply(x: Int, y: Int): Coords = Point(x, y)
  inline def apply(point: Point): Coords   = point

  /** Converts a point in screen space (i.e. coordinates aligned with drawn pixels, such as a raw pointer position) back
    * into grid `Coords`. As with `toScreenSpace`, magnification must be taken into consideration explicitly.
    */
  def fromScreenSpace(pt: Point, charSize: Size, magnification: Magnification): Coords =
    Coords(pt / (charSize * magnification.toInt).toPoint)

  val zero: Coords = Coords(0, 0)
  val one: Coords  = Coords(1, 1)

  extension (c: Coords)
    private[datatypes] inline def toPoint: Point = c
    inline def unsafeToPoint: Point              = c
    inline def toDimensions: Dimensions          = Dimensions(c.toSize)

    /** Converts this into a 1:1 local coordinate space shared with the surrounding entities, i.e. magnification is
      * being handled for us, elsewhere. Typically used for rendering entities into layers where magnification will be
      * applied at the surrounding `LayerEntry` level.
      */
    inline def toLocalSpace(charSize: Size): Point = c * charSize.toPoint

    /** Converts this into screen space, i.e. the coordinates align with drawn pixels, e.g. for mouse/pointer hit
      * detection. To do that, magnification must be taken into consideration explicitly.
      */
    inline def toScreenSpace(charSize: Size, magnification: Magnification): Point =
      c * (charSize * magnification.toInt).toPoint

    inline def x: Int = c.x
    inline def y: Int = c.y

    inline def +(other: Coords): Coords = c + other
    inline def +(i: Int): Coords        = c + i
    inline def -(other: Coords): Coords = c - other
    inline def -(i: Int): Coords        = c - i
    inline def *(other: Coords): Coords = c * other
    inline def *(i: Int): Coords        = c * i
    inline def /(other: Coords): Coords = c / other
    inline def /(i: Int): Coords        = c / i

    inline def abs: Coords = c.abs
