package tyrian

import indigoengine.shared.collections.Batch
import indigoengine.shared.typeclass.Monoid
import tyrian.classic.Console

/** ConsoleFragment is a JVM/Native view that produces an append only output, printing to stdout and stderr. It is set
  * up in the style of other 'fragment' views for consistency, but as this is an append only log, there is no view
  * diffing of the view before render.
  */
final case class ConsoleFragment(ops: Batch[ConsoleOps]):

  def |+|(other: ConsoleFragment): ConsoleFragment =
    ConsoleFragment.combine(this, other)

  def toConsole: Console[GlobalMsg] =
    ops.map(_.toConsole).foldLeft(Console.NoOp())((acc, next) => acc |+| next)

  def stdout(out: String): ConsoleFragment =
    this.copy(ops = ops :+ ConsoleOps.Stdout(out))
  def stderr(out: String): ConsoleFragment =
    this.copy(ops = ops :+ ConsoleOps.Stderr(out))
  def println(out: String): ConsoleFragment =
    stdout(out)
  def errorln(out: String): ConsoleFragment =
    stderr(out)

object ConsoleFragment:

  given Monoid[ConsoleFragment] =
    Monoid.instance(
      empty,
      combine
    )

  def stdout(out: String): ConsoleFragment =
    ConsoleFragment(Batch(ConsoleOps.Stdout(out)))
  def stderr(out: String): ConsoleFragment =
    ConsoleFragment(Batch(ConsoleOps.Stderr(out)))
  def println(out: String): ConsoleFragment =
    stdout(out)
  def errorln(out: String): ConsoleFragment =
    stderr(out)

  def empty: ConsoleFragment =
    ConsoleFragment(Batch.empty[ConsoleOps])

  def combine(a: ConsoleFragment, b: ConsoleFragment): ConsoleFragment =
    a.copy(ops = a.ops ++ b.ops)

  def combineAll(frags: Batch[ConsoleFragment]): ConsoleFragment =
    if frags.isEmpty then ConsoleFragment.empty
    else
      val h = frags.head
      val t = frags.tail

      t.foldLeft(h)(_ |+| _)

  def apply(ops: ConsoleOps*): ConsoleFragment =
    ConsoleFragment(Batch.fromSeq(ops))
