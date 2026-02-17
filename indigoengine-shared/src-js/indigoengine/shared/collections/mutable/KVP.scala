package indigoengine.shared.collections.mutable

import indigoengine.shared.collections.Batch
import indigoengine.shared.collections.KVP as ImmutableKVP
import indigoengine.shared.collections.KVPOps

import scala.annotation.nowarn

import scalajs.js.Dictionary

/** Mutable KVP backed by js.Dictionary. Provides in-place mutation operations for performance-sensitive code paths
  * (caching, registers).
  */
final class KVP[A] private (private val _underlying: Dictionary[A]) extends KVPOps[A, KVP]:
  def get(key: String): Option[A] =
    _underlying.get(key)

  def getUnsafe(key: String): A =
    _underlying(key)

  @nowarn("msg=unused")
  def add(key: String, value: A): KVP[A] =
    _underlying.addOne((key, value))
    this

  @nowarn("msg=unused")
  def add(value: (String, A)): KVP[A] =
    _underlying.addOne(value)
    this

  @nowarn("msg=unused")
  def addAll(values: Batch[(String, A)]): KVP[A] =
    _underlying.addAll(values.toJSArray)
    this

  def keys: Batch[String] =
    Batch.fromIterable(_underlying.keys)

  def size: Int =
    _underlying.size

  def toMap: Map[String, A] =
    _underlying.toMap

  def toBatch: Batch[(String, A)] =
    Batch.fromList(_underlying.toList)

  def update(key: String, value: A): Unit =
    _underlying.update(key, value)

  def clear(): Unit =
    _underlying.clear()

  @nowarn("msg=unused")
  def remove(key: String): Option[A] =
    _underlying.remove(key)

  def toKVP: ImmutableKVP[A] =
    ImmutableKVP.from(Dictionary.empty[A].addAll(_underlying))

  def map[B](f: ((String, A)) => ((String, B))): KVP[B] =
    new KVP(_underlying.map(f))

object KVP:
  def empty[A]: KVP[A] =
    new KVP(Dictionary.empty[A])

  def from[A](kvp: ImmutableKVP[A]): KVP[A] =
    new KVP(Dictionary.empty[A].addAll(kvp.toDictionary))
