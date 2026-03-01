package tyrian

import indigoengine.shared.collections.Batch
import tyrian.classic.Terminal

final case class TerminalFragment(ops: Batch[TerminalOps]):

  def |+|(other: TerminalFragment): TerminalFragment =
    this.copy(ops = ops ++ other.ops)

  def toTerminal: Terminal[GlobalMsg] =
    ops.map(_.toTerminal).foldLeft(Terminal.NoOp())((acc, next) => acc |+| next)

object TerminalFragment:

  def empty[Msg]: TerminalFragment =
    TerminalFragment(Batch.empty[TerminalOps])

  def combineAll[Msg](frags: Batch[TerminalFragment]): TerminalFragment =
    if frags.isEmpty then TerminalFragment.empty
    else
      val h = frags.head
      val t = frags.tail

      t.foldLeft(h)(_ |+| _)

enum TerminalOps derives CanEqual:
  case Print(value: String)

object TerminalOps:

  extension (ops: TerminalOps)
    def toTerminal: Terminal[GlobalMsg] =
      ops match
        case Print(value) =>
          Terminal.Print(value)
