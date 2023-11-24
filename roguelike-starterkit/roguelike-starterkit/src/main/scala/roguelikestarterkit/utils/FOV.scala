package roguelikestarterkit.utils

import indigo.*

import scala.annotation.tailrec

/** Helper methods for working out a players field of view. */
object FOV:

  /** An implementation of the Bresenham Line algorithm, which plots clean lines across grids. Used
    * by drawing tools and in this case, for lines of sight.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def bresenhamLine(from: Point, to: Point): Batch[Point] =
    val x0: Int = from.x
    val y0: Int = from.y
    val x1: Int = to.x
    val y1: Int = to.y
    val dx      = Math.abs(x1 - x0)
    val dy      = Math.abs(y1 - y0)
    val sx      = if x0 < x1 then 1 else -1
    val sy      = if y0 < y1 then 1 else -1

    @tailrec
    def rec(x: Int, y: Int, err: Int, acc: Batch[Point]): Batch[Point] =
      val next = Point(x, y)
      if x == x1 && y == y1 then next :: acc
      else if next.distanceTo(to) <= 1 then to :: next :: acc
      else
        var e  = err
        var x2 = x
        var y2 = y

        if err > -dx then
          e -= dy
          x2 += sx

        if e < dy then
          e += dx
          y2 += sy

        rec(x2, y2, e, next :: acc)

    rec(x0, y0, (if dx > dy then dx else -dy) / 2, Batch.empty)
