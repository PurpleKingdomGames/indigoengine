package indigoengine.shared.typeclass

import indigoengine.shared.collections.Batch

trait Monoid[T]:
  def empty: T
  def combine(a: T, b: T): T

  def combineAll(ts: Batch[T]): T =
    ts.foldLeft(empty)((a, b) => combine(a, b))

object Monoid:

  def instance[T](
      _empty: T,
      _combine: (T, T) => T
  ): Monoid[T] =
    new Monoid[T]:
      def empty: T               = _empty
      def combine(a: T, b: T): T = _combine(a, b)
