package indigoengine.shared.collections

import scala.annotation.tailrec
import scala.collection.immutable.Vector
import scala.reflect.ClassTag
import scala.util.control.NonFatal

/** Batch is a really thin wrapper over `Vector` to replace `List` on the Indigo APIs. Its purpose is to provide fast
  * scene construction and fast conversion back to Vector for the engine to use. Most operations that require any sort
  * of traversal are performed by flattening the structure and delegated to `Vector`. In practice, scene construction is
  * mostly about building the structure, so the penalty is acceptable, and still faster than using `List`.
  */
sealed trait Batch[+A] extends BatchOps[A]:
  private lazy val _underlying: Vector[A] = toVector

  def head: A
  def headOption: Option[A]
  def last: A
  def lastOption: Option[A]
  def isEmpty: Boolean
  def size: Int
  def toVector: Vector[A]

  def length: Int                  = size
  def lengthCompare(len: Int): Int = _underlying.lengthCompare(len)

  def ++[B >: A](other: Batch[B]): Batch[B] =
    if this.isEmpty then other
    else if other.isEmpty then this
    else Batch.Combine(this, other)

  def |+|[B >: A](other: Batch[B]): Batch[B] =
    this ++ other

  def ::[B >: A](value: B): Batch[B] =
    Batch(value) ++ this

  def +:[B >: A](value: B): Batch[B] =
    Batch(value) ++ this

  def :+[B >: A](value: B): Batch[B] =
    this ++ Batch(value)

  def apply(index: Int): A =
    _underlying(index)

  def collect[B >: A, C](f: PartialFunction[B, C]): Batch[C] =
    Batch.Wrapped(_underlying.collect(f))

  def collectFirst[B >: A, C](f: PartialFunction[B, C]): Option[C] =
    _underlying.collectFirst(f)

  def compact[B >: A]: Batch.Wrapped[B] =
    Batch.Wrapped(_underlying)

  def contains[B >: A](p: B): Boolean =
    given CanEqual[B, B] = CanEqual.derived
    _underlying.exists(_ == p)

  def distinct: Batch[A] =
    Batch.fromVector(_underlying.distinct)

  def distinctBy[B](f: A => B): Batch[A] =
    Batch.fromVector(_underlying.distinctBy(f))

  def take(n: Int): Batch[A] =
    Batch.Wrapped(_underlying.take(n))

  def takeRight(n: Int): Batch[A] =
    Batch.Wrapped(_underlying.takeRight(n))

  def takeWhile(p: A => Boolean): Batch[A] =
    Batch.Wrapped(_underlying.takeWhile(p))

  def drop(count: Int): Batch[A] =
    Batch.Wrapped(_underlying.drop(count))

  def dropRight(count: Int): Batch[A] =
    Batch.Wrapped(_underlying.dropRight(count))

  def dropWhile(p: A => Boolean): Batch[A] =
    Batch.Wrapped(_underlying.dropWhile(p))

  def exists(p: A => Boolean): Boolean =
    _underlying.exists(p)

  def find(p: A => Boolean): Option[A] =
    _underlying.find(p)

  def filter(p: A => Boolean): Batch[A] =
    Batch.Wrapped(_underlying.filter(p))

  def filterNot(p: A => Boolean): Batch[A] =
    Batch.Wrapped(_underlying.filterNot(p))

  def flatMap[B](f: A => Batch[B]): Batch[B] =
    Batch.Wrapped(toVector.flatMap(v => f(v).toVector))

  def flatten[B](using asBatch: A => Batch[B]): Batch[B] =
    flatMap(asBatch)

  def forall(p: A => Boolean): Boolean =
    _underlying.forall(p)

  def fold[B >: A](z: B)(f: (B, B) => B): B =
    _underlying.fold(z)(f)

  def foldLeft[B](z: B)(f: (B, A) => B): B =
    _underlying.foldLeft(z)(f)

  def foldRight[B](z: B)(f: (A, B) => B): B =
    _underlying.foldRight(z)(f)

  def foreach(f: A => Unit): Unit =
    _underlying.foreach(f)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def foreachWithIndex(f: (A, Int) => Unit): Unit =
    var idx: Int = 0
    foreach { v =>
      f(v, idx)
      idx = idx + 1
    }

  def groupBy[K](f: A => K): Map[K, Batch[A]] =
    _underlying.groupBy(f).map(p => (p._1, Batch.fromVector(p._2)))

  def grouped(size: Int): Batch[Batch[A]] =
    Batch.fromIterator(
      _underlying.grouped(size).map(ar => Batch.fromVector(ar))
    )

  def insert[B >: A](index: Int, value: B): Batch[B] =
    val p = _underlying.splitAt(index)
    Batch.fromVector((p._1 :+ value) ++ p._2)

  def replace[B >: A](index: Int, value: B): Batch[B] =
    val p = _underlying.splitAt(index)
    Batch.fromVector((p._1 :+ value) ++ p._2.drop(1))

  def lift(index: Int): Option[A] =
    _underlying.lift(index)

  def padTo[B >: A](len: Int, elem: B): Batch[B] =
    Batch.fromVector(_underlying.padTo(len, elem))

  def partition(p: A => Boolean): (Batch[A], Batch[A]) =
    val (a, b) = _underlying.partition(p)
    (Batch.Wrapped(a), Batch.Wrapped(b))

  def map[B](f: A => B): Batch[B] =
    Batch.Wrapped(_underlying.map(f))

  def maxBy[B](f: A => B)(using ord: Ordering[B]): A =
    _underlying.maxBy(f)

  def maxByOption[B](f: A => B)(using ord: Ordering[B]): Option[A] =
    Option.when(_underlying.nonEmpty)(_underlying.maxBy(f))

  def minBy[B](f: A => B)(using ord: Ordering[B]): A =
    _underlying.minBy(f)

  def minByOption[B](f: A => B)(using ord: Ordering[B]): Option[A] =
    Option.when(_underlying.nonEmpty)(_underlying.minBy(f))

  /** Converts the batch into a String`
    * @return
    *   `String`
    */
  def mkString: String =
    toVector.mkString

  /** Converts the batch into a String
    * @param separator
    *   A string to add between the elements
    * @return
    *   `String`
    */
  def mkString(separator: String): String =
    toVector.mkString(separator)

  /** Converts the batch into a String
    * @param prefix
    *   A string to add before the elements
    * @param separator
    *   A string to add between the elements
    * @param suffix
    *   A string to add after the elements
    * @return
    *   `String`
    */
  def mkString(prefix: String, separator: String, suffix: String): String =
    toVector.mkString(prefix, separator, suffix)

  def nonEmpty: Boolean =
    !isEmpty

  def reduce[B >: A](f: (B, B) => B): B =
    _underlying.reduce(f)

  def reverse: Batch[A] =
    Batch.Wrapped(_underlying.reverse)

  def sortBy[B](f: A => B)(implicit ord: Ordering[B]): Batch[A] =
    Batch.Wrapped(_underlying.sortBy(f))

  def sorted[B >: A](implicit ord: Ordering[B]): Batch[A] =
    Batch.Wrapped(_underlying.sorted)

  def sortWith(f: (A, A) => Boolean): Batch[A] =
    Batch.Wrapped(_underlying.sortWith(f))

  def splitAt(n: Int): (Batch[A], Batch[A]) =
    val p = _underlying.splitAt(n)
    (Batch.Wrapped(p._1), Batch.Wrapped(p._2))

  def sum[B >: A](implicit num: Numeric[B]): B =
    _underlying.sum

  def tail: Batch[A] =
    Batch.Wrapped(_underlying.tail)

  def tailOrEmpty: Batch[A] =
    if _underlying.isEmpty then Batch.empty
    else Batch.Wrapped(_underlying.tail)

  def tailOption: Option[Batch[A]] =
    if _underlying.isEmpty then None
    else Option(Batch.Wrapped(_underlying.tail))

  def uncons: Option[(A, Batch[A])] =
    headOption.map(a => (a, tailOrEmpty))

  // def toVector: Vector[A] =
  //   _underlying

  def toList: List[A] =
    _underlying.toList

  def toMap[K, V](using A <:< (K, V)) =
    _underlying.toMap

  def toSet[B >: A]: Set[B] =
    _underlying.toSet

  def toArray[B >: A: ClassTag]: Array[B] =
    _underlying.toArray

  override def toString: String =
    "Batch(" + _underlying.mkString(", ") + ")"

  def update[B >: A](index: Int, value: B): Batch[B] =
    val p = _underlying.splitAt(index)
    Batch.fromVector((p._1 :+ value) ++ p._2.tail)

  def zipWithIndex: Batch[(A, Int)] =
    Batch.Wrapped(_underlying.zipWithIndex)

  def zip[B](other: Batch[B]): Batch[(A, B)] =
    Batch.Wrapped(_underlying.zip(other.toVector))

  override def hashCode(): Int =
    _underlying.foldLeft(31)((acc, v) => 31 * acc + v.hashCode())

