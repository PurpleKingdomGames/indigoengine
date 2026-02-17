package indigoengine.shared.collections

/** Common read-only operations shared between KVP and mutable.KVP. This trait exists to keep the APIs in sync — it is
  * NOT intended for polymorphic use (the types are effectively separate).
  */
trait KVPOps[A, KVPOut[_]]:
  def get(key: String): Option[A]
  def getUnsafe(key: String): A
  def add(key: String, value: A): KVPOut[A]
  def add(value: (String, A)): KVPOut[A]
  def addAll(values: Batch[(String, A)]): KVPOut[A]
  def keys: Batch[String]
  def size: Int
  def toMap: Map[String, A]
  def toBatch: Batch[(String, A)]
  def map[B](f: ((String, A)) => ((String, B))): KVPOut[B]
