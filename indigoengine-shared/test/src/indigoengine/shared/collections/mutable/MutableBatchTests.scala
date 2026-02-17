package indigoengine.shared.collections.mutable

import indigoengine.shared.collections.{Batch => ImmutableBatch}
import scala.annotation.nowarn

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
@nowarn("msg=unused")
class MutableBatchTests extends munit.FunSuite {

  // -- Construction --

  test("empty - creates an empty mutable Batch") {
    val b = Batch.empty[Int]
    assert(b.isEmpty)
    assertEquals(b.size, 0)
  }

  test("apply - creates mutable Batch with values") {
    val b = Batch(1, 2, 3)
    assertEquals(b.size, 3)
    assertEquals(b(0), 1)
    assertEquals(b(1), 2)
    assertEquals(b(2), 3)
  }

  test("from - creates mutable Batch from immutable Batch") {
    val immutable = ImmutableBatch(10, 20, 30)
    val b         = Batch.from(immutable)
    assertEquals(b.toList, List(10, 20, 30))
  }

  // -- Read ops (BatchOps contract) --

  test("head - returns first element") {
    assertEquals(Batch(1, 2, 3).head, 1)
  }

  test("head - throws on empty") {
    intercept[NoSuchElementException] {
      Batch.empty[Int].head
    }
  }

  test("headOption - Some for non-empty") {
    assertEquals(Batch(1, 2, 3).headOption, Some(1))
  }

  test("headOption - None for empty") {
    assertEquals(Batch.empty[Int].headOption, None)
  }

  test("last - returns last element") {
    assertEquals(Batch(1, 2, 3).last, 3)
  }

  test("last - throws on empty") {
    intercept[NoSuchElementException] {
      Batch.empty[Int].last
    }
  }

  test("lastOption - Some for non-empty") {
    assertEquals(Batch(1, 2, 3).lastOption, Some(3))
  }

  test("lastOption - None for empty") {
    assertEquals(Batch.empty[Int].lastOption, None)
  }

  test("isEmpty / nonEmpty") {
    assert(Batch.empty[Int].isEmpty)
    assert(!Batch.empty[Int].nonEmpty)
    assert(!Batch(1).isEmpty)
    assert(Batch(1).nonEmpty)
  }

  test("size / length") {
    val b = Batch(1, 2, 3)
    assertEquals(b.size, 3)
    assertEquals(b.length, 3)
  }

  test("apply(index)") {
    val b = Batch(10, 20, 30)
    assertEquals(b(0), 10)
    assertEquals(b(1), 20)
    assertEquals(b(2), 30)
  }

  test("contains") {
    val b = Batch(1, 2, 3)
    assert(b.contains(2))
    assert(!b.contains(5))
  }

  test("exists") {
    val b = Batch(1, 2, 3)
    assert(b.exists(_ > 2))
    assert(!b.exists(_ > 5))
  }

  test("find") {
    val b = Batch(1, 2, 3)
    assertEquals(b.find(_ > 1), Some(2))
    assertEquals(b.find(_ > 5), None)
  }

  test("forall") {
    val b = Batch(2, 4, 6)
    assert(b.forall(_ % 2 == 0))
    assert(!b.forall(_ > 3))
  }

  test("foreach") {
    var sum = 0
    Batch(1, 2, 3).foreach(sum += _)
    assertEquals(sum, 6)
  }

  test("fold") {
    val result = Batch(1, 2, 3).fold(0)(_ + _)
    assertEquals(result, 6)
  }

  test("foldLeft") {
    val result = Batch(1, 2, 3).foldLeft("")((acc, i) => acc + i.toString)
    assertEquals(result, "123")
  }

  test("mkString") {
    val b = Batch(1, 2, 3)
    assertEquals(b.mkString, "123")
    assertEquals(b.mkString(", "), "1, 2, 3")
  }

  test("toList") {
    assertEquals(Batch(1, 2, 3).toList, List(1, 2, 3))
    assertEquals(Batch.empty[Int].toList, Nil)
  }

  test("toVector") {
    assertEquals(Batch(1, 2, 3).toVector, Vector(1, 2, 3))
  }

  // -- Mutation ops --

  test("+= appends a value") {
    val b = Batch(1, 2)
    b += 3
    assertEquals(b.toList, List(1, 2, 3))
  }

  test("+=: prepends a value") {
    val b = Batch(2, 3)
    0 +=: b
    assertEquals(b.toList, List(0, 2, 3))
  }

  test("append") {
    val b = Batch(1, 2)
    b.append(3)
    assertEquals(b.toList, List(1, 2, 3))
  }

  test("prepend") {
    val b = Batch(2, 3)
    b.prepend(1)
    assertEquals(b.toList, List(1, 2, 3))
  }

  test("update(index, value)") {
    val b = Batch(1, 2, 3)
    b.update(1, 20)
    assertEquals(b.toList, List(1, 20, 3))
  }

  test("clear") {
    val b = Batch(1, 2, 3)
    b.clear()
    assert(b.isEmpty)
    assertEquals(b.size, 0)
  }

  // -- Mutation verification: same instance is modified --

  test("mutation modifies the same instance") {
    val b = Batch(1, 2, 3)
    b += 4
    assertEquals(b.size, 4)
    assertEquals(b(3), 4)
  }

  // -- Derived collections --

  test("map - returns new mutable Batch") {
    val b      = Batch(1, 2, 3)
    val mapped = b.map(_ * 10)
    assertEquals(mapped.toList, List(10, 20, 30))
    // original unchanged
    assertEquals(b.toList, List(1, 2, 3))
  }

  test("filter - returns new mutable Batch") {
    val b        = Batch(1, 2, 3, 4, 5)
    val filtered = b.filter(_ % 2 == 0)
    assertEquals(filtered.toList, List(2, 4))
    // original unchanged
    assertEquals(b.size, 5)
  }

  test("flatMap - returns new mutable Batch") {
    val b       = Batch(1, 2, 3)
    val flatted = b.flatMap(i => Batch(i, i * 10))
    assertEquals(flatted.toList, List(1, 10, 2, 20, 3, 30))
  }

  // -- Conversion --

  test("toBatch - produces correct immutable Batch") {
    val b         = Batch(1, 2, 3)
    val immutable = b.toBatch
    assertEquals(immutable.toList, List(1, 2, 3))
  }

  test("toBatch - is a defensive copy") {
    val b         = Batch(1, 2, 3)
    val immutable = b.toBatch
    b += 4
    // immutable should not be affected
    assertEquals(immutable.toList, List(1, 2, 3))
  }

  // -- toString --

  test("toString format") {
    val b = Batch(1, 2, 3)
    assertEquals(b.toString, "mutable.Batch(1, 2, 3)")
  }

  test("toString - empty") {
    assertEquals(Batch.empty[Int].toString, "mutable.Batch()")
  }

}
