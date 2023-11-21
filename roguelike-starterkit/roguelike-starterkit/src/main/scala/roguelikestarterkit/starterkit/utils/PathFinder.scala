package roguelikestarterkit.utils

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.dice.Dice
import roguelikestarterkit.syntax.toPoints

import scala.annotation.tailrec

/** A simple path finding implementation for a grid. */
final case class PathFinder(area: Rectangle, grid: Batch[GridSquare]):

  def map(f: GridSquare => GridSquare): PathFinder =
    this.copy(grid = grid.map(f))

  def contains(coords: Point): Boolean =
    area.contains(coords)

  def locatePath(
      start: Point,
      end: Point,
      scoreAmount: GridSquare => Int
  ): Batch[Point] =
    PathFinder
      .locatePath(
        start,
        end,
        this.copy(
          grid = PathFinder.scoreGridSquares(start, end, this, scoreAmount)
        )
      )
      .filterNot(_ == start)

  def locatePath(
      start: Point,
      end: Point
  ): Batch[Point] =
    PathFinder
      .locatePath(
        start,
        end,
        this.copy(
          grid = PathFinder.scoreGridSquares(start, end, this, _ => 1)
        )
      )
      .filterNot(_ == start)

  def nextMove(
      start: Point,
      end: Point,
      scoreAmount: GridSquare => Int
  ): Point =
    locatePath(start, end, scoreAmount).headOption.getOrElse(start)

  def nextMove(
      start: Point,
      end: Point
  ): Point =
    locatePath(start, end).headOption.getOrElse(start)

  def withImpassable(blocked: Batch[Point]): PathFinder =
    this.copy(
      grid = grid.map { gs =>
        if blocked.contains(gs.coords) then gs.toBlocked
        else gs
      }
    )
  def withImpassable(blocked: Point*): PathFinder =
    withImpassable(Batch.fromSeq(blocked))

  def withWalkable(accessible: Batch[Point], scoreAmount: GridSquare => Int): PathFinder =
    this.copy(
      grid = grid.map { gs =>
        if accessible.contains(gs.coords) then gs.toWalkable(scoreAmount(gs))
        else gs
      }
    )
  def withWalkable(scoreAmount: GridSquare => Int, accessible: Point*): PathFinder =
    withWalkable(Batch.fromSeq(accessible), scoreAmount)

