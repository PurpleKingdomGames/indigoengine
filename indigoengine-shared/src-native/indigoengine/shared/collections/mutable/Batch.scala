package indigoengine.shared.collections.mutable

import indigoengine.shared.collections.Batch as ImmutableBatch
import indigoengine.shared.collections.BatchOps

import scala.collection.mutable.ArrayBuffer

/** Mutable Batch backed by ArrayBuffer. Provides in-place mutation operations for performance-sensitive code paths
  * (rendering, caching).
  */
final class Batch[A] private (private val _underlying: ArrayBuffer[A]) extends BatchOps[A]:
  def head: A =
    _underlying.head

  def headOption: Option[A] =
    _underlying.headOption

  def last: A =
    _underlying.last

  def lastOption: Option[A] =
    _underlying.lastOption

  def isEmpty: Boolean =
    _underlying.isEmpty

  def nonEmpty: Boolean =
    _underlying.nonEmpty

  def size: Int =
    _underlying.size

  def length: Int =
    _underlying.size

  def lengthCompare(len: Int): Int =
    _underlying.lengthCompare(len)

  def apply(index: Int): A =
    _underlying(index)

  def contains[B >: A](p: B): Boolean =
    given CanEqual[B, B] = CanEqual.derived
    _underlying.exists(_ == p)

  def exists(p: A => Boolean): Boolean =
    _underlying.exists(p)

  def find(p: A => Boolean): Option[A] =
    _underlying.find(p)

  def forall(p: A => Boolean): Boolean =
    _underlying.forall(p)

  def foreach(f: A => Unit): Unit =
    _underlying.foreach(f)

  def fold[B >: A](z: B)(f: (B, B) => B): B =
    _underlying.fold(z)(f)

  def foldLeft[B](z: B)(f: (B, A) => B): B =
    _underlying.foldLeft(z)(f)

  def mkString: String =
    _underlying.mkString

  def mkString(separator: String): String =
    _underlying.mkString(separator)

  def toList: List[A] =
    _underlying.toList

  def toVector: Vector[A] =
    _underlying.toVector

  def +=(value: A): Unit =
    _underlying += value
    ()

  def +=:(value: A): Unit =
    value +=: _underlying
    ()

  def append(value: A): Unit =
    _underlying += value
    ()

  def prepend(value: A): Unit =
    value +=: _underlying
    ()

  def update(index: Int, value: A): Unit =
    _underlying(index) = value

  def clear(): Unit =
    _underlying.clear()

  def map[B](f: A => B): Batch[B] =
    new Batch(_underlying.map(f))

  def filter(p: A => Boolean): Batch[A] =
    new Batch(_underlying.filter(p))

  def flatMap[B](f: A => Batch[B]): Batch[B] =
    new Batch(_underlying.flatMap(v => f(v)._underlying))

  def toBatch: ImmutableBatch[A] =
    ImmutableBatch.fromVector(_underlying.toVector)

  override def toString: String =
    "mutable.Batch(" + _underlying.mkString(", ") + ")"

  def collectFirst[B >: A, C](f: PartialFunction[B, C]): Option[C] =
    _underlying.collectFirst(f)

  def foldRight[B](z: B)(f: (A, B) => B): B =
    _underlying.foldRight(z)(f)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def foreachWithIndex(f: (A, Int) => Unit): Unit =
    var idx: Int = 0
    foreach { v =>
      f(v, idx)
      idx = idx + 1
    }

  def lift(index: Int): Option[A] =
    _underlying.lift(index)

  def maxBy[B](f: A => B)(using ord: Ordering[B]): A =
    _underlying.maxBy(f)

  def maxByOption[B](f: A => B)(using ord: Ordering[B]): Option[A] =
    Option.when(_underlying.nonEmpty)(_underlying.maxBy(f))

  def minBy[B](f: A => B)(using ord: Ordering[B]): A =
    _underlying.minBy(f)

  def minByOption[B](f: A => B)(using ord: Ordering[B]): Option[A] =
    Option.when(_underlying.nonEmpty)(_underlying.minBy(f))

  def mkString(prefix: String, separator: String, suffix: String): String =
    _underlying.mkString(prefix, separator, suffix)

  def reduce[B >: A](f: (B, B) => B): B =
    _underlying.reduce(f)

  def sum[B >: A](implicit num: Numeric[B]): B =
    _underlying.sum

  def toArray[B >: A](using ev: scala.reflect.ClassTag[B]): Array[B] =
    _underlying.toArray

  def toMap[K, V](using x$1: A <:< (K, V)): Map[K, V] =
    _underlying.toMap

  def toSet[B >: A]: Set[B] =
    _underlying.toSet

object Batch:
  def apply[A](values: A*): Batch[A] =
    new Batch(ArrayBuffer.from(values))

  def empty[A]: Batch[A] =
    new Batch(ArrayBuffer.empty[A])

  def from[A](batch: ImmutableBatch[A]): Batch[A] =
    new Batch(ArrayBuffer.from(batch.toVector))
