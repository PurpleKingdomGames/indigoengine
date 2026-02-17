package indigoengine.shared.collections

import scala.reflect.ClassTag

/** Common read-only operations shared between Batch and mutable.Batch. This trait exists to keep the APIs in sync — it
  * is NOT intended for polymorphic use (the types are effectively separate).
  */
trait BatchOps[+A]:
  // type T[_] // One of the Batch types.

  def head: A
  def headOption: Option[A]
  def last: A
  def lastOption: Option[A]
  def isEmpty: Boolean
  def size: Int
  def length: Int
  def lengthCompare(len: Int): Int
  // def ++[B >: A](other: T[B]): T[B]
  // def |+|[B >: A](other: T[B]): T[B]
  // def ::[B >: A](value: B): T[B]
  // def +:[B >: A](value: B): T[B]
  // def :+[B >: A](value: B): T[B]
  def apply(index: Int): A
  // def collect[B >: A, C](f: PartialFunction[B, C]): T[C]
  def collectFirst[B >: A, C](f: PartialFunction[B, C]): Option[C]
  // def compact[B >: A]: T[B]
  def contains[B >: A](p: B): Boolean
  // def distinct: T[A]
  // def distinctBy[B](f: A => B): T[A]
  // def take(n: Int): T[A]
  // def takeRight(n: Int): T[A]
  // def takeWhile(p: A => Boolean): T[A]
  // def drop(count: Int): T[A]
  // def dropRight(count: Int): T[A]
  // def dropWhile(p: A => Boolean): T[A]
  def exists(p: A => Boolean): Boolean
  def find(p: A => Boolean): Option[A]
  // def filter(p: A => Boolean): T[A]
  // def filterNot(p: A => Boolean): T[A]
  // def flatMap[B](f: A => Batch[B]): T[B]
  // def flatten[B](using asBatch: A => Batch[B]): T[B]
  def forall(p: A => Boolean): Boolean
  def fold[B >: A](z: B)(f: (B, B) => B): B
  def foldLeft[B](z: B)(f: (B, A) => B): B
  def foldRight[B](z: B)(f: (A, B) => B): B
  def foreach(f: A => Unit): Unit
  def foreachWithIndex(f: (A, Int) => Unit): Unit
  // def groupBy[K](f: A => K): Map[K, Batch[A]]
  // def grouped(size: Int): T[Batch[A]]
  // def insert[B >: A](index: Int, value: B): T[B]
  // def replace[B >: A](index: Int, value: B): T[B]
  def lift(index: Int): Option[A]
  // def padTo[B >: A](len: Int, elem: B): T[B]
  // def partition(p: A => Boolean): (Batch[A], Batch[A])
  // def map[B](f: A => B): T[B]
  def maxBy[B](f: A => B)(using ord: Ordering[B]): A
  def maxByOption[B](f: A => B)(using ord: Ordering[B]): Option[A]
  def minBy[B](f: A => B)(using ord: Ordering[B]): A
  def minByOption[B](f: A => B)(using ord: Ordering[B]): Option[A]
  def mkString: String
  def mkString(separator: String): String
  def mkString(prefix: String, separator: String, suffix: String): String
  def nonEmpty: Boolean
  def reduce[B >: A](f: (B, B) => B): B
  // def reverse: T[A]
  // def sortBy[B](f: A => B)(implicit ord: Ordering[B]): T[A]
  // def sorted[B >: A](implicit ord: Ordering[B]): T[A]
  // def sortWith(f: (A, A) => Boolean): T[A]
  // def splitAt(n: Int): (Batch[A], Batch[A])
  def sum[B >: A](implicit num: Numeric[B]): B
  // def tail: T[A]
  // def tailOrEmpty: T[A]
  // def tailOption: Option[Batch[A]]
  // def uncons: Option[(A, Batch[A])]
  def toArray[B >: A: ClassTag]: Array[B]
  def toList: List[A]
  def toMap[K, V](using A <:< (K, V)): Map[K, V]
  def toSet[B >: A]: Set[B]
  // def update[B >: A](index: Int, value: B): T[B]
  // def zipWithIndex: T[(A, Int)]
  // def zip[B](other: T[B]): T[(A, B)]
