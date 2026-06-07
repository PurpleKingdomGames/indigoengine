package tyrian

import tyrian.syntax.*

import Result.*

final case class TestMsg(message: String) extends GlobalMsg

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class ResultTests extends munit.FunSuite {

  test("Transforming outcomes.sequencing (list) (using syntax import)") {
    val l: List[Result[Int]] =
      List(
        Result(1).addActions(Action.Emit(TestMsg("a"))),
        Result(2).addActions(Action.Emit(TestMsg("b"))),
        Result(3).addActions(Action.Emit(TestMsg("c")))
      )

    val actual: Result[List[Int]] =
      l.sequence

    val expected: Result[List[Int]] =
      Result(List(1, 2, 3))
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Transforming outcomes.sequencing (batch) (using syntax import)") {
    val l: Batch[Result[Int]] =
      Batch(
        Result(1).addActions(Action.Emit(TestMsg("a"))),
        Result(2).addActions(Action.Emit(TestMsg("b"))),
        Result(3).addActions(Action.Emit(TestMsg("c")))
      )

    val actual: Result[Batch[Int]] =
      l.sequence

    val expected: Result[Batch[Int]] =
      Result(Batch(1, 2, 3))
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Convert Option[A] to Result[A] (using syntax import)") {

    val e = new Exception("Boom!")

    val actual =
      Option(123).toResult(e)

    val expected =
      Result[Int](123)

    assertEquals(actual, expected)
  }

  // Error handling

  def errorsMatch[A](actual: Result[A], expected: Result[A]): Boolean =
    (actual, expected) match {
      case (Result.Error(e1, _), Result.Error(e2, _)) =>
        e1.getMessage == e2.getMessage

      case _ =>
        false
    }

  test("Convert Option[A] to Result[A] (error case) (using syntax import)") {

    val e = new Exception("Boom!")

    val actual =
      Option.empty[Int].toResult(e)

    val expected =
      Result.Error(e)

    assert(errorsMatch(actual, expected))
  }

}
