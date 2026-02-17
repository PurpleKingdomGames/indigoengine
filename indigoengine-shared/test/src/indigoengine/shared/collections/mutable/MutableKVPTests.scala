package indigoengine.shared.collections.mutable

import indigoengine.shared.collections.{KVP => ImmutableKVP}
import indigoengine.shared.collections.{Batch => ImmutableBatch}

import scala.annotation.nowarn

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
@nowarn("msg=unused")
class MutableKVPTests extends munit.FunSuite {

  // -- Construction --

  test("empty - creates an empty mutable KVP") {
    val kvp = KVP.empty[Int]
    assertEquals(kvp.size, 0)
  }

  test("from - creates mutable KVP from immutable KVP") {
    val immutable = ImmutableKVP.empty[Int].add("a", 1).add("b", 2)
    val kvp       = KVP.from(immutable)
    assertEquals(kvp.get("a"), Some(1))
    assertEquals(kvp.get("b"), Some(2))
    assertEquals(kvp.size, 2)
  }

  // -- Read ops (KVPOps contract) --

  test("get - returns Some for existing key") {
    val kvp = KVP.empty[String]
    kvp.add("greeting", "hello")
    assertEquals(kvp.get("greeting"), Some("hello"))
  }

  test("get - returns None for missing key") {
    val kvp = KVP.empty[String]
    assertEquals(kvp.get("missing"), None)
  }

  test("getUnsafe - returns value for existing key") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 10)
    assertEquals(kvp.getUnsafe("a"), 10)
  }

  test("getUnsafe - throws for missing key") {
    val kvp = KVP.empty[Int]
    intercept[java.util.NoSuchElementException] {
      kvp.getUnsafe("missing")
    }
  }

  test("keys - returns Batch of all keys") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.add("b", 2)
    kvp.add("c", 3)
    val keysList = kvp.keys.toList.sorted
    assertEquals(keysList, List("a", "b", "c"))
  }

  test("size - reflects number of entries") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.add("b", 2)
    assertEquals(kvp.size, 2)
  }

  test("toMap - converts to Map correctly") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.add("b", 2)
    assertEquals(kvp.toMap, Map("a" -> 1, "b" -> 2))
  }

  test("toBatch - converts to Batch of tuples") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.add("b", 2)
    val tuples = kvp.toBatch.toList.sortBy(_._1)
    assertEquals(tuples, List(("a", 1), ("b", 2)))
  }

  // -- Mutation ops --

  test("add(key, value) - mutates in place and returns this") {
    val kvp    = KVP.empty[Int]
    val result = kvp.add("a", 1)
    assert(result eq kvp)
    assertEquals(kvp.get("a"), Some(1))
  }

  test("add(tuple) - mutates in place and returns this") {
    val kvp    = KVP.empty[Int]
    val result = kvp.add("x" -> 42)
    assert(result eq kvp)
    assertEquals(kvp.get("x"), Some(42))
  }

  test("addAll - mutates in place and returns this") {
    val kvp    = KVP.empty[Int]
    val result = kvp.addAll(ImmutableBatch(("a", 1), ("b", 2)))
    assert(result eq kvp)
    assertEquals(kvp.size, 2)
    assertEquals(kvp.get("a"), Some(1))
    assertEquals(kvp.get("b"), Some(2))
  }

  test("update - overwrites existing value") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.update("a", 99)
    assertEquals(kvp.get("a"), Some(99))
    assertEquals(kvp.size, 1)
  }

  test("update - adds if key doesn't exist") {
    val kvp = KVP.empty[Int]
    kvp.update("new", 42)
    assertEquals(kvp.get("new"), Some(42))
  }

  test("clear - removes all entries") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.add("b", 2)
    kvp.clear()
    assertEquals(kvp.size, 0)
    assertEquals(kvp.get("a"), None)
  }

  test("remove - removes entry and returns its value") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.add("b", 2)
    val removed = kvp.remove("a")
    assertEquals(removed, Some(1))
    assertEquals(kvp.size, 1)
    assertEquals(kvp.get("a"), None)
  }

  test("remove - returns None for missing key") {
    val kvp = KVP.empty[Int]
    assertEquals(kvp.remove("nope"), None)
  }

  // -- Mutation verification --

  test("mutation modifies the same instance") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.add("b", 2)
    kvp.remove("a")
    assertEquals(kvp.size, 1)
    assertEquals(kvp.get("a"), None)
    assertEquals(kvp.get("b"), Some(2))
  }

  // -- Conversion --

  test("toKVP - produces correct immutable KVP") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.add("b", 2)
    val immutable = kvp.toKVP
    assertEquals(immutable.toMap, Map("a" -> 1, "b" -> 2))
  }

  test("toKVP - is a defensive copy") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    val immutable = kvp.toKVP
    kvp.add("b", 2)
    // immutable should not be affected
    assertEquals(immutable.get("b"), None)
    assertEquals(immutable.size, 1)
  }

  // -- map --

  test("map - transforms values into new mutable KVP") {
    val kvp = KVP.empty[Int]
    kvp.add("a", 1)
    kvp.add("b", 2)
    val mapped = kvp.map { case (k, v) => (k, v * 10) }
    assertEquals(mapped.toMap, Map("a" -> 10, "b" -> 20))
    // original unchanged
    assertEquals(kvp.toMap, Map("a" -> 1, "b" -> 2))
  }

}
