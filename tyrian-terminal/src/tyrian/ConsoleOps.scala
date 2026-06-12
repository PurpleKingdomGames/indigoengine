package tyrian

import tyrian.classic.Console

enum ConsoleOps derives CanEqual:
  case Stdout(out: String)
  case Stderr(out: String)

object ConsoleOps:

  def stdout(out: String): ConsoleOps.Stdout =
    ConsoleOps.Stdout(out)
  def stderr(out: String): ConsoleOps.Stderr =
    ConsoleOps.Stderr(out)
  def println(out: String): ConsoleOps.Stdout =
    ConsoleOps.Stdout(out)
  def errorln(out: String): ConsoleOps.Stderr =
    ConsoleOps.Stderr(out)

  extension (ops: ConsoleOps)
    def toConsole: Console[GlobalMsg] =
      ops match
        case Stdout(out) =>
          Console.Stdout(out)

        case Stderr(out) =>
          Console.Stderr(out)
