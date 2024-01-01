package roguelikestarterkit.terminal

import indigo.*
import roguelikestarterkit.FOV

import scala.annotation.tailrec

import scalajs.js

/** */
final class SparseGrid[A](
    val size: Size,
    values: Batch[js.UndefOr[A]]
):
  /** */
  lazy val length: Int = size.width * size.height

  /** */
  def put(
      coords: Point,
      value: A
  ): SparseGrid[A] =
    new SparseGrid(
      size,
      values(SparseGrid.pointToIndex(coords, size.width)) = value
    )

  /** */
  def put(newValues: Batch[(Point, A)]): SparseGrid[A] =
    val arr = values.toJSArray
    newValues.foreach { t =>
      val idx = SparseGrid.pointToIndex(t._1, size.width)
      val tt  = t._2

      if idx < length then arr(idx) = tt
    }

    new SparseGrid(size, Batch(arr))

  /** */
  def put(values: Batch[(Point, A)], offset: Point): SparseGrid[A] =
    put(values.map(p => p._1 + offset -> p._2))

  /** */
  def put(values: (Point, A)*): SparseGrid[A] =
    put(Batch.fromSeq(values))

  /** */
  def fill(value: A): SparseGrid[A] =
    new SparseGrid[A](size, Batch.fill(size.width * size.height)(value))

  /** */
  def get(coords: Point): Option[A] =
    val idx = SparseGrid.pointToIndex(coords, size.width)
    values(idx).toOption

  /** */
  def remove(coords: Point): SparseGrid[A] =
    new SparseGrid(
      size,
      values(SparseGrid.pointToIndex(coords, size.width)) = js.undefined
    )

  /** Empty the grid */
  def clear: SparseGrid[A] =
    SparseGrid(size)

  /** Returns all set values, guarantees order. */
  def toBatch: Batch[Option[A]] =
    values.map(_.toOption)

  /** Returns all set values and a default for any value that is not present, guarantees order. */
  def toBatch(default: A): Batch[A] =
    values.map(v => v.getOrElse(default))

  /** Returns all values in a given region. */
  @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
  def toBatch(region: Rectangle): Batch[A] =
    val count = length
    var i     = 0
    var j     = 0
    val acc   = new js.Array[js.UndefOr[A]](length)

    while i < count do
      if region.contains(SparseGrid.indexToPoint(i, size.width)) then acc(j) = values(i)
      j += 1
      i += 1

    Batch(acc.collect { case p if p.isDefined => p.get })

  /** Returns all values in a given region, or the provided default for any missing values. */
  @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
  def toBatch(region: Rectangle, default: A): Batch[A] =
    val count = length
    var i     = 0
    var j     = 0
    val acc   = new js.Array[js.UndefOr[A]](length)

    while i < count do
      if region.contains(SparseGrid.indexToPoint(i, size.width)) then
        val v = values(i)
        acc(j) = v.getOrElse(default)
      j += 1
      i += 1

    Batch(acc.collect { case p if p.isDefined => p.get })

  /** Returns all set values with their grid positions.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
  def toPositionedBatch: Batch[(Point, A)] =
    val count = length
    var i     = 0
    var j     = 0
    val acc   = new js.Array[js.UndefOr[(Point, A)]](length)

    while i < count do
      val v  = values(i)
      val pt = SparseGrid.indexToPoint(i, size.width)
      if !js.isUndefined(v) then acc(j) = pt -> v.get
      j += 1
      i += 1

    Batch(acc.collect { case p if p.isDefined => p.get })

  /** Returns all values with their grid positions in a given region.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
  def toPositionedBatch(region: Rectangle): Batch[(Point, A)] =
    val count = length
    var i     = 0
    var j     = 0
    val acc   = new js.Array[js.UndefOr[(Point, A)]](length)

    while i < count do
      val v  = values(i)
      val pt = SparseGrid.indexToPoint(i, size.width)
      if !js.isUndefined(v) && region.contains(pt) then acc(j) = pt -> v.get
      j += 1
      i += 1

    Batch(acc.collect { case p if p.isDefined => p.get })

  /** */
  def |+|(other: SparseGrid[A]): SparseGrid[A] =
    combine(other)
  /** */
  def combine(other: SparseGrid[A]): SparseGrid[A] =
    put(other.toPositionedBatch)

  /** */
  def inset(other: SparseGrid[A], offset: Point): SparseGrid[A] =
    put(other.toPositionedBatch, offset)

  /** */
//   def modifyAt(position: Point)(modifier: A => A): SparseGrid[A] =
//     val idx = SparseGrid.pointToIndex(position, size.width)
//     val t   = _values(idx)
//     val mt  = modifier(t)

//     updateAt(idx, mt)
//     this

  /** */
//   @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
//   def map(modifier: (Point, A) => A): SparseGrid[A] =
//     val count = length
//     var i     = 0

//     while i < count do
//       val t  = _values(i)
//       val mt = modifier(SparseGrid.indexToPoint(i, size.width), A(t))

//       updateAt(i, mt.char, mt.foreground, mt.background)

//       i += 1

//     this

  /** */
//   @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
//   def mapRectangle(region: Rectangle)(
//       modifier: (Point, A) => A
//   ): SparseGrid[A] =
//     val count = length
//     var i     = 0

