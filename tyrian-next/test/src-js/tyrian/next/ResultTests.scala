package tyrian.next

import tyrian.next.syntax.*
import indigoengine.shared.collections.Batch

import Result.*

final case class TestMsg(message: String) extends GlobalMsg

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class ResultTests extends munit.FunSuite {

  test("Adding messages.adding messages after the fact") {
    assertEquals(Result(10).unsafeActions, Batch.empty)
    assertEquals(Result(10).addActions(Action.Emit(TestMsg("a"))).unsafeActions.head, Action.Emit(TestMsg("a")))
  }

  test("Adding messages.creating messages based on new state") {
    val actual = Result(10)
      .addActions(Action.Emit(TestMsg("a")))
      .createActions(i => Batch(Action.Emit(TestMsg(s"count: $i"))))
      .unsafeActions

    val expected = Batch(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("count: 10")))

    assertEquals(actual, expected)
  }

  test("Extractor should allow pattern match") {
    val a = Result(1).addActions(Action.Emit(TestMsg("a")))

    a match {
      case Result(n, Batch(Action.Emit(TestMsg(s)))) =>
        assertEquals(n, 1)
        assertEquals(s, "a")

      case x =>
        fail("shouldn't have got here.")
    }
  }

  test("Transforming outcomes.sequencing (list)") {
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

  test("Transforming outcomes.sequencing (batch)") {
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

  test("Mapping over Results.map state") {
    assertEquals(Result(10).map(_ + 10).unsafeGet, Result(20).unsafeGet)

    assertEquals(
      Result(10).addActions(Action.Emit(TestMsg("a"))).map(_ + 10).unsafeGet,
      Result(20).addActions(Action.Emit(TestMsg("a"))).unsafeGet
    )
    assertEquals(
      Result(10).addActions(Action.Emit(TestMsg("a"))).map(_ + 10).unsafeActions,
      Result(20).addActions(Action.Emit(TestMsg("a"))).unsafeActions
    )
  }

  test("Replace global message list") {
    val actual =
      Result(10)
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))
        .replaceActions(_.filter {
          case Action.Emit(TestMsg(msg)) =>
            msg == "b"

          case _ =>
            fail("Expected Action.Emit, but got something else.")
        })

    val expected =
      Result(10)
        .addActions(Action.Emit(TestMsg("b")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("clear global message list") {
    val actual =
      Result(10)
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))
        .clearActions

    val expected =
      Result(10, Batch.empty)

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Mapping over Results.map global messages") {
    val actual =
      Result(10)
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))
        .mapActions {
          case Action.Emit(TestMsg(msg)) =>
            Action.Emit(TestMsg(msg + msg))

          case _ =>
            fail("Expected Action.Emit, but got something else.")
        }

    val expected =
      Result(10)
        .addActions(Action.Emit(TestMsg("aa")), Action.Emit(TestMsg("bb")), Action.Emit(TestMsg("cc")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Mapping over Results.map all") {
    val actual =
      Result(10)
        .addActions(Action.Emit(TestMsg("a")), Action.Emit(TestMsg("b")), Action.Emit(TestMsg("c")))
        .mapAll(
          _ + 20,
          _.filter {
            case Action.Emit(TestMsg(msg)) =>
              msg == "b"

            case _ =>
              fail("Expected Action.Emit, but got something else.")
          }
        )

    val expected =
      Result(30)
        .addActions(Action.Emit(TestMsg("b")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("flat map & join.join preserves message order") {
    val oa =
      Result(
        Result(
          Result(10).addActions(Action.Emit(TestMsg("z")))
        ).addActions(Action.Emit(TestMsg("x")), Action.Emit(TestMsg("y")))
      ).addActions(Action.Emit(TestMsg("w")))

    val expected =
      Result(10)
        .addActions(
          Action.Emit(TestMsg("w")),
          Action.Emit(TestMsg("x")),
          Action.Emit(TestMsg("y")),
          Action.Emit(TestMsg("z"))
        )

    val actual = Result.join(Result.join(oa))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("flat map & join.flatMap") {
    assertEquals(Result(10).flatMap(i => Result(i * 10)).unsafeGet, Result(100).unsafeGet)
    assertEquals(Result(10).flatMap(i => Result(i * 10)).unsafeActions, Result(100).unsafeActions)

    assertEquals(Result.join(Result(10).map(i => Result(i * 10))).unsafeGet, Result(100).unsafeGet)
    assertEquals(
      Result.join(Result(10).map(i => Result(i * 10))).unsafeActions,
      Result(100).unsafeActions
    )
  }

  test("Applicative.ap") {

    val actual: Result[Int] =
      Result(10).ap(Result((i: Int) => i + 10))

    val expected: Result[Int] =
      Result(20)

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Applicative.ap with message") {

    val actual: Result[Int] =
      Result(10).addActions(Action.Emit(TestMsg("x"))).ap(Result((i: Int) => i + 10))

    val expected: Result[Int] =
      Result(20).addActions(Action.Emit(TestMsg("x")))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeActions, expected.unsafeActions)
  }

  test("Combine - 2 Results can be combined") {

    val oa = Result("count").addActions(Action.Emit(TestMsg("x")))
    val ob = Result(1).addActions(Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))

    val actual1 = oa.combine(ob)
    val actual2 = Result.combine(oa, ob)
    val actual3 = (oa, ob).combine

    val expected =
      Result(("count", 1)).addActions(Action.Emit(TestMsg("x")), Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeActions, expected.unsafeActions)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeActions, expected.unsafeActions)
    assertEquals(actual3.unsafeGet, expected.unsafeGet)
    assertEquals(actual3.unsafeActions, expected.unsafeActions)
  }

  test("Combine - 3 Results can be combined") {

    val oa = Result("count").addActions(Action.Emit(TestMsg("x")))
    val ob = Result(1).addActions(Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))
    val oc = Result(true).addActions(Action.Emit(TestMsg("w")))

    val actual1 = Result.combine3(oa, ob, oc)
    val actual2 = (oa, ob, oc).combine

    val expected =
      Result(("count", 1, true)).addActions(
        Action.Emit(TestMsg("x")),
        Action.Emit(TestMsg("y")),
        Action.Emit(TestMsg("z")),
        Action.Emit(TestMsg("w"))
      )

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeActions, expected.unsafeActions)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeActions, expected.unsafeActions)
  }

  test("Applicative.map2 / merge") {
    val oa = Result[String]("count").addActions(Action.Emit(TestMsg("x")))
    val ob = Result[Int](1).addActions(Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))

    val actual1: Result[String] =
      Result.merge(oa, ob)((a: String, b: Int) => a + ": " + b)
    val actual2: Result[String] =
      oa.merge(ob)((a: String, b: Int) => a + ": " + b)
    val actual3: Result[String] =
      (oa, ob).merge((a: String, b: Int) => a + ": " + b)

    val expected: Result[String] =
      Result("count: 1").addActions(Action.Emit(TestMsg("x")), Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeActions, expected.unsafeActions)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeActions, expected.unsafeActions)
    assertEquals(actual3.unsafeGet, expected.unsafeGet)
    assertEquals(actual3.unsafeActions, expected.unsafeActions)
  }

  test("Applicative.map3 / merge") {
    val oa = Result[String]("count").addActions(Action.Emit(TestMsg("x")))
    val ob = Result[Int](1).addActions(Action.Emit(TestMsg("y")), Action.Emit(TestMsg("z")))
    val oc = Result[Boolean](true).addActions(Action.Emit(TestMsg("w")))

    val actual1: Result[String] =
      Result.merge3(oa, ob, oc)((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)
    val actual2: Result[String] =
      (oa, ob, oc).merge((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)

    val expected: Result[String] =
      Result("count: 1: true").addActions(
        Action.Emit(TestMsg("x")),
        Action.Emit(TestMsg("y")),
        Action.Emit(TestMsg("z")),
        Action.Emit(TestMsg("w"))
      )

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeActions, expected.unsafeActions)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeActions, expected.unsafeActions)
  }

  // Error handline

  def errorsMatch[A](actual: Result[A], expected: Result[A]): Boolean =
    (actual, expected) match {
      case (Result.Error(e1, _), Result.Error(e2, _)) =>
        e1.getMessage == e2.getMessage

      case _ =>
        false
    }

  test("Exceptions thrown during creation are handled") {
    val e = new Exception("Boom!")

    val actual =
      Result[Int](throw e)

    val expected =
      Result.Error(e)

    assert(errorsMatch(actual, expected))
  }

  test("mapping an error") {
    val e = new Exception("Boom!")
    val actual =
      Result[Int](10).map[Int](_ => throw e).map(i => i * i)

    val expected =
      Result.Error(e)

    assert(errorsMatch(actual, expected))
  }

  test("flatMapping an error") {
    def foo(): Int =
      throw new Exception("amount: 10")

    val actual =
      for {
        a <- Result[Int](10)
        b <- Result[Int](foo())
        c <- Result[Int](30)
      } yield a + b + c

    val expected =
      Result.Error(new Exception("amount: 10"))

    assertEquals(actual.isError, expected.isError)
    assert(errorsMatch(actual, expected))
  }

  test("raising an error") {
    val e = new Exception("Boom!")

    def foo(o: Result[Int]): Result[Int] =
      o.flatMap { i =>
        if i % 2 == 0 then Result(i * 10)
        else Result.raiseError(e)
      }

    assertEquals(foo(Result(4)), Result(40))
    assert(errorsMatch(foo(Result(5)), Result(throw e)))
  }

  test("recovering from an error") {
    val e = new Exception("Boom!")
    val actual =
      Result(10)
        .map[Int](_ => throw e)
        .map(i => i * i)
        .handleError { case e =>
          Result(e.getMessage.length)
        }

    val expected =
      Result(5)

    assertEquals(actual, expected)
  }

  test("recovering from an error with orElse") {
    val e = new Exception("Boom!")
    val actual =
      Result(10)
        .map[Int](_ => throw e)
        .map(i => i * i)
        .orElse(Result(e.getMessage.length))

    val expected =
      Result(5)

    assertEquals(actual, expected)
  }

  test("logging a crash") {
    val e = new Exception("Boom!")

    val actual =
      try
        Result(10)
          .map[Int](_ => throw e)
          .map(i => i * i)
          .logCrash { case e => e.getMessage }
      catch {
        case _: Throwable =>
          ()
      }

    val expected =
      "Boom!"

    actual match {
      case Error(e, r) =>
        assertEquals(r(e), expected)

      case _ =>
        fail("Failed...")
    }

  }

  test("Convert Option[A] to Result[A]") {

    val e = new Exception("Boom!")

    val actual =
      Option(123).toResult(e)

    val expected =
      Result[Int](123)

    assertEquals(actual, expected)
  }

  test("Convert Option[A] to Result[A] (error case)") {

    val e = new Exception("Boom!")

    val actual =
      Option.empty[Int].toResult(e)

    val expected =
      Result.Error(e)

    assert(errorsMatch(actual, expected))
  }

}
