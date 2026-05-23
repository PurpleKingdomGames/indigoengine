package tyrian.extensions

import indigoengine.shared.collections.Batch
import indigoengine.shared.datatypes.Seconds
import indigoengine.shared.typeclass.Monoid
import tyrian.*

import scala.collection.mutable.ArrayBuffer

final case class TestView(parts: List[String]) derives CanEqual
object TestView:
  given Monoid[TestView] =
    Monoid.instance(TestView(Nil), (a, b) => TestView(a.parts ++ b.parts))

final case class TestCtx(label: String) derives CanEqual

final class GraphicalRecorder(
    _id: String,
    contextHook: Int => Option[TestCtx] = _ => None
) extends Extension.Graphical[TestCtx, TestView]:
  type ExtensionModel = Int
  val calls: ArrayBuffer[(TestCtx, Seconds, Int)] = ArrayBuffer.empty
  def id: ExtensionId                             = ExtensionId(_id)
  def init: Result[Int]                           = Result(0)
  def update(m: Int): GlobalMsg => Result[Int]    = _ => Result(m)
  def view(m: Int): TestView                      = TestView(Nil)
  def watchers(m: Int): Batch[Watcher]            = Batch.empty
  def draw(ctx: TestCtx, t: Seconds, m: Int): Int =
    calls += ((ctx, t, m))
    m + 1
  def provideContext(m: Int): Option[TestCtx] = contextHook(m)

class ExtensionRegisterDrawTests extends munit.FunSuite {

  test("Graphical: runtime ctx is used and model is updated") {
    val ext      = new GraphicalRecorder("g")
    val register = new ExtensionRegister[TestCtx, TestView]()
    val _        = register.register(Batch(ext))

    register.draw(Some(TestCtx("runtime")), Seconds(1.0))

    assertEquals(ext.calls.toList, List((TestCtx("runtime"), Seconds(1.0), 0)))
  }

  test("Graphical: ctx None + provideContext Some → extension ctx is used") {
    val ext      = new GraphicalRecorder("g", _ => Some(TestCtx("ext")))
    val register = new ExtensionRegister[TestCtx, TestView]()
    val _        = register.register(Batch(ext))

    register.draw(None, Seconds.zero)

    assertEquals(ext.calls.toList, List((TestCtx("ext"), Seconds.zero, 0)))
  }

  test("Graphical: ctx None + provideContext None → draw is not invoked") {
    val ext      = new GraphicalRecorder("g")
    val register = new ExtensionRegister[TestCtx, TestView]()
    val _        = register.register(Batch(ext))

    register.draw(None, Seconds.zero)

    assertEquals(ext.calls.toList, Nil)
  }

  test("Graphical: runtime ctx wins over provideContext") {
    val ext      = new GraphicalRecorder("g", _ => Some(TestCtx("ext")))
    val register = new ExtensionRegister[TestCtx, TestView]()
    val _        = register.register(Batch(ext))

    register.draw(Some(TestCtx("runtime")), Seconds.zero)

    assertEquals(ext.calls.head._1, TestCtx("runtime"))
  }

  test("Graphical: model is threaded across successive draws") {
    val ext      = new GraphicalRecorder("g")
    val register = new ExtensionRegister[TestCtx, TestView]()
    val _        = register.register(Batch(ext))

    register.draw(Some(TestCtx("c")), Seconds.zero)
    register.draw(Some(TestCtx("c")), Seconds.zero)
    register.draw(Some(TestCtx("c")), Seconds.zero)

    assertEquals(ext.calls.map(_._3).toList, List(0, 1, 2))
  }

}
