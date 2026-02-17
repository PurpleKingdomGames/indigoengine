package indigoengine.shared.collections

class KVPTests extends munit.FunSuite {

  test("empty - has size 0") {
    val kvp = KVP.empty[Int]
    assertEquals(kvp.size, 0)
  }

  test("empty - get returns None") {
    val kvp = KVP.empty[Int]
    assertEquals(kvp.get("a"), None)
  }

  test("add(key, value) - returns new KVP with entry") {
    val kvp = KVP.empty[Int].add("a", 1)
    assertEquals(kvp.get("a"), Some(1))
    assertEquals(kvp.size, 1)
  }

  test("add(key, value) - multiple entries") {
    val kvp = KVP.empty[Int].add("a", 1).add("b", 2).add("c", 3)
    assertEquals(kvp.get("a"), Some(1))
    assertEquals(kvp.get("b"), Some(2))
    assertEquals(kvp.get("c"), Some(3))
    assertEquals(kvp.size, 3)
  }

  test("add(tuple) - tuple syntax") {
    val kvp = KVP.empty[Int].add("x" -> 42)
    assertEquals(kvp.get("x"), Some(42))
    assertEquals(kvp.size, 1)
  }

  test("addAll - adds multiple entries from a Batch") {
    val entries = Batch(("a", 1), ("b", 2), ("c", 3))
    val kvp     = KVP.empty[Int].addAll(entries)
    assertEquals(kvp.get("a"), Some(1))
    assertEquals(kvp.get("b"), Some(2))
    assertEquals(kvp.get("c"), Some(3))
    assertEquals(kvp.size, 3)
  }

  test("get - returns Some for existing key") {
    val kvp = KVP.empty[String].add("greeting", "hello")
    assertEquals(kvp.get("greeting"), Some("hello"))
  }

  test("get - returns None for missing key") {
    val kvp = KVP.empty[String].add("greeting", "hello")
    assertEquals(kvp.get("farewell"), None)
  }

  test("getUnsafe - returns value for existing key") {
    val kvp = KVP.empty[Int].add("a", 10)
    assertEquals(kvp.getUnsafe("a"), 10)
  }

  test("getUnsafe - throws for missing key") {
    val kvp = KVP.empty[Int]
    intercept[java.util.NoSuchElementException] {
      kvp.getUnsafe("missing")
    }
  }

  test("keys - returns Batch of all keys") {
    val kvp      = KVP.empty[Int].add("a", 1).add("b", 2).add("c", 3)
    val keysList = kvp.keys.toList.sorted
    assertEquals(keysList, List("a", "b", "c"))
  }

  test("size - reflects number of entries") {
    val kvp = KVP.empty[Int].add("a", 1).add("b", 2)
    assertEquals(kvp.size, 2)
  }

  test("toMap - converts to Map correctly") {
    val kvp = KVP.empty[Int].add("a", 1).add("b", 2)
    val m   = kvp.toMap
    assertEquals(m, Map("a" -> 1, "b" -> 2))
  }

  test("toBatch - converts to Batch of tuples") {
    val kvp    = KVP.empty[Int].add("a", 1).add("b", 2)
    val tuples = kvp.toBatch.toList.sortBy(_._1)
    assertEquals(tuples, List(("a", 1), ("b", 2)))
  }

  test("map - transforms values preserving keys") {
    val kvp    = KVP.empty[Int].add("a", 1).add("b", 2)
    val mapped = kvp.map { case (k, v) => (k, v * 10) }
    assertEquals(mapped.toMap, Map("a" -> 10, "b" -> 20))
  }

}
