package tyrian.classic

import zio.Runtime
import zio.Task
import zio.Unsafe

import scala.annotation.nowarn

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianZIOApp[Msg, Model] extends TyrianApp[Task, Msg, Model]:

  private val runtime = Runtime.default

  @nowarn("msg=discarded")
  val run: Task[Nothing] => Unit = runnable =>
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.fork(runnable)
    }

  def main(args: Array[String]): Unit =
    launch(args)