//     while i < count do
//       val pos = SparseGrid.indexToPoint(i, size.width)
//       if region.contains(pos) then
//         val t  = _values(i)
//         val mt = modifier(pos, A(t))

//         updateAt(i, mt.char, mt.foreground, mt.background)

//       i += 1

//     this

  /** */
//   def fillRectangle(region: Rectangle, mapTile: A): SparseGrid[A] =
//     mapRectangle(region)((_, _) => mapTile)
  /** */
//   def fillRectangle(region: Rectangle, value: A): SparseGrid[A] =
//     mapRectangle(region)((_, mt) => mt.withChar(value))
  /** */
//   def fillRectangle(region: Rectangle, value: A, foreground: RGBA): SparseGrid[A] =
//     mapRectangle(region)((_, mt) => A(value, foreground, mt.background))
  /** */
//   def fillRectangle(
//       region: Rectangle,
//       value: A,
//       foreground: RGBA,
//       background: RGBA
//   ): SparseGrid[A] =
//     mapRectangle(region)((_, mt) => A(value, foreground, background))

  /** */
//   @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
//   def mapCircle(circle: Circle)(modifier: (Point, A) => A): SparseGrid[A] =
//     val count = length
//     var i     = 0

//     while i < count do
//       val pos = SparseGrid.indexToPoint(i, size.width)
//       if circle.contains(pos) then
//         val t  = _values(i)
//         val mt = modifier(pos, A(t))

//         updateAt(i, mt.char, mt.foreground, mt.background)

//       i += 1

//     this

  /** */
//   def fillCircle(circle: Circle, mapTile: A): SparseGrid[A] =
//     mapCircle(circle)((_, _) => mapTile)
  /** */
//   def fillCircle(circle: Circle, value: A): SparseGrid[A] =
//     mapCircle(circle)((_, mt) => mt.withChar(value))
  /** */
//   def fillCircle(circle: Circle, value: A, foreground: RGBA): SparseGrid[A] =
//     mapCircle(circle)((_, mt) => A(value, foreground, mt.background))
  /** */
//   def fillCircle(
//       circle: Circle,
//       value: A,
//       foreground: RGBA,
//       background: RGBA
//   ): SparseGrid[A] =
//     mapCircle(circle)((_, mt) => A(value, foreground, background))

  /** */
//   @SuppressWarnings(Array("scalafix:DisableSyntax.while", "scalafix:DisableSyntax.var"))
//   def mapLine(from: Point, to: Point)(
//       modifier: (Point, A) => A
//   ): SparseGrid[A] =
//     val pts   = FOV.bresenhamLine(from, to)
//     val count = length
//     var i     = 0

//     while i < count do
//       val pos = SparseGrid.indexToPoint(i, size.width)
//       if pts.contains(pos) then
//         val t  = _values(i)
//         val mt = modifier(pos, A(t))

//         updateAt(i, mt.char, mt.foreground, mt.background)

//       i += 1

//     this

  /** */
//   def mapLine(line: LineSegment)(modifier: (Point, A) => A): SparseGrid[A] =
//     mapLine(line.start.toPoint, line.end.toPoint)(modifier)
  /** */
//   def fillLine(line: LineSegment, mapTile: A): SparseGrid[A] =
//     mapLine(line.start.toPoint, line.end.toPoint)((_, _) => mapTile)
  /** */
//   def fillLine(line: LineSegment, value: A): SparseGrid[A] =
//     mapLine(line.start.toPoint, line.end.toPoint)((_, mt) => mt.withChar(value))
  /** */
//   def fillLine(line: LineSegment, value: A, foreground: RGBA): SparseGrid[A] =
//     mapLine(line.start.toPoint, line.end.toPoint)((_, mt) =>
//       A(value, foreground, mt.background)
//     )
  /** */
//   def fillLine(
//       line: LineSegment,
//       value: A,
//       foreground: RGBA,
//       background: RGBA
//   ): SparseGrid[A] =
//     mapLine(line.start.toPoint, line.end.toPoint)((_, mt) => A(value, foreground, background))
  /** */
//   def fillLine(from: Point, to: Point, mapTile: A): SparseGrid[A] =
//     mapLine(from, to)((_, _) => mapTile)
  /** */
//   def fillLine(from: Point, to: Point, value: A): SparseGrid[A] =
//     mapLine(from, to)((_, mt) => mt.withChar(value))
  /** */
//   def fillLine(from: Point, to: Point, value: A, foreground: RGBA): SparseGrid[A] =
//     mapLine(from, to)((_, mt) => A(value, foreground, mt.background))
  /** */
//   def fillLine(
//       from: Point,
//       to: Point,
//       value: A,
//       foreground: RGBA,
//       background: RGBA
//   ): SparseGrid[A] =
//     mapLine(from, to)((_, mt) => A(value, foreground, background))

object SparseGrid:

  inline def pointToIndex(point: Point, gridWidth: Int): Int =
    point.x + (point.y * gridWidth)

  inline def indexToPoint(index: Int, gridWidth: Int): Point =
    Point(
      x = index % gridWidth,
      y = index / gridWidth
    )

  def apply[A](size: Size): SparseGrid[A] =
    new SparseGrid[A](
      size,
      Batch(new js.Array[A](size.width * size.height))
    )
