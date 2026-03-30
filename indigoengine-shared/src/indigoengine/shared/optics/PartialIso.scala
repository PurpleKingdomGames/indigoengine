package indigoengine.shared.optics

/** Represents an incomplete / illegal `Iso`, where Iso's are total, in a PartialIso the transformation can fail in
  * either direction.
  *
  * The use-case for a partial iso, is to be able to transform and filter.
  */
trait PartialIso[A, B]:
  def to: A => Option[B]
  def from: B => Option[A]

  def reverse: PartialIso[B, A] =
    PartialIso(from, to)

  def >=>[C](that: PartialIso[B, C]): PartialIso[A, C] =
    andThen(that)

  def andThen[C](that: PartialIso[B, C]): PartialIso[A, C] =
    val f: A => Option[C] =
      a => to(a).flatMap(that.to)

    val g: C => Option[A] =
      c => that.from(c).flatMap(from)

    PartialIso(f, g)

  def <=<[Z](that: PartialIso[Z, A]): PartialIso[Z, B] =
    compose(that)

  def compose[Z](that: PartialIso[Z, A]): PartialIso[Z, B] =
    val f: Z => Option[B] =
      z => that.to(z).flatMap(to)

    val g: B => Option[Z] =
      b => from(b).flatMap(that.from)

    PartialIso(f, g)

object PartialIso:

  def apply[A, B](_to: A => Option[B], _from: B => Option[A]): PartialIso[A, B] =
    new PartialIso[A, B]:
      def to: A => Option[B]   = _to
      def from: B => Option[A] = _from

  def identity[A]: PartialIso[A, A] =
    PartialIso(Option.apply, Option.apply)

  def none[A, B]: PartialIso[A, B] =
    PartialIso(_ => None, _ => None)
