package tyrian.classic

import cats.effect.IO
import cats.effect.unsafe.implicits.global

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianIOApp[Msg, Model] extends TyrianApp[IO, Msg, Model]:

  val run: IO[Nothing] => Unit =
    _.unsafeRunSync()

  def main(args: Array[String]): Unit =
    launch(args)
