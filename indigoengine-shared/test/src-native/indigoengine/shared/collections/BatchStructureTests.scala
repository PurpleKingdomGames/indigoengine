package indigoengine.shared.collections

import scala.annotation.nowarn

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
@nowarn("msg=unused")
class BatchStructureTests extends munit.FunSuite {

  test("compact") {
    val actual =
      Batch.Combine(
        Batch(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    val expected =
      Batch.Wrapped(Vector(1, 2, 3, 4, 5, 6))

    assertEquals(actual.compact.toList, expected.toList)
  }

  test("equals") {
    assert(Batch(1) != Batch.empty)
    assert(Batch(1) == Batch.Wrapped(Vector(1)))
    assert(Batch(2) != Batch.Wrapped(Vector(1, 2)))
    assert(Batch.Wrapped(Vector(1, 2)) != Batch.Wrapped(Vector(2, 1)))
    assert(Batch.empty == Batch.empty)
    assert(Batch.Combine(Batch.empty, Batch.empty) == Batch.empty)
    assert(Batch.Combine(Batch(1), Batch.empty) == Batch(1))

    val a: Batch[Int] =
      Batch.Combine(
        Batch(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    val b: Batch[Int] =
      Batch(1, 2, 3, 4, 5, 6)

    assert(a == b)
  }

  test("isEmpty") {
    assert(Batch.empty.isEmpty)
    assert(!Batch(1).isEmpty)
    assert(!Batch.Combine(Batch(1), Batch(2)).isEmpty)
    assert(!Batch.Wrapped(Vector(1, 2, 3)).isEmpty)
  }

}
