package indigoengine.shared.collections

import scalajs.js.Dictionary

sealed trait KVP[A]:
  private val _underlying: Dictionary[A] = toDictionary

  def toDictionary: Dictionary[A]

  def clear(): Unit =
    _underlying.clear()

  def get(key: String): Option[A] =
    _underlying.get(key)

  def getUnsafe(key: String): A =
    _underlying(key)

  def remove(key: String): Option[A] =
    _underlying.remove(key)

  def update(key: String, value: A): Unit =
    _underlying.update(key, value)

  def add(key: String, value: A): KVP[A] =
    add(key -> value)

  def add(value: (String, A)): KVP[A] =
    KVP.from(_underlying.addOne(value))

  def addAll(values: Batch[(String, A)]): KVP[A] =
    KVP.from(_underlying.addAll(values.toVector))

  def keys: Batch[String] =
    Batch.fromIterable(_underlying.keys)

  def size: Int =
    _underlying.size

  def toMap: Map[String, A] =
    _underlying.toMap

  def toBatch: Batch[(String, A)] =
    Batch.fromList(_underlying.toList)

  def map[B](f: ((String, A)) => ((String, B))): KVP[B] =
    KVP.from(_underlying.map(f))

object KVP:

  final private class KVPImpl[A](dict: Dictionary[A]) extends KVP[A]:
    def toDictionary: Dictionary[A] = dict

  def from[A](dict: Dictionary[A]): KVP[A] =
    KVPImpl(dict)

  def empty[A]: KVP[A] =
    KVPImpl(Dictionary.empty[A])
