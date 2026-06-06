package tyrian

import cats.effect.IO
import indigoengine.shared.datatypes.*
import tyrian.syntax.*

import scala.scalajs.js

@SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.var"))
class WatcherJsTests extends munit.CatsEffectSuite {

  type Obs[A] = IO[(Either[Throwable, A] => Unit) => IO[Option[IO[Unit]]]]

  final case class IntMsg(i: Int) extends GlobalMsg

  import ActionWatcherUtils.*

  test("map - Empty") {
    assertEquals(Watcher.None.map(_ => IntMsg(10)), Watcher.None)
  }

  test("Run") {
    var state = 0

    val callback: Either[Throwable, Int] => Unit = {
      case Right(i) => state = i; ()
      case Left(_)  => throw new Exception("failed")
    }

    val observable: Obs[Int] = IO.delay { cb =>
      cb(Right(10))
      IO(Option(IO(())))
    }

    val runnable =
      Watcher.Observe("test", observable, i => Option(IntMsg(i)))

    runnable.run(callback).map(_ => state == 10).assert
  }

  test("fromDate - preserves the js.Date value") {
    val watcher = Watcher.fromDate(100.millis, "test", date => IntMsg(date.getTime().toInt))

    watcher match
      case Watcher.Observe(_, _, toMsg: (js.Date => Option[GlobalMsg]) @unchecked) =>
        assertEquals(toMsg(new js.Date(123.0)), Option(IntMsg(123)))

      case _ =>
        fail("unexpected watcher type")
  }

  test("every - still maps the current date to Millis") {
    val watcher = Watcher.every(100.millis, "test", millis => IntMsg(millis.toLong.toInt))

    watcher match
      case Watcher.Observe(_, _, toMsg: (js.Date => Option[GlobalMsg]) @unchecked) =>
        assertEquals(toMsg(new js.Date(123.0)), Option(IntMsg(123)))

      case _ =>
        fail("unexpected watcher type")
  }

}