/** A simple path finding implementation for a grid. */
object PathFinder:

  def fromRectangles(rectangles: Batch[Rectangle]): PathFinder =
    if rectangles.isEmpty then PathFinder(Rectangle.zero, Batch.empty)
    else PathFinder.fromWalkable(Batch.fromSet(rectangles.flatMap(_.toPoints).toSet))

  def fromRectangles(rectangles: Rectangle*): PathFinder =
    fromRectangles(Batch.fromSeq(rectangles))

  def fromRectangle(rectangle: Rectangle): PathFinder =
    fromRectangles(Batch(rectangle))

  def fromWalkable(walkable: Batch[Point]): PathFinder =
    val a    = Rectangle.fromPointCloud(walkable)
    val area = a.resize(a.size + 1)

    val grid: Batch[GridSquare] =
      Batch.fromIndexedSeq(0 until (area.size.width * area.size.height)).map { index =>
        GridSquare.fromIndex(index, area.size.width) match
          case c: Point if walkable.contains(c) =>
            GridSquare.Walkable(index, c, -1)

          case c: Point =>
            GridSquare.Blocked(index, c)
      }

    PathFinder(area, grid)

  def locatePath(start: Point, end: Point, searchGrid: PathFinder): Batch[Point] = {
    val width: Int = searchGrid.area.size.width

    @tailrec
    def rec(
        currentPosition: Point,
        currentScore: Int,
        acc: Batch[Point]
    ): Batch[Point] =
      if (currentPosition == end) acc
      else
        val squares = sampleAt(searchGrid, currentPosition, width).filter(c =>
          c.score != -1 && c.score < currentScore
        )

        squares.sortBy(_.coords.distanceTo(end)).headOption match
          case None =>
            acc

          case Some(next) =>
            rec(next.coords, next.score, acc ++ Batch(next.coords))

    rec(
      start,
      GridSquare.Max,
      Batch(start)
    )
  }

  private[utils] def sampleAt(
      searchGrid: PathFinder,
      coords: Point,
      gridWidth: Int
  ): Batch[GridSquare] =
    Batch(
      coords + GridSquare.relativeUp,
      coords + GridSquare.relativeLeft,
      coords + GridSquare.relativeRight,
      coords + GridSquare.relativeDown
    ).filter(c => searchGrid.contains(c))
      .map(c => searchGrid.grid(GridSquare.toIndex(c, gridWidth)))

  private[utils] def scoreGridSquares(
      start: Point,
      end: Point,
      searchGrid: PathFinder,
      scoreAmount: GridSquare => Int
  ): Batch[GridSquare] = {
    @tailrec
    def rec(
        target: Point,
        unscored: Batch[GridSquare],
        scoreValue: Int,
        lastCoords: Batch[Point],
        scored: Batch[GridSquare]
    ): Batch[GridSquare] =
      (unscored, lastCoords) match
        case (a, b) if a.isEmpty || b.isEmpty =>
          scored ++ unscored

        case (_, last) if last.exists(_ == target) =>
          scored ++ unscored

        case (remainingSquares, lastScoredLocations) =>
          // Find the squares from the remaining pile that the previous scores squares touched.
          val roughEdges: Batch[Batch[GridSquare]] =
            lastScoredLocations.map(c => sampleAt(searchGrid, c, searchGrid.area.size.width))

          // Filter out any squares that aren't in the remainingSquares list
          val edges: Batch[GridSquare] =
            roughEdges.flatMap(_.filter(c => remainingSquares.contains(c)))

          // Deduplicate and score
          val next: Batch[GridSquare] =
            edges
              .foldLeft(Batch.empty[GridSquare]) { (l, x) =>
                if (l.exists(p => p.coords == x.coords)) l else l ++ Batch(x)
              }
              .map(gs => gs.withScore(scoreValue + scoreAmount(gs)))

          rec(
            target = target,
            unscored = remainingSquares.filterNot(p => next.exists(q => q.coords == p.coords)),
            scoreValue = scoreValue + 1,
            lastCoords = next.map(_.coords),
            scored = next ++ scored
          )

    val (done, todo) = searchGrid.grid.partition(_.coords == end)

    rec(start, todo, 0, Batch(end), done.map(_.withScore(0))).sortBy(_.index)
  }

/** A GridSquare represents a position on the gird in the PathFinder implementation. */
enum GridSquare(val score: Int):
  val index: Int
  val coords: Point

  case Walkable(index: Int, coords: Point, weight: Int) extends GridSquare(weight)
  case Blocked(index: Int, coords: Point)               extends GridSquare(GridSquare.Max)

  def withScore(newScore: Int): GridSquare =
    this match
      case gs: Walkable => gs.copy(weight = newScore)
      case gs: Blocked  => gs

  def toBlocked: Blocked =
    this match
      case Walkable(index, coords, _) => Blocked(index, coords)
      case b @ Blocked(_, _)          => b

  def toWalkable(score: Int): Walkable =
    this match
      case w @ Walkable(_, _, _)      => w
      case b @ Blocked(index, coords) => Walkable(index, coords, score)

/** A GridSquare represents a position on the gird in the PathFinder implementation. */
object GridSquare:
  val Max: Int = Int.MaxValue

  val relativeUpLeft: Point    = Point(-1, -1)
  val relativeUp: Point        = Point(0, -1)
  val relativeUpRight: Point   = Point(1, -1)
  val relativeLeft: Point      = Point(-1, 0)
  val relativeRight: Point     = Point(1, 0)
  val relativeDownLeft: Point  = Point(-1, 1)
  val relativeDown: Point      = Point(0, 1)
  val relativeDownRight: Point = Point(1, 1)

  def toIndex(coords: Point, gridWidth: Int): Int =
    coords.x + (coords.y * gridWidth)

  def fromIndex(index: Int, gridWidth: Int): Point =
    Point(
      x = index % gridWidth,
      y = index / gridWidth
    )

  def add(a: Point, b: Point): Point =
    Point(a.x + b.x, a.y + b.y)