object Batch:

  extension [A](s: Seq[A]) def toBatch: Batch[A] = Batch.fromSeq(s)

  given CanEqual[Batch[?], Batch[?]]         = CanEqual.derived
  given CanEqual[Batch[?], Batch.Combine[?]] = CanEqual.derived
  given CanEqual[Batch[?], Batch.Wrapped[?]] = CanEqual.derived

  given CanEqual[Batch.Combine[?], Batch[?]]         = CanEqual.derived
  given CanEqual[Batch.Combine[?], Batch.Combine[?]] = CanEqual.derived
  given CanEqual[Batch.Combine[?], Batch.Wrapped[?]] = CanEqual.derived

  given CanEqual[Batch.Wrapped[?], Batch[?]]         = CanEqual.derived
  given CanEqual[Batch.Wrapped[?], Batch.Combine[?]] = CanEqual.derived
  given CanEqual[Batch.Wrapped[?], Batch.Wrapped[?]] = CanEqual.derived

  /** Creates a Batch from a variable number of elements. */
  def apply[A](values: A*): Batch[A] =
    Wrapped(Vector.from(values))

  def unapplySeq[A](b: Batch[A]): Seq[A] =
    b.toList

  object ==: {
    def unapply[A](b: Batch[A]): Option[(A, Batch[A])] =
      if b.isEmpty then None
      else Some(b.head -> b.tail)
  }

  object :== {
    def unapply[A](b: Batch[A]): Option[(Batch[A], A)] =
      if b.isEmpty then None
      else
        val r = b.reverse
        Some(r.tail.reverse -> r.head)
  }

  /** Creates a Batch with n copies of the given element. */
  def fill[A](n: Int)(elem: => A): Batch[A] =
    Batch.fromList(List.fill[A](n)(elem))

  /** Creates a Batch from a Scala vector. */
  def fromVector[A](values: Vector[A]): Batch[A] =
    Wrapped(values)

  /** Creates a Batch from a Scala array. */
  def fromArray[A](values: Array[A]): Batch[A] =
    Wrapped(values.toVector)

  /** Creates a Batch from a List. */
  def fromList[A](values: List[A]): Batch[A] =
    Wrapped(Vector.from(values))

  /** Creates a Batch from a Set. */
  def fromSet[A](values: Set[A]): Batch[A] =
    Wrapped(values.toVector)

  /** Creates a Batch from any Seq. */
  def fromSeq[A](values: Seq[A]): Batch[A] =
    Wrapped(values.toVector)

  /** Creates a Batch from an IndexedSeq. */
  def fromIndexedSeq[A](values: IndexedSeq[A]): Batch[A] =
    Wrapped(values.toVector)

  /** Creates a Batch from an Iterator. */
  def fromIterator[A](values: Iterator[A]): Batch[A] =
    Wrapped(values.toVector)

  /** Creates a Batch from an Iterable. */
  def fromIterable[A](values: Iterable[A]): Batch[A] =
    Wrapped(values.toVector)

  def fromMap[K, V](values: Map[K, V]): Batch[(K, V)] =
    Wrapped(values.toVector)

  def fromOption[A](value: Option[A]): Batch[A] =
    Wrapped(value.toVector)

  def fromRange(value: Range): Batch[Int] =
    Wrapped(value.toVector)

  /** Creates an empty Batch. */
  def empty[A]: Batch[A] =
    Batch()

  /** Combines two batches by concatenation. */
  def combine[A](batch1: Batch[A], batch2: Batch[A]): Batch[A] =
    batch1 ++ batch2

  /** Combines multiple batches by concatenation. */
  def combineAll[A](batches: Batch[A]*): Batch[A] =
    batches.foldLeft(Batch.empty[A])(_ ++ _)

  private[shared] final case class Combine[A](batch1: Batch[A], batch2: Batch[A]) extends Batch[A]:
    val isEmpty: Boolean = batch1.isEmpty && batch2.isEmpty

    export batch1.head
    export batch1.headOption

    def last: A =
      if batch2.isEmpty then batch1.last else batch2.last

    def lastOption: Option[A] =
      if batch2.isEmpty then batch1.lastOption else batch2.lastOption

    def toVector: Vector[A] =

      @tailrec
      def rec(remaining: List[Batch[A]], acc: Vector[A]): Vector[A] =
        remaining match
          case Nil =>
            acc

          case Batch.Combine(c1, c2) :: xs =>
            rec(c1 :: c2 :: xs, acc)

          case Batch.Wrapped(vs) :: xs =>
            rec(xs, acc ++ vs)

      rec(List(batch1, batch2), Vector.empty[A])

    lazy val size: Int = batch1.size + batch2.size

    override def equals(that: Any): Boolean =
      try
        that match
          case c @ Combine(_, _) =>
            // val vs: Vector[A] = c.compact.toVector
            compact.values.sameElements(c.compact.values)

          case Wrapped(arr) =>
            compact.values.sameElements(arr)

          case _ => false
      catch { case NonFatal(_) => false }

  private[shared] final case class Wrapped[A](values: Vector[A]) extends Batch[A]:
    val isEmpty: Boolean      = values.isEmpty
    def head: A               = values.head
    def headOption: Option[A] = values.headOption
    def last: A               = values.last
    def lastOption: Option[A] = values.lastOption
    def toVector: Vector[A]   = values

    lazy val size: Int = values.length

    override def equals(that: Any): Boolean =
      try
        that match
          case c @ Combine(_, _) =>
            // val vs: Vector[A] = c.compact.toVector
            values.sameElements(c.compact.toVector)

          case Wrapped(arr) =>
            values.sameElements(arr)

          case _ => false
      catch { case NonFatal(_) => false }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def sequenceOption[A](b: Batch[Option[A]]): Option[Batch[A]] =
    @tailrec
    def rec(remaining: Batch[Option[A]], acc: Batch[A]): Option[Batch[A]] =
      if remaining.isEmpty then Option(acc.reverse)
      else
        remaining match
          case None ==: xs =>
            rec(xs, acc)

          case Some(x) ==: xs =>
            rec(xs, x :: acc)

          case _ =>
            throw new Exception("Error encountered sequencing Batch[Option[A]]")

    rec(b, Batch.empty[A])

  def sequenceListOption[A](l: List[Option[A]]): Option[List[A]] =
    @tailrec
    def rec(remaining: List[Option[A]], acc: List[A]): Option[List[A]] =
      remaining match
        case Nil =>
          Some(acc.reverse)

        case None :: as =>
          rec(as, acc)

        case Some(a) :: as =>
          rec(as, a :: acc)

    rec(l, Nil)
