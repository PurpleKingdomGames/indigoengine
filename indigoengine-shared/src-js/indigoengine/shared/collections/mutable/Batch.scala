package indigoengine.shared.collections.mutable

import indigoengine.shared.collections.Batch as ImmutableBatch
import indigoengine.shared.collections.BatchOps

import scala.annotation.nowarn

import scalajs.js

/** Mutable Batch backed by js.Array. Provides in-place mutation operations for performance-sensitive code paths
  * (rendering, caching).
  */
final class Batch[A] private (private val _underlying: js.Array[A]) extends BatchOps[A]:
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
    _underlying.length

  def length: Int =
    _underlying.length

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

  @nowarn("msg=unused")
  def +=(value: A): Unit =
    _underlying.push(value)
    ()

  @nowarn("msg=unused")
  def +=:(value: A): Unit =
    _underlying.unshift(value)
    ()

  @nowarn("msg=unused")
  def append(value: A): Unit =
    _underlying.push(value)
    ()

  @nowarn("msg=unused")
  def prepend(value: A): Unit =
    _underlying.unshift(value)
    ()

  def update(index: Int, value: A): Unit =
    _underlying(index) = value

  def clear(): Unit =
    _underlying.length = 0

  def map[B](f: A => B): Batch[B] =
    new Batch(_underlying.map(f))

  def filter(p: A => Boolean): Batch[A] =
    new Batch(_underlying.filter(p))

  def flatMap[B](f: A => Batch[B]): Batch[B] =
    new Batch(_underlying.flatMap(v => f(v)._underlying))

  def toBatch: ImmutableBatch[A] =
    ImmutableBatch.fromJSArray(_underlying.slice(0, _underlying.length))
  def toJSArray: js.Array[A] =
    _underlying

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
    new Batch(js.Array(values*))

  def empty[A]: Batch[A] =
    new Batch(new js.Array[A]())

  def from[A](batch: ImmutableBatch[A]): Batch[A] =
    new Batch(batch.toJSArray)
