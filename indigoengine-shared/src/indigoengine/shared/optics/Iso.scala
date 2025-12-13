package indigoengine.shared.optics

/** Represents a simple-as-it-gets isomorphic relationship. Sometimes you need to be able to say "I can convert this
  * thing to that thing and back again."
  */
trait Iso[A, B]:
  def to: A => B
  def from: B => A

  def reverse: Iso[B, A] =
    Iso(from, to)

  def >=>[C](that: Iso[B, C]): Iso[A, C] =
    andThen(that)

  def andThen[C](that: Iso[B, C]): Iso[A, C] =
    Iso(
      to.andThen(that.to),
      that.from.andThen(from)
    )

  def <=<[Z](that: Iso[Z, A]): Iso[Z, B] =
    compose(that)

  def compose[Z](that: Iso[Z, A]): Iso[Z, B] =
    Iso(
      to.compose(that.to),
      that.from.compose(from)
    )

  /** Produces a function that modifies A, given a function that modifies B.
    *
    * Example, `Recangle` is isomorphic to a tuple of points, you can represent one as the other losslessly. Sometimes
    * it is convenient to modify one, by first converting it to the other, and then converting it back again. Consider
    * translation:
    *
    * ```
    * val iso: Iso[Rectangle, (Point, Point)] = ???
    * def move(r: Rectangle, by: Int): Recangle =
    *   val movePts = (pts: (Point, Point)) => (pts._1.moveBy(by), pts._2.moveBy(by))
    *   iso.modify(movePts)(r)
    * ```
    */
  def modify(f: B => B): A => A =
    a => from(f(to(a)))

object Iso:

  def apply[A, B](_to: A => B, _from: B => A): Iso[A, B] =
    new Iso[A, B]:
      def to: A => B   = _to
      def from: B => A = _from

  def identity[A]: Iso[A, A] =
    Iso(Predef.identity[A], Predef.identity[A])
